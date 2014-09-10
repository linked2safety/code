# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
import os
import xlrd
from attributes import Attribute

class XlsAttribute( Attribute ):
    """An attribute read from RAW file"""
    def __init__(self, sheetname, columnname, isPrimaryKey = False):
        Attribute.__init__(self)
        self.IsPrimaryKey = isPrimaryKey
        self.Name = u"{0}".format(columnname)
        self.Namespace = u"{0}".format(sheetname)
        self.PrintValue = (self.Namespace + u"." if self.Namespace else u'') + (self.Name if self.Name else u'').replace("_", " ");
        self.Category = self.Namespace
                 
class XlsParser():
    """XLS file parser"""
    
    #def __init__(self):        
        
    def parse(self, xlsContent, xlsFilePath = None):    
        wb = None
        if xlsFilePath:
            wb = xlrd.open_workbook( filename = xlsFilePath )
        else:
            wb = xlrd.open_workbook( file_contents = xlsContent )
        
        attributesList = []
            
        # get each sheet and ints columns
        for sheetName in wb.sheet_names():
            ws = wb.sheet_by_name(sheetName)
            for col in range(ws.ncols):
                cellValue = ws.cell_value(0, col)
                if cellValue != None:
                    attributesList.append(XlsAttribute(sheetName, cellValue))
                
        #attributesList = list(set(attributesList))    # duplicates removal        
        #attributesList.sort()

        # return a typed list of objects
        return attributesList
        