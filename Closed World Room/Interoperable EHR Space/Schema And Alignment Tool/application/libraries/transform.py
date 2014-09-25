# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os, sys, getopt, datetime, uuid, time

from lxml import etree
import xlrd
import csv
import codecs

from attributes import Attribute
from xls_utils import XlsAttribute
from owl_utils import OwlAttribute
import log
import configs

class MappedAttribute:
    def __init__(self, idx, rawAttribute, owlAttribute ):
        self.idx = idx
        self.attribute = rawAttribute.Name
        self.mappedTo = owlAttribute.Name
        self.RawAttribute = rawAttribute
        self.OwlAttribute = owlAttribute

def usePercentUpdate(percent, processUpdate, withEcho = False):
    if processUpdate != None:     
        processUpdate( percent )

    if withEcho:
        print("Progress: %(percent)f\n" % {'percent':percent})
        
def riseProcessUpdate(current, total, processUpdate, withEcho = False, partPercent = 100.0, currentPercent = 0.0):
    percent = (current * partPercent / total) + currentPercent
    usePercentUpdate(percent, processUpdate, withEcho)
    return percent

def saveTransformation(mappingFilePath, rawFilePath, outFilePath, processUpdate = None, processComplete = None, printInConsole = True):
    """
    This routine transforms the data-set from a RAW file into a RDF file, based on a XML mappings file
    
    mappingFilePath     - path to XML mapping file
    rawFilePath         - path to XLS raw data file
    outFilePath         - path to RDF transformation output file
    processUpdate       - callback for process status
    processComplete     - callback for process complete
    printInConsole      - echo on console flag
    
    The transformation is done like so:
    
    1.  It generates some internal mapping structures
    
    2.  It opens the rawFile ( XLS ) and it creates the output file
    
    3.  Gets the mappend attributes and total number their of cells
    
    4.  It transforms the XLS file:
        Foreach SHEET
            Foreach MAPPED_ATTRIBUTE in SHEET
                Foreach CELL for MAPPED_ATTRIBUTE
                    Checks if CELL value is not empty & is valid
                        Saves an RDF element on disk
    """
    
    currentPercent = 0.0
    usePercentUpdate(currentPercent, processUpdate, printInConsole)    
 
    """
    --------------------------------------------------------------------------    
    1. Obtain mappings structures
    --------------------------------------------------------------------------
    """
    with open(mappingFilePath, 'r') as mapping:
        mappingFileContent = mapping.read()
    
    # create collections
    rawAttributes = {}
    owlAttributes = {}
    rawMappingSet = {}
    
    getMappings(mappingFileContent, rawAttributes, owlAttributes, rawMappingSet)

    currentPercent += 2.0
    usePercentUpdate(currentPercent, processUpdate, printInConsole)

    # create the output file
    with codecs.open(outFilePath ,'w','utf8') as txt:
        
        t_start = time.clock()
        t_xls_start = t_start
        owlNamespaces = dict( (v,k) for k,v in configs.application_configs["owlNamespaces"].iteritems())
        owlMappedNamespaces = dict()
        # write RDF header
        rdfHeader = u"""<?xml version='1.0' encoding='UTF-8'?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
"""
        writedNamespaces = []
        temp=[]
        for rawId,owlIds in rawMappingSet.iteritems():
            for owlId in owlIds:
                if owlId in temp:
                    continue
                temp.append(owlId)
                owlA = owlAttributes[owlId]
                if owlA.Namespace:
                    if owlA.Namespace in owlNamespaces:
                        if not(owlA.Namespace in owlMappedNamespaces):
                            owlMappedNamespaces[owlA.Namespace]=owlNamespaces[owlA.Namespace]
                            rdfHeader +=u"""         xmlns:{0}="{1}"
    """.format(owlMappedNamespaces[owlA.Namespace], owlA.Namespace)
                    else:
                        owlMappedNamespaces[owlA.Namespace] = owlA.Namespace
        if len(owlMappedNamespaces) == 0:
            owlMappedNamespaces[""]="clin"
            rdfHeader +=u"""         xmlns:clin="http://www.linked2safety-project.eu/owl/v1/Clinical#"
"""
        rdfHeader +=u"""         xmlns:map="http://www.linked2safety-project.eu/owl/v1/Mapping#">
    <rdf:Description rdf:about="&map;RecordDescription">"""
        mappedOwlAttributes = temp
        internalCounters = { k:v for k,v in owlAttributes.items() if v.Name in configs.settings["transformation"]["internalCounterCollumns"] and k in mappedOwlAttributes}
        
        rdfHeader += u"""
        <map:hasInternalNo>{0}</map:hasInternalNo>""".format( "yes" if internalCounters else "no" )
        temp=[]
        for rawId,owlIds in rawMappingSet.iteritems():
            for owlId in owlIds:
                if owlId in temp:
                    continue
                temp.append(owlId)
                owlA = owlAttributes[owlId]
                if owlA.Range:
                    rdfHeader += u"""
        <map:hasRange rdf:about="{0}{1}">{2}</map:hasRange>""".format( "&{0};".format(owlMappedNamespaces[owlA.Namespace]) if owlA.Namespace in owlMappedNamespaces else owlA.Namespace , owlA.Name, owlA.Range)
        rdfHeader +=u"""
    </rdf:Description>
"""
        txt.write(rdfHeader)
        
        """
        --------------------------------------------------------------------------        
        2. Open XLS file
        --------------------------------------------------------------------------
        """
        wb = xlrd.open_workbook( rawFilePath )             
        
        currentPercent += 18.0
        usePercentUpdate(currentPercent, processUpdate, printInConsole)        
        
        t_xls_end = time.clock()
        if printInConsole:
            print("opened XLS file in :%f\r\n" % (t_xls_end - t_xls_start))       
        
        """
        --------------------------------------------------------------------------        
        3. Make internal mapping structures for better processing
        --------------------------------------------------------------------------        
        """
        # find out total numer of data cells & mark mapped attributes
        mappedSheets = {}        
        totalCellNo = 0
        sheetsPrimaryKeys = {}
        sheetsRawAttributes = {}
        for sheetName in wb.sheet_names():
            ws = wb.sheet_by_name(sheetName)            

            num_cols = ws.ncols - 1
            num_rows = ws.nrows - 1
            mappedAttributes = {}
            sheetsPrimaryKeys[sheetName] = []
            sheetsRawAttributes[sheetName] = {}
            curr_col = -1            
            while curr_col < num_cols:
                curr_col += 1             
                cellValue = ws.cell_value(0, curr_col)
                if cellValue!=None:
                    attributeName = cellValue
                    rawAttribute = [v for k, v in rawAttributes.iteritems() if (v.Namespace == sheetName and v.Name == attributeName)]                                              
                    if len(rawAttribute) > 0:
                        sheetsRawAttributes[sheetName][curr_col] = rawAttribute[0]
                        if rawAttribute[0].IsPrimaryKey:
                            sheetsPrimaryKeys[sheetName].append(curr_col)
                            
                        # check if a mapping exists
                        if rawAttribute[0].Id in rawMappingSet:
                            for owlAttributeId in rawMappingSet[rawAttribute[0].Id]:                     
                                # check if mapped attribute is in collection
                                if owlAttributeId in owlAttributes:
                                    # store the exact mapping for optimal processing
                                    mappedAttributes[curr_col] = MappedAttribute( curr_col, rawAttribute[0], owlAttributes[owlAttributeId] )
                            
            totalCellNo += len(mappedAttributes) * num_rows                            
            mappedSheets[sheetName] = mappedAttributes                               
        currentCellNo = 0
        validCellNo = 0
        loopPercent = 80.0
        """
        --------------------------------------------------------------------------        
        4. Process cells for the mapped attributes
        --------------------------------------------------------------------------        
        """
        t_process_start = time.clock()
        fivePercent = int(totalCellNo / 20)
        primaryKeys={}
        # for each sheet        
        for sheetName in wb.sheet_names():
            t_sheet_start = time.clock()
            
            ws = wb.sheet_by_name(sheetName)
                        
            curr_row = 0 # -1
            mappedAttributes = mappedSheets[sheetName]
                
            num_rows = ws.nrows - 1
            rowPrimaryKeys = sheetsPrimaryKeys[sheetName]
            rowPrimaryKeys.sort();
            primaryKeyCollumnNames = u'\u00b6'.join([u"{t}".format( t = sheetsRawAttributes[sheetName][ i ].Name ) for i in rowPrimaryKeys])
            
            while curr_row < num_rows:
                curr_row += 1                            
                outputAttributes = ''
                
                if rowPrimaryKeys:
                    #compute primary key
                    rowPrimaryKey = primaryKeyCollumnNames + u'\u00a6' + u'\u00a7'.join([u"{t}".format( t = ws.cell_value(curr_row, i) ) for i in rowPrimaryKeys])
                    #check if rowPrimaryKey is in list
                    if not(rowPrimaryKey in primaryKeys):
                        primaryKeys[rowPrimaryKey] = uuid.UUID(bytes=os.urandom(16), version=1)
                    guid = primaryKeys[rowPrimaryKey]
                else:
                    guid = uuid.UUID(bytes=os.urandom(16), version=1)
                
                for mappedAttribute in [v for k, v in mappedAttributes.iteritems()]:
                    ns = mappedAttribute.OwlAttribute.Namespace
                    nsEntity = ns
                    if ns in owlMappedNamespaces:
                        ns = owlMappedNamespaces[ns]
                        nsEntity = u"&{0};".format(ns)
                    # get cell value
                    value = u"{v}".format( v = ws.cell_value(curr_row, mappedAttribute.idx) )

                    # save non-empty value mappings
                    value = value.strip()
                    if configs.application_configs["rdfIncludeEmptyAttributes"] == "true" or len(value) > 0:
                        outputAttributes += u"\t\t<%(ns)s:%(mappedTo)s>%(value)s</%(ns)s:%(mappedTo)s>\n" % {'mappedTo':mappedAttribute.mappedTo, 'value':value, 'ns' : ns}
                                          
                        validCellNo += 1
                    
                    currentCellNo += 1
                    if fivePercent==0 or currentCellNo % fivePercent == 0:
                        riseProcessUpdate( currentCellNo, totalCellNo, processUpdate, True, loopPercent, currentPercent )
                
                if len(outputAttributes) > 1:
                    outputAttributes =  u"\t<rdf:Description rdf:about=\"&map;ID:%(guid)s\">\n%(children)s\t</rdf:Description>\n"  % {'guid':guid, 'children': outputAttributes}
                    txt.write(outputAttributes)             
                
            t_sheet_end = time.clock()
            if printInConsole:            
                print("processed sheet %(a)s in :%(b)f\r\n" %{ 'a': sheetName, 'b': (t_sheet_end - t_sheet_start)})                        
        
        # write RDF footer
        txt.write(u"</rdf:RDF>")
        
        currentPercent += loopPercent
        t_process_end = time.clock()
        if printInConsole:        
            print("processed all sheets; valuedCells: %(validCells)d, mappedCells: %(totalCells)d, in: %(elapsed)f s.\r\n" \
                  % {'validCells':validCellNo, 'totalCells':totalCellNo, 'elapsed':(t_process_end - t_process_start)})
            
        usePercentUpdate(currentPercent, processUpdate, printInConsole) 
        
        if printInConsole:        
            print("finished all :%f\r\n" % (time.clock() - t_start))  
    
    # rise the complete process callback
    if processComplete != None:
        processComplete(outFilePath)
        
    import subprocess
    import log
    params = {
        "rdfFile" : outFilePath,
        "rdfFileNoExt" : os.path.splitext(outFilePath)[0],
        "appDir": configs.settings["application_dir"],
        "mappingFile" : mappingFilePath
    }
    DETACHED_PROCESS = 0x00000008
    for cmd in configs.application_configs["postTransformationConsoleCommands"]:
        temp = cmd % params
        if temp:
            print temp
            log.write(str("External Command: %(cmd)s" % { "cmd": temp}))
            
            #Popen( temp ,shell=True,stdin=None,stdout=None,stderr=None,close_fds=True,creationflags=DETACHED_PROCESS)
            task = subprocess.Popen( temp , shell=True )
            task.wait()

def getMappings(mappingFileContent, rawAttributes, owlAttributes, rawMappingSet):
    # TODO: validate mappingFileContent with xsd schema
    #xsdFilePath = os.path.join(configs.settings['resources']['path'], configs.settings['resources']['xsd']['mappingFileValidateSchemaFile'])
    #with open(xsdFilePath, "r") as f:
    #    schema_root = etree.XML(f.read());        
    #    schema = etree.XMLSchema(schema_root)
    #    
    #parser = etree.XMLParser(schema = schema)
    tree = etree.fromstring( mappingFileContent) #, parser )
    
    # prepare collections
    rawAttributes.clear()
    owlAttributes.clear()
    rawMappingSet.clear()
    
    roots = tree.xpath('//mappings')
    if len(roots) > 0:
        # we have a root element
        root = roots[0]

        # parse XlsAttributes
        sourceAttributes = etree.ElementTree(root).xpath('//sourceAttributes/sourceAttribute')
        if len(sourceAttributes) > 0:
            # get XlsAttributes
            owls = {}
            for sourceAttribute in sourceAttributes:
                xlsAttribute = XlsAttribute(sourceAttribute.get("namespace"), sourceAttribute.get("name"), True if sourceAttribute.get("isPrimaryKey") else False )
                rawAttributes[xlsAttribute.Id] = xlsAttribute
                
                # check if we have mappings
                destinationAttributes = etree.ElementTree(sourceAttribute).xpath('//destinationAttribute')
                if len(destinationAttributes) > 0:
                    # we do, save them
                    destinationAttribute = destinationAttributes[0]
                    name = u"{0}".format(destinationAttribute.get("name", ''))
                    namespace = u"{0}".format(destinationAttribute.get("namespace", ''))
                    key = name + u'\u00a6' + namespace
                    if key in owls:
                        owlAttribute = owls[key]
                    else:
                        owlAttribute = OwlAttribute(destinationAttribute.get("name"), destinationAttribute.get("dataType"), range = destinationAttribute.get("range", ''), namespace=destinationAttribute.get("namespace", ''))
                        owls[key] = owlAttribute
   
                    owlAttributes[owlAttribute.Id] = owlAttribute
                    
                    if not ( xlsAttribute.Id in rawMappingSet):
                        rawMappingSet[xlsAttribute.Id] = []
                    # save mapping                    
                    rawMappingSet[xlsAttribute.Id].append( owlAttribute.Id);


def main(argv):
    mappingFilePath = ''
    rawFilePath = ''
    outFilePath = ''
    helpMsg = 'transform.py -m <mappingFilePath> -r <rawFilePath> -o <outputfilePath>'
    
    try:
        opts, args = getopt.getopt(argv,"hm:r:o:")
    except getopt.GetoptError:
        print helpMsg
        sys.exit(2)    
    
    if opts == False:
        print("Invalid arguments. Check -h for help.")
        sys.exit(2)
    
    for opt, arg in opts:
        if opt == '-h':
            print helpMsg
            sys.exit()
        elif opt == '-m':
            mappingFilePath = arg
        elif opt == '-r':       
            rawFilePath = arg           
        elif opt == '-o':        
            outFilePath = arg         

    if  os.path.isfile(mappingFilePath) == False:
        print("Mapping file couldn't be found.")
        sys.exit(2)
    if  os.path.isfile(rawFilePath) == False:
        print("Raw file couldn't be found.")
        sys.exit(2)                              
    if  os.path.isfile(outFilePath):
        print("Output file already exists.")
        sys.exit(2)
        
    saveTransformation(mappingFilePath, rawFilePath, outFilePath)
                        
if __name__ == "__main__":
   main(sys.argv[1:])