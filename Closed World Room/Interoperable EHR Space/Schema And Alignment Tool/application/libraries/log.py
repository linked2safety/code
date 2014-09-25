# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os
import configs
import datetime
import pickle
import traceback

def write( data, pickleIt = False):
    if data == None:
        return
    logFileName = '{:%Y-%m-%d.log}'.format( datetime.datetime.now())
    
    directory = configs.settings['log_path']
    
    if os.path.exists(directory) == False:
        os.mkdir(directory,0755)
        
    logFile = os.path.join( directory, logFileName )
    
    dumpedData = ''
    if pickleIt==True or type(data)== unicode:
        dumpedData = pickle.dumps(data)
    else:
        dumpedData = str(data)
    
    with open( logFile , 'ab') as fh:
        fh.writelines(
            "\n%(logSeparator)s\n%(date)s\n%(data)s" % {
                "logSeparator":    "---------------------------------------------------------------",
                "date" :        "{:%Y-%m-%d %H:%M:%S}:".format( datetime.datetime.now()),
                "data" :        dumpedData
            }
        )

def writeTraceback():
    write(traceback.format_exc())