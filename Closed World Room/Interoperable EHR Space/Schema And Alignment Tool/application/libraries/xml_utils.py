# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os
import datetime
from lxml import etree
import log
from xls_utils import XlsAttribute
from owl_utils import OwlAttribute
import configs

def saveMappingFile(rawAttributes, owlAttributes, mappingSet, description, comments, fileName, providerName):
    #define root element
    now = "{:%Y-%m-%dT%H:%M:%S}".format(datetime.datetime.now() )    

    root = etree.Element("mappings", { k:v for k, v in {
                                                            "createDate": now,
                                                            "description": description,
                                                            "comments": comments,
                                                            "provider": providerName
                                                        }.iteritems() if v!=None and len(v) > 0
                                       });
    # create source attributes collection    
    sourceAttributes = etree.SubElement(root, "sourceAttributes", { k:v for k, v in {
                                                                                        "fileName": fileName
                                                                                    }.iteritems() if v!=None and len(v) > 0
                                                                   });
    
    # for each attribute from the RAW file
    for k, rawAttribute in rawAttributes.iteritems():        
        sourceAttribute = etree.SubElement(sourceAttributes, "sourceAttribute",
                                           { k:v for k, v in {
                                                            "name": rawAttribute.Name,
                                                            "namespace": rawAttribute.Namespace,
                                                            "dataType": rawAttribute.DataType if rawAttribute.DataType!=None and len(rawAttribute.DataType) >0 else None,
                                                            "isPrimaryKey": "true" if rawAttribute.IsPrimaryKey else None
                                                        }.iteritems() if v!=None
                                           }
                                           );
        
        # see if it has a mapping for a given OWL attribute
        if rawAttribute.Id in mappingSet:
            owlId = mappingSet[rawAttribute.Id]
            if owlId in owlAttributes:
                owlAttribute = owlAttributes[owlId]                
                if (not (owlAttribute is None)):
                    destinationAttributes = etree.SubElement(sourceAttribute, "destinationAttribute",
                                            { k:v for k, v in {
                                                            "name": owlAttribute.Name,
                                                            "namespace": owlAttribute.Namespace,
                                                            "dataType": owlAttribute.DataType,
                                                            "range": owlAttribute.Range
                                                        }.iteritems() if v!=None and len(v) > 0
                                           });                    
                
    return etree.tostring(root, pretty_print=True, xml_declaration=True, encoding='UTF-8')

def openMappingFile(mappingFileContent, mappingFilePath, variables):
    xsdFilePath = os.path.join(configs.settings['resources']['path'], configs.settings['resources']['xsd']['mappingFileValidateSchemaFile'])
    with open(xsdFilePath, "r") as f:
        schema_root = etree.XML(f.read());        
        schema = etree.XMLSchema(schema_root)
        
    parser = etree.XMLParser(schema = schema)
    
    if mappingFilePath:
        with open(mappingFilePath, "r") as xmlf:
            mappingFileContent = xmlf.read()
    
    tree = etree.fromstring( mappingFileContent, parser )
    
    # clear current mapped attributes & mappings - we'll read it from mappingFileContent
    variables['RawAttributes'].clear();
    variables['mappedRawAtributes'].clear();
    
    roots = tree.xpath('//mappings')
    if len(roots) > 0:
        # we have a root element
        root = roots[0]
        
        # get properties
        createDate = root.get("createDate")
        variables['MappingDescription'] = root.get("description")
        
        variables['MappingComments'] = root.get("comments")
        
        # parse XlsAttributes
        for sourceAttributes in etree.ElementTree(root).xpath('//sourceAttributes'):
            variables['raw_filename'] = sourceAttributes.get("fileName")
            for sourceAttribute in sourceAttributes.xpath('sourceAttribute'):
                # get XlsAttributes
                xlsAttribute = XlsAttribute(sourceAttribute.get("namespace"), sourceAttribute.get("name"), True if sourceAttribute.get("isPrimaryKey") else False)
                variables['RawAttributes'][xlsAttribute.Id] = xlsAttribute
                
                # check if we have mappings
                destinationAttributes = etree.ElementTree(sourceAttribute).xpath('//destinationAttribute')
                if len(destinationAttributes) > 0:
                    # we do, save them
                    destinationAttribute = destinationAttributes[0]
                    owlAttribute = OwlAttribute(destinationAttribute.get("name"), destinationAttribute.get("dataType"), namespace=destinationAttribute.get("namespace"))
                    
                    # search if OwlAttribute is known
                    existingOwlAttributes = [v for k, v in variables['owlAttributes'].iteritems() if v.Name == owlAttribute.Name]
                    if len(existingOwlAttributes) > 0:
                        # yes, we konw it
                        owlAttribute = existingOwlAttributes[0]
                    else:
                        # no, we'll add it
                        variables['owlAttributes'][owlAttribute.Id] = owlAttribute
                    owlAttribute.Range = destinationAttribute.get("range", '')
                    
                    # save mapping                    
                    variables['mappedRawAtributes'][xlsAttribute.Id] = owlAttribute.Id
