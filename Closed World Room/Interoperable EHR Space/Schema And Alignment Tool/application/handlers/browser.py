# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os
import re
import configs
from handlers import custom
from libraries import log

class Handler( custom.Handler ):
    def index(self, **params):
        try:
            configLocationName =  "LastBrowsingLocation%(name)s" % {"name" : self.get_argument("configName", "")}
            
            defaultLocation = configs.application_configs[configLocationName] if configLocationName in configs.application_configs else None
            if defaultLocation==None or os.path.isdir(os.path.realpath(os.path.join(defaultLocation))) == False:
                defaultLocation = u"."
                
            directory = self.get_argument("location", defaultLocation)
            realpath = os.path.realpath(os.path.join(directory))
            
            if configLocationName in configs.application_configs:
                configs.updateSettings( configLocationName, realpath if os.path.isdir(realpath) else u"." )
                
            filterFiles = self.get_argument("filterFiles", None)
            files = []
            directories = []
            for e in os.listdir(realpath):
                fullEntry = os.path.join(realpath, e)
                if os.path.isfile(fullEntry):
                    if filterFiles!=None:
                        if re.match("^{0}$".format(filterFiles), e, re.IGNORECASE | re.UNICODE) == None:
                            continue
                    files.append(e)
                else:
                    if os.path.isdir(fullEntry):
                        directories.append(e)
            return self.writeJSON({
                "parentDirectory"   : "..",
                "current"           : realpath + (os.path.sep if realpath[-1:]!=os.path.sep else ''),
                "directories"       : directories,
                "files"             : files
            })
        except Exception as ex:
                return self.writeJSON({
                    "error" : str(ex)
                })
    
    def drives(self, **params):
        try:
            items = re.findall(r"[A-Z]+:.*$",os.popen("mountvol /").read(),re.MULTILINE)
            return self.writeJSON({
                "drives" : items
            })
        except Exception as ex:
            return self.writeJSON({
                "error" : str(ex)
            })