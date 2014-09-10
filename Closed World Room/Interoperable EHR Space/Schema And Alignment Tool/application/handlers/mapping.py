# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
import os
import re
import traceback
from datetime import datetime
from handlers import custom
from libraries import log, xls_utils, owl_utils, xml_utils
import configs
reOwlAttributeName = re.compile(r"^owlAttribute\[(.+)]$")
reMappingFilename = re.compile(r"[^\w]+", re.M | re.U | re.I)

class Handler( custom.Handler ):
    ViewVariables = {}
        
    def reset( self, variables ):
        variables['BrowseFileOn'] = configs.application_configs["browseFileOn"]
        variables['mappedRawAtributes'] = {}
        variables['mappedOwlAtributes'] = {}
        variables['RawAttributes'] = {}
        variables['RawAttributesCategories'] = []
        variables['raw_filename'] = ''
        variables['saved_mapping_filename'] = ''
        variables['edit_filename'] = ''
        variables['errors'] = []
        variables["MappingDescription"] = configs.application_configs["mappingDescription"]
        variables["MappingComments"] = ''
        variables["ApplicationVersion"] = configs.application_configs["version"]
        
    def get(self, **params):
        variables = self.getViewVariables()
        self.mapping(variables)
    
    def post(self, **params):
        variables = self.getViewVariables()
        try:
            mappingDescription = self.get_argument("mapping-description", None)
            if mappingDescription!=None:
                variables['MappingDescription'] = mappingDescription
                
            mappingComments = self.get_argument("mapping-comments", None)
            if mappingComments!=None:
                variables['MappingComments'] = mappingComments
                
            #if raw should change
            if  self.request.arguments.has_key("raw-file-server"):
                filePath = self.get_argument('raw-file-server');
                if os.path.exists(filePath):
                    self.reset( variables )
                    variables['raw_filename'] = os.path.basename(filePath);
                    variables['RawAttributes'] = self.getRawAttributesFromUpload(None, filePath)
                    self.setupRawCategories(variables);
                
                
            #if owl should change
            if  self.request.files.has_key("raw-file"):
                self.reset( variables )
                variables['raw_filename'] = self.request.files["raw-file"][0]['filename']
                variables['RawAttributes'] = self.getRawAttributesFromUpload(self.request.files["raw-file"])
                self.setupRawCategories(variables);
            
            for k,v in variables['RawAttributes'].iteritems():
                v.IsPrimaryKey = True if self.get_argument( "rawAttribute[{0}]IsPrimaryKey".format(v.Id), False) else False
            
            #if raw should change
            if  self.request.arguments.has_key("edit-file-server"):
                filePath = self.get_argument('edit-file-server');
                if os.path.exists(filePath):
                    self.reset( variables )
                    variables['edit_filename'] = os.path.basename(filePath);
                    self.getEditAttributesFromUpload( None, variables, filePath )
            
            #if owl should change
            if  self.request.files.has_key("edit-file"):
                self.reset( variables )
                variables['edit_filename'] = self.request.files["edit-file"][0]['filename']
                list1 = self.getEditAttributesFromUpload(self.request.files["edit-file"], variables )
            
            newList = [(reOwlAttributeName.search(k).group(1), v) for k, v in self.request.arguments.iteritems() if reOwlAttributeName.match(k) != None]
            if len(newList)>0:
                variables['mappedRawAtributes'] = {}
                for o, rs in newList:
                    for r in rs:
                        variables['mappedRawAtributes'][r] = o
                variables['mappedOwlAtributes'] = newList
                
                for k, v in variables['owlAttributes'].iteritems():
                    v.Range = self.get_argument("owlAttribute[{0}]range".format(v.Id), '')
                
                mappingsXml = xml_utils.saveMappingFile(variables['RawAttributes'],
                                                        variables['owlAttributes'],
                                                        variables['mappedRawAtributes'],
                                                        variables['MappingDescription'],
                                                        variables['MappingComments'],
                                                        variables['raw_filename'],
                                                        variables["ProviderName"])
               
                if len(mappingsXml)>0:
                    filename = variables['MappingDescription'] if variables['MappingDescription']!=None and len(variables['MappingDescription']) >0 else u"Mapping"
                    filename = reMappingFilename.sub(u" ",filename ).strip().replace(u" ", u"_")
                    self.set_header("Content-Type", "text/xml; charset=utf-8")
                    self.set_header("Content-Disposition", u'attachment;filename="{0}-{1}"'.format(filename, datetime.now().strftime(u"%Y-%m-%d %H-%M-%S.xml")))
                    self.write(mappingsXml)
                    return
        except Exception as ex:
            log.writeTraceback()
            variables['errors'][:] = ex
        self.mapping(variables)
    
    def getViewVariables(self):
        variables = self.getSession()
        if not("RawAttributes" in variables):
            self.reset(variables)
            variables['owlAttributes']={}
            variables['RootOwlCategory'] = owl_utils.OwlCategory("Root")
            for owl in configs.application_configs['owlAttributesFiles']:
                owlFile = owl if os.path.exists(owl) else os.path.join(self.settings['resources']['owls_path'], owl) 
                variables['owlAttributes'].update( self.getOwlAttributesFromFile(owlFile, variables))
            
            variables["ProviderName"] = configs.application_configs["providerName"]
            variables["MappingDescription"] = configs.application_configs["mappingDescription"]
            
        variables['errors'] = []
        return variables
    
    def mapping(self, variables):
        #load default attributes
        #if len(variables['mapping_file_name'])==0:
        #    variables['mapping_file_name'] = self.get_argument('mapping_file_name', '')
        #if len(variables['raw_file_name'])==0:
        #    variables['raw_file_name'] = self.get_argument('raw_file_name', '')
            
        #variables.update({'mappedDescrinationAttributes' : [x['value'] for x in variables['mappedAttributes'] if 'value' in x and len(x['value'])>0]})
        self.render( "mapping/default.html", **variables )
        
    
    def getRawAttributesFromUpload( self, fileInfo, filePath = None ):
        # get raw attributes from an xls
        parser = xls_utils.XlsParser()
        if filePath!=None:
            attributes = parser.parse( None, filePath )
        else:
            attributes = parser.parse(fileInfo[0]['body'])
        rawAttributes = {}
        for att in attributes:
            rawAttributes[att.Id] = att
        return rawAttributes
    
    def getEditAttributesFromUpload(self, fileInfo, variables, mappingFilePath = None ):
        # get raw attributes from an xls
        if mappingFilePath!=None:
            xml_utils.openMappingFile(None, mappingFilePath, variables)
        else:
            xml_utils.openMappingFile(fileInfo[0]['body'], None, variables)
        variables['mappedOwlAtributes'] = {}
        for r,o in variables['mappedRawAtributes'].iteritems():
            if not (o in variables['mappedOwlAtributes']):
                variables['mappedOwlAtributes'][o] = []
            variables['mappedOwlAtributes'][o].append(r)
        self.setupRawCategories(variables);
    
    def getOwlAttributesFromFile(self, fileOwlPath, variables):
        # get raw attributes from an xls        
        filePath = os.path.join(fileOwlPath)

        parser = owl_utils.OwlParser()
        attributes = parser.parse(filePath, variables['RootOwlCategory'])
        
        owlAttributes = {}
        for att in attributes:
            owlAttributes[att.Id] = att
        return owlAttributes
    
    def setupOwlCategories( self, variables ):
        categories = {}
        for k,v in sorted( variables['owlAttributes'].iteritems(), lambda x, y: cmp(x[1].PrintValue.lower(), y[1].PrintValue.lower())):
            if not (v.Category in categories):
                categories.update({v.Category : []})
            categories[v.Category].append(k)
        variables['OwlAttributesCategories'] = sorted(categories.iteritems(), lambda x, y: cmp(x[0].Name.lower(), y[0].Name.lower()))
        
    def setupRawCategories( self, variables ):
        categories = {}
        for k,v in sorted( variables['RawAttributes'].iteritems(), lambda x, y: cmp(x[1].PrintValue.lower(), y[1].PrintValue.lower())):
            if not (v.Category in categories):
                categories.update({v.Category : []})
            categories[v.Category].append(k)
        variables['RawAttributesCategories'] = sorted(categories.iteritems(), lambda x, y: cmp(x[0].lower(), y[0].lower()))