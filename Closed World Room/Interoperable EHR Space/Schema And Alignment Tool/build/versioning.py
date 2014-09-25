import os
import re
import codecs
from datetime import datetime

curdir = os.path.realpath(os.path.join(os.path.dirname(__file__), '..'))
appdir = os.path.realpath(os.path.join(curdir, 'application'))

def UpdateVersion( ):
	fileLocation = os.path.join(appdir, 'application.config.xml')
	dt = datetime.today()
	version = "v1.1.%(year)s.%(month)s%(day)s" % { "year" : dt.year, "month" : dt.month, "day" : dt.day if dt.day>9 else "0" + str(dt.day) }
	content = ''
	with codecs.open( fileLocation, encoding='utf-8', mode = 'r') as h:
		content = re.sub(
		    u"<config\s*name=[\"']version[\"']\s*value=[\"'][^\"']*[\"']\s*/>",
		    u"<config name=\"version\" value=\"%s\" />" % version,
		    h.read(),
		    flags = re.UNICODE
		   )
	with codecs.open(fileLocation, encoding='utf-8', mode = 'w') as h:
		h.write(content)
