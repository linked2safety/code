# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import tornado.httpserver
import tornado.ioloop
import tornado.options
import tornado.web
import webbrowser
import sys
import os
from tornado.options import define, options

#import application configs
import configs
import mappings

openInBrowser = False
if "open in browser" in sys.argv:
    sys.argv.remove("open in browser")
    openInBrowser = True


class Application(tornado.web.Application):
    def __init__(self):
        tornado.web.Application.__init__(self, mappings.handlers, **configs.settings)

def main():
    from libraries.tray import SysTrayRunner
    SysTrayRunner()
    
    global openInBrowser
    tornado.options.parse_command_line()
    app = Application()
    print ("\nStarting tornado web-server listening on {0}: {1}...".format( configs.application_configs["address"], configs.application_configs["port"] ))
    app.listen( address = configs.application_configs["address"], port = configs.application_configs["port"] ) 
    if openInBrowser == True:
        print ("\nhttp://{0}:{1}/ in a web browser".format( configs.application_configs["address"], configs.application_configs["port"] ))
        webbrowser.open("http://{0}:{1}/".format(configs.application_configs["address"], configs.application_configs["port"] ),0,True )
    tornado.ioloop.IOLoop.instance().start()

if __name__ == "__main__":
    main()