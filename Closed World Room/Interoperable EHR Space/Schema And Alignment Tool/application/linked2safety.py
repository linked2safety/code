# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import sys

if "-h" in sys.argv or "-m" in sys.argv or "-o" in sys.argv or "-r" in sys.argv:
    import libraries.transform
    libraries.transform.main(sys.argv[1:])
else:
    sys.argv.append("open in browser")
    sys.argv.append("no debug")
    import webstart
    webstart.main()
