# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
import unittest
import os

# Here's our "unit tests".

class MappingComponentTests(unittest.TestCase):
    def testOpenRawDataFile(self):
        basedir = os.path.dirname(__file__)
        settings = dict(
            application_dir = basedir,
            application_config_file = [os.path.join(basedir, "application.config.xml")],
            cookiesid = dict(
                name = 'clientsid',
                expires_days = 1,
            ),
            transformation = dict(
                internalCounterCollumns = ["internal_no"]
            )
        )
        self.failIf(settings is None)
        
    def testValidateRawDataFile(self):
        pass
    def testProcessRawDataFile(self):
        pass
    def testOpenSchemaFile(self):
        pass
    def testValidateSchemaFile(self):
        pass
    def testOpenMappingFile(self):
        pass
    def testSaveMappingFile(self):
        pass
    def testValidateMappingFile(self):
        pass
    def testIdentifyMappedFields(self):
        pass
    def testShowMessage(self):
        pass
    def testDisplayMappings(self):
        pass

def main():
    unittest.main()

if __name__ == '__main__':
    main()
