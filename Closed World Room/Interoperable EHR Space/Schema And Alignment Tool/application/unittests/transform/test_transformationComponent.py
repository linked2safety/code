# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import unittest

# Here's our "unit tests".
class TransformationComponentTests(unittest.TestCase):
    def testOpenRawDataFile(self):
        pass
    def testValidateRawDataFile(self):
        pass
    def testOpenSchemaFile(self):
        pass
    def testValidateSchemaFile(self):
        pass
    def testOpenMappingFile(self):
        pass
    def testValidateMappingFile(self):
        pass
    def testIdentifyMappedFields(self):
        pass
    def testFetchMappedFieldsValues(self):
        pass
    def testSaveAlignedDataFile(self):
        pass
    
    def testShowMessage(self):

        from libraries.tray import SysTrayRunner

        SysTrayRunner()
        global openInBrowser

        tornado.options.parse_command_line()
        app = Application()

        print ("\nStarting tornado web-server listening on {0}: {1}...".format( configs.application_configs["address"], configs.application_configs["port"] ))
        app.listen( address = configs.application_configs["address"], port = configs.application_configs["port"] )

        if openInBrowser:
            print ("\nhttp://{0}:{1}/ in a web browser".format( configs.application_configs["address"], configs.application_configs["port"] ))
            webbrowser.open("http://{0}:{1}/".format(configs.application_configs["address"], configs.application_configs["port"] ),0,True )

        tornado.ioloop.IOLoop.instance().start()

        self.assertIsNotNone( app )

    def testShowProgress(self):
        pass


def main():
    unittest.main()

if __name__ == '__main__':
    main()
