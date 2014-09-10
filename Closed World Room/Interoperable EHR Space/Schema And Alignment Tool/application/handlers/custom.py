# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
from libraries import log
from tornado import web, httputil
import json
import socket
import os
import uuid
import types
import datetime
import base64
import uuid

class Handler( web.RequestHandler ):
    __SESSION = {}
        
    def getClientSid( self ):
        clientsid = self.get_secure_cookie(self.settings['cookiesid']['name'], None)
        if clientsid == None:
            clientsid = base64.b64encode( uuid.uuid4().bytes + uuid.uuid4().bytes )
            self.set_secure_cookie(self.settings['cookiesid']['name'], clientsid, self.settings['cookiesid']['expires_days'] )
        return clientsid
    
    def getSession( self ):
        clientsid = self.getClientSid()
        if not(clientsid in self.__SESSION):
            self.__SESSION[clientsid] = dict()
        return self.__SESSION[clientsid]
    
    def clearSession( self ):
        clientsid = self.getClientSid()
        if clientsid in self.__SESSION:
            del self.__SESSION[clientsid]
    
    def get(self, **params):
        try:
            self.__dispatchAction( **params )
        except Exception as ex:
            log.writeTraceback()
            raise
    
    def post(self, **params):
        try:
            self.__dispatchAction( **params )
        except Exception as ex:
            log.writeTraceback()
            raise
    
    def __dispatchAction(self, **params):
        func = None
        if "action" in params:
            func = getattr(self, params['action'])
        else:
            func = self.index
        if func != None:
            func( **params )
    
    #Method for uploading files to specified directory, default temp
    def upload(self, fileinfo, directory = None, keepName = True):
        
        if directory != None:
            directory = os.path.join(self.settings['temp_path'], directory)
            
        if os.path.exists(directory) == False:
            os.mkdir(directory,0755)
        
        if fileinfo:
            if (type(fileinfo) is list  and len(fileinfo))>0:
                    fileinfo = fileinfo[0]
              
            if not type(fileinfo) is httputil.HTTPFile:
                return False
            
            if fileinfo.has_key('filename') == False or fileinfo.has_key('body') == False:
                return False
                
            fname = fileinfo['filename']
            localFname = fname
            
            if keepName == False:
                extn = os.path.splitext(fname)[1]
                localFname = str(uuid.uuid4()) + extn
                
            filePath = os.path.join(directory, localFname)

            #Writing the uploaded file content to local file
            with open( filePath , 'wb') as fh:
                fh.write(fileinfo['body'])
            return True
        
        return False
    
    def writeJSON(self, variable, setHeader = True):
        if setHeader == True:
            self.set_header("Content-Type", "application/json")
        return self.write(json.dumps(variable))
