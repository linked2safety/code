# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import uuid
import os

class Attribute():
    def __init__(self):
        self.Id = uuid.UUID(bytes=os.urandom(16), version=1).hex
        self.Name = ""
        self.Namespace = ""
        self.DataType = ""
        self.Description = ""
        self.PrintValue = ""