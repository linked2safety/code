#!/usr/bin/env python
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
#
#define dependencies
packages = (
				#{ "name" : "pywin32", "version" : "214" },
				{ "name" : "Cython" },
				{ "name" : "setuptools" },
				{ "name" : "lxml", "version" : "2.3" },
				{ "name" : "xlrd", "version" : "0.8" },
				{ "name" : "tornado", "version" : "2.4.1" },
				#{ "name" : "wxPython" },
			)

#install dependencies
import ez_setup
def install(package, version = None):
	if len(package) == 0:
		return
	if version!=None and len(version)>0:
		package = "%(p)s==%(v)s" % {"p" : package, "v" : version}
	ez_setup.main([package])
	
for p in packages:
	install( package = p["name"], version = p["version"] if "version" in p else None )