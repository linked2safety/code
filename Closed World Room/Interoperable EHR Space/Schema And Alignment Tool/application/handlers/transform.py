# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os
import threading
import time
import re
from datetime import datetime
from handlers import custom
from libraries import log, transform
import configs

class Handler( custom.Handler ):
    
    def resetVariables( self, variables ):
        variables["ProcessIsRunning"] = False
        variables["ProcessIsCompleted"] = False
        variables["ProcessIsFaulted"] = False
        variables["ProcessErrors"] = None
        variables["CurrentProcess"] = 0
        variables["RawFileName"] = ''
        variables["MappingFileName"] = ''
        variables["OutputFileName"] = ''
        variables["RawFile"] =  None
        variables["MappingFile"] = None
        variables["ProviderName"] = configs.application_configs["providerName"]
	variables["ApplicationVersion"] = configs.application_configs["version"]
        if not( "OutputFile" in variables):
            variables["OutputFile"] = None
	variables["OutputFileDownload"] = False
    
    def getVariables( self ):
        variables = self.getSession()
        if not ("ProcessIsRunning" in variables):
            self.resetVariables(variables)
            
        return variables
    
    def index( self, **params ):
        variables = self.getVariables()
        hasStarted = False
        if variables["ProcessIsRunning"] == False:
	    rawFile = self.get_argument("raw-file-server", None)
	    if rawFile!=None and os.path.isfile(rawFile)==False:
		rawFile = None
	    
	    mappingFile = self.get_argument("mapping-file-server", None)
	    if mappingFile!=None and os.path.isfile(mappingFile)==False:
		mappingFile = None
		
	    if rawFile == None and "raw-file" in self.request.files and self.upload(self.request.files["raw-file"], directory = 'raws') == True:
		rawFile =  os.path.join(self.settings["temp_path"], "raws", self.request.files["raw-file"][0]["filename"])
		
            if mappingFile == None and "mapping-file" in self.request.files and self.upload(self.request.files["mapping-file"], directory = 'mappings') == True:
		mappingFile = os.path.join(self.settings["temp_path"], "mappings", self.request.files["mapping-file"][0]["filename"])
		
	    if rawFile != None and os.path.isfile(rawFile) and mappingFile != None and os.path.isfile(mappingFile):
		hasStarted = True
		variables["RawFile"] =  rawFile
		variables["MappingFile"] = mappingFile
		
		directory = self.get_argument("output-file-server", None)
		if directory == None or len(directory) == 0 or os.path.exists(directory)==False:
		    variables["OutputFileDownload"] = True
		    directory = os.path.join(self.settings['temp_path'], "data")
		    
		
		variables["RawFileName"] = os.path.basename(variables["RawFile"])
		variables["MappingFileName"] = os.path.basename(variables["MappingFile"])
		
		mappingFilename = os.path.splitext(variables["MappingFileName"])[0]
		results = re.match("(.*)-[0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2} [0-9]{2,2}-[0-9]{2,2}-[0-9]{2,2}", mappingFilename, re.UNICODE)
		
		baseName = results.group(1) if results!=None and len(results.group(1))>0 else mappingFilename
		
		if os.path.exists(directory) == False:
		    os.mkdir(directory,0755)
		
		variables["OutputFile"] = os.path.join(directory, datetime.now().strftime(baseName + "-%Y-%m-%d %H-%M-%S.rdf"))
		

		variables["OutputFileName"] = os.path.basename(variables["OutputFile"])
		
		self.__transform( variables )
        else:
            variables["ProcessIsRunning"] = True
        
        isFaulted = variables["ProcessIsFaulted"]
        isCompleted = variables["ProcessIsCompleted"]
        
        viewVariables = { k:v for k,v in variables.iteritems() }
        
        viewVariables["ProcessHasStarted"] = hasStarted
	viewVariables['BrowseFileOn'] = configs.application_configs["browseFileOn"]
        self.render( "transform.html", **viewVariables )
        
        if isFaulted == True or isCompleted == True:
            self.resetVariables( variables )
    
    def getStatus( self, **params ):
        variables = self.getVariables()
        result = {
                "progress"      : variables["CurrentProcess"],
                "isRunning"     : variables["ProcessIsRunning"] == True,
                "isCompleted"   : variables["ProcessIsCompleted"] == True,
                "isFaulted"     : variables["ProcessIsFaulted"] == True,
                "errors"        : str(variables["ProcessErrors"]) if variables["ProcessErrors"]!= None else ''
            }
        return self.writeJSON(result)
        
    def downloadOutputFile(self, **params):
        variables = self.getVariables()
        filePath = variables["OutputFile"]
        if filePath != None:
            if os.path.exists( filePath ):
                with open( filePath ,"rb") as f:
                    ext = filePath.split(".")[-1:][0]
                    self.set_header("Content-Type", "text/{0}".format(ext))
                    self.set_header("Content-Disposition", 'attachment;filename="{0}"'.format(os.path.basename( filePath )))
                    return self.write(f.read())
        self.set_status(404)
    
    def __transform(self, variables ):
        threading.Thread(target = lambda : self.__runTransformation( variables )).start()
        
    def __runTransformation (self, variables ):
        variables["ProcessIsRunning"] = True
        variables["ProcessIsCompleted"] = False
        variables["ProcessIsFaulted"] = False
        variables["ProcessErrors"] = None
        try:
            transform.saveTransformation( variables["MappingFile"], variables["RawFile"], variables["OutputFile"],
                                                processUpdate = lambda percent: self.__onProgressUpdate( variables, percent )
                                         )
            variables["CurrentProcess"] = 100
            variables["ProcessIsCompleted"] = True
        except Exception as ex:
	    log.writeTraceback()
            variables["ProcessIsFaulted"] = True
            variables["ProcessErrors"] = ex
        finally:
            variables["ProcessIsRunning"] = False
    
    def __onProgressUpdate(self, variables, percent):
        percentInt = int(percent)
        if percentInt<100:
            variables['CurrentProcess'] = percentInt