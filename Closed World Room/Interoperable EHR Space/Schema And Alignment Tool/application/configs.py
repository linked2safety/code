# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU Lesser General Public License, version 3.0 (LGPL-3.0)
#For details see: http://opensource.org/licenses/LGPL-3.0
import os
import re
import sys
import glob
import codecs
from lxml import etree
import types
from libraries.owl_utils import OwlCategory


if getattr(sys, 'frozen', None):
    basedir = sys._MEIPASS
else:
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
    ),
    cookie_secret="0n0w/zKMRD2100TGE26AqWO0wP3aj0lauxsV7bUw5tM=",
    template_path = os.path.join(basedir, "templates"),
    static_path = os.path.join(basedir, "static"),
    temp_path = os.path.join(basedir, "temp"),
    log_path = os.path.join(basedir, "log"),
    resources = dict(
        path = os.path.join(basedir, "resources"),
        owls_path = os.path.join(basedir, "resources", "owl"),
        xsd = dict(
                mappingFileValidateSchemaFile = os.path.join("xsd", "MappingFile.xsd")
            )
    ),
    xsrf_cookies=True,
    debug = True,
    autoescape="xhtml_escape",
)

if os.path.exists(settings["temp_path"]) == False:
    os.mkdir(settings["temp_path"],755)
if os.path.exists(settings["log_path"]) == False:
    os.mkdir(settings["log_path"],755)

if "no debug" in sys.argv:
    settings["debug"] = False

#the general application configs
application_configs = dict(
	version = "v1.0",
	application_name = "Linked2Safety",
	application_icon = os.path.join(basedir, "static", "favicon.ico"),
	port = 8888,
	address = "127.0.0.1",
	browseFileOn = "client",
	providerName = None,
	mappingDescription = None,
	useMedDRA = "false",
	rdfIncludeEmptyAttributes = "true",
	owlNamespaces = dict(),
	postTransformationConsoleCommands = [],
	owlAttributesFiles = [],
	#Owl categories (simple name strings) that are read from config xml file
	owlCategories = dict( ),
	#Owl categories of type OwlCategories that are generated from owlCategories
	owlNamespaceCategories = dict(),
	#default category name
	defaultOwlCategoryName = "«Other categories»",
	defaultMedDRAOwlCategoryName = "«Other categories»",
	#localization
	L10N = dict(),
	#Owl namespaces that should be ignored
	owlIgnoredNamespaces = dict(),
	ignoreOwlSubproperties = "false",
)

def GetVersion():
    return application_configs['version']

def saveMedDRA( value ):
    updateSettings("useMedDRA", "true" if (value == "true" or value == True) else "false")
    
def updateSettings(name, value):
    application_configs[name] = value
    content = ''
    with codecs.open(settings["application_config_file"][0], encoding='utf-8', mode = 'r') as h:
	content = re.sub(
		    u"<config\s*name=[\"']{0}[\"']\s*value=[\"'][^\"']*[\"']\s*/>".format(name),
		    u"<config name=\"{0}\" value=\"{1}\" />".format(name, value.replace("\\", "\\\\")),
		    h.read(),
		    flags = re.UNICODE
		   )
    with codecs.open(settings["application_config_file"][0], encoding='utf-8', mode = 'w') as h:
	h.write(content)
    
def L10N(key):
    return application_configs["L10N"][key] if key in application_configs["L10N"] else key

for file in glob.glob(os.path.join(basedir, "application.*.config.xml")):
	if not(file in settings["application_config_file"]):
		settings["application_config_file"].append(file)

for file in settings["application_config_file"]:
	if os.path.exists(file):
		doc = etree.parse(file)
		conf = doc.xpath("/configs/config")
		if conf!=None:
			for c in conf:
				name = c.get( "name", None )
				if name!=None:
					
					
					items = c.xpath("item")
					if items or (
									name in  application_configs and    (
																			type(application_configs[name]) == types.DictionaryType or
																			type(application_configs[name]) == types.TupleType or
																			type(application_configs[name]) == types.ListType
																		)
								):
						if name not in application_configs:
							application_configs[name] = {}
						for item in items:
							iname = item.get("name", None)
							temp = item.get( "value", None )
							value = temp if temp != None else item.text
							if type(application_configs[name]) == types.DictionaryType or type(application_configs[name]) == types.TupleType:
								application_configs[name][iname] = value
							elif type(application_configs[name]) == types.ListType:
								application_configs[name].append( value )
					else:
						value = c.get( "value", None )
						text = c.text
						application_configs[name] = value if value != None else text

for k,v in application_configs['owlNamespaces'].iteritems():
    if k in application_configs['owlCategories']:
        application_configs['owlNamespaceCategories'][v] = application_configs['owlCategories'][k]

application_configs["defaultOwlCategoryName"] = L10N("OtherCategories")
application_configs["defaultMedDRAOwlCategoryName"] = L10N("MedDRAOtherCategories")