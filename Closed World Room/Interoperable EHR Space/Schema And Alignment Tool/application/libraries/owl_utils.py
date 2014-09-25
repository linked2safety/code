# -*- coding: utf-8 -*-
#
#This code is licensed under the GNU General Public License, version 3.0 (GPL-3.0)
#For details see: http://opensource.org/licenses/GPL-3.0
import os, re
from StringIO import StringIO
from lxml import etree
from sets import Set
from attributes import Attribute
import log
import configs

class OwlCategory():
    #begin
    def __init__(self, name, displayName = None ):
	
	self.SubCategories 			= dict()
	self.Attributes 			= dict()
	self.Namespace				= None
	self.IndexedCategoriesByNamespace 	= dict()
	self.Name = name
	self.DisplayName = displayName if displayName!= None and len(displayName)>0 else self.Name
	
    def AddSubCategory( self, subCategory, index = False ):
	#begin
	if subCategory.Name not in self.SubCategories:
	    self.SubCategories[subCategory.Name] = subCategory
	    subCategory.SetParentNamespace( self.Namespace )
	    if index:
		self.IndexSubCategory( subCategory )
	#end
    
    def GetSubCategory( self, subCategoryName = None, namespace = None ):
	#begin
	if subCategoryName!=None:
	    return self.SubCategories[subCategoryName] if subCategoryName in self.SubCategories  else None
	if namespace!=None:
	    return self.IndexedCategoriesByNamespace[namespace] if namespace in self.IndexedCategoriesByNamespace else None
	return None
	#end
    def IndexSubCategory(self, subCategory):
	#begin
	if subCategory!=None and subCategory.Namespace!=None and subCategory.Namespace not in self.IndexedCategoriesByNamespace:
	    self.IndexedCategoriesByNamespace[subCategory.Namespace] = subCategory
	#end
    
    def SetParentNamespace(self, parentNamespace):
	if parentNamespace!=None and len(parentNamespace)>0:
	    self.Namespace = parentNamespace + "." + self.Name
	else:
	    self.Namespace = self.Name
	
    def HasSubCategory(self, subCategory = None, subCategoryName = None):
	#begin
	if subCategory != None:
	    return subCategory.Name in self.SubCategories;
	
	if subCategoryName != None:
	    return subCategoryName in self.SubCategories;
	#end
    
    def AddOwlAttribute(self, owlAttribute):
	#begin
	self.Attributes[owlAttribute.Id] = owlAttribute
	#end
	
    def ListSubCategories( self ):
	#begin
	return [
	    (attr[1].Name, attr[1])
	    for attr in
	    sorted(
		self.SubCategories.iteritems(),
		lambda x, y : cmp(x[1].DisplayName.lower(), y[1].DisplayName.lower())
		)
	    ]
	#end
    
    def ListAttributes( self ):
	#begin
	return [
	    (attr[1].Name, attr[1])
				for attr in
				    sorted(
					    self.Attributes.iteritems(),
					    lambda x, y: cmp(x[1].PrintValue.lower(), y[1].PrintValue.lower())
				    )
	]
	#end
    
    #end of class

class OwlAttribute(Attribute):
    #begin
    """An attribute read from OWL file"""
    def __init__(self, fullName, type, comment = "", range="", namespace="", label = "", categoryPath = "", parentFullName = "" ):
	#begin
	Attribute.__init__(self)
	temp = fullName.split("#")
	if len(temp)>0:
	    tName = temp[-1:][0]
	    if len(temp)>1:
		self.Namespace = temp[0:-1][0] + "#"
	    else:
		self.Namespace = namespace
	else:
	    tName =  fullName
	    self.Namespace = namespace
	if len(namespace)>0:
	    self.Namespace = namespace
	self.FullName = fullName
	self.Name = tName
	self.DataType = type
	self.Description = comment
	self.PrintValue = label if len(label)>0 else tName.replace("_", " ") if tName else u"";
	self.Range = range
	self.ParentFullName = parentFullName
	#override namespace by application configs
	self.CategoryPath = (categoryPath if isinstance(categoryPath, (list)) else [categoryPath]) if len(categoryPath)>0 else ([configs.application_configs["owlNamespaceCategories"][self.Namespace] if self.Namespace in configs.application_configs["owlNamespaceCategories"] else  self.Namespace] if len(self.Namespace)>0 else [])
	#end
    #end

class OwlParser():
    """OWL file parser"""
    #medDRAPerser = re.compile( "(MedDRA\s*CODE:)?\s+(?P<name>[^/]*)\s+//\s+(?P<label>[^/]*)\s+?($|//)", re.IGNORECASE | re.UNICODE )
    medDRAPerser = re.compile( "(MedDRA\s*CODE:)?(?P<name>[^/]*)//(?P<label>[^/]*)(//||$)", re.IGNORECASE | re.UNICODE )
    
    def __init__(self):
	# define namespaces
	self.namespaces = configs.application_configs["owlNamespaces"]

    def extractInfo(self, e):
	subtree = etree.ElementTree(e)
	attrName = ''
	attrComment = ''
	attrType = ''
	attrLabel = ''
	categoryPath = ''
	subPropertyOf = ''
	temp =subtree.xpath('//owl:ObjectProperty/@rdf:about|//owl:DatatypeProperty/@rdf:about',namespaces = self.namespaces,)
	if temp:
	    attrName = temp[0]
	temp = subtree.xpath('//owl:ObjectProperty//rdfs:comment//text()|//owl:DatatypeProperty//rdfs:comment//text()',namespaces = self.namespaces)
	if temp:
	    attrComment = temp[0]
	temp = subtree.xpath('//owl:ObjectProperty//rdfs:label//text()|//owl:DatatypeProperty//rdfs:label//text()',namespaces = self.namespaces)
	if temp:
	    attrLabel = temp[0]
	temp =subtree.xpath('//owl:ObjectProperty//rdfs:range/@rdf:resource|//owl:DatatypeProperty//rdfs:range/@rdf:resource',namespaces = self.namespaces)
	if temp:
	    attrType = temp[0]
	    
	temp =subtree.xpath('//owl:ObjectProperty//rdfs:subPropertyOf/@rdf:resource|//owl:DatatypeProperty//rdfs:subPropertyOf/@rdf:resource',namespaces = self.namespaces)
	if temp:
	    subPropertyOf = temp[0]
	
	#If medDRA
	if configs.application_configs["useMedDRA"] == "true":
	    texts = subtree.xpath('//owl:ObjectProperty//obo:IAO_0000118//text()|//owl:DatatypeProperty//obo:IAO_0000118//text()',namespaces = self.namespaces)
	    if texts:
		#if owl subproperties should be ignored
		if len(subPropertyOf) == 0 or configs.application_configs["useMedDRAOwlSubproperties"] == "true":
		    attributes = []
		    for text in texts:
			results = [m.groupdict() for m in self.medDRAPerser.finditer(text)]
			if results:
			    mlist = [group["name"].strip() +": " + group["label"].strip() for group in results if "name" in group and "label" in group]
			    if len(mlist)>0:
				categoryPath = mlist
				
			attribute = OwlAttribute( attrName, attrType, comment = attrComment.replace("\n", "\n\t\t\t"), label = attrLabel, categoryPath = categoryPath, parentFullName = subPropertyOf )
			if not(attribute.Namespace in configs.application_configs["owlIgnoredNamespaces"]):
			    attributes.append( attribute )
		    return attributes
		return []
	#end of Medra
	if len(subPropertyOf) == 0 or configs.application_configs["useOwlSubproperties"] == "true":
	    #create attribute
	    attribute = OwlAttribute( attrName, attrType, comment = attrComment.replace("\n", "\n\t\t\t"), label = attrLabel, categoryPath = categoryPath, parentFullName = subPropertyOf )
	    if not(attribute.Namespace in configs.application_configs["owlIgnoredNamespaces"]):
		if configs.application_configs["useMedDRA"] == "true":
		    attribute.CategoryPath.insert(0, configs.application_configs['defaultMedDRAOwlCategoryName'] )
		if len(attribute.CategoryPath) == 0:
		    attribute.CategoryPath.append(configs.application_configs['defaultOwlCategoryName'])
		return [ attribute ]
	return []
	
    
    def AddAttribute(self, owlAttribute, rootOwlCategory):
	#get category for attribute
	if isinstance(owlAttribute.CategoryPath, (list)):
	    self.AddCategories(owlAttribute.CategoryPath, rootOwlCategory)
	    #owlAttribute.CategoryPath = ".".join(owlAttribute.CategoryPath)
	    
	category = rootOwlCategory.GetSubCategory( namespace = ".".join(owlAttribute.CategoryPath) )
	if category == None:
	    category = OwlCategory( owlAttribute.CategoryPath )
	    rootOwlCategory.AddSubCategory(category, index = True)
	#add attribute to category)
	category.AddOwlAttribute(owlAttribute)
	
	return owlAttribute
    
    def AddCategories(self, listNames, rootOwlCategory ):
	#all new categories should be added to/from root
	parentCategory = rootOwlCategory
	for name in listNames:
	    category=None
	    if parentCategory.HasSubCategory( subCategoryName = name ) == False:
		category = OwlCategory(name)
		parentCategory.AddSubCategory( category  )
		rootOwlCategory.IndexSubCategory(category)
	    else:
		category = parentCategory.GetSubCategory(subCategoryName = name)
	    
	    parentCategory = category
    
    def parse(self, filePath, rootOwlCategory):
	with open( filePath , 'r') as fh:
	    owlFile = fh.read()

	#owlFile = owlFile.replace("&clin;", "")
	#owlFile = owlFile.replace("&xsd;", "")

	tree = etree.parse(StringIO(owlFile))
	
	elements = tree.xpath('//rdf:RDF//owl:ObjectProperty|//rdf:RDF//owl:DatatypeProperty', namespaces = self.namespaces)
	
	attributes = []
	atributesIndexedByName = dict()
	attributesWithParent = []
	attributesWithNoParent = []
	for e in elements:
	    for attribute in self.extractInfo(e):
		attributes.append(attribute)
		atributesIndexedByName[attribute.FullName] = attribute
		if len(attribute.ParentFullName)>0:
		    attributesWithParent.append(attribute)
		else:
		    attributesWithNoParent.append(attribute)
	
	#build tree
	for attribute in attributesWithNoParent:
	    self.AddAttribute( attribute, rootOwlCategory )
	reset = True
	while reset:
	    reset = False
	    for attribute in attributesWithParent:
		if configs.application_configs["groupSubProperties"] == "true" or len(attribute.CategoryPath) == 0 :
		    if len(attribute.ParentFullName)>0:
			if attribute.ParentFullName in atributesIndexedByName:
			    parent = atributesIndexedByName[attribute.ParentFullName]
			    if len(parent.ParentFullName) == 0:
				attribute.CategoryPath = [x for x in parent.CategoryPath]
				if configs.application_configs["groupSubProperties"] == "true":
				    attribute.CategoryPath.append( parent.PrintValue )
				attribute.ParentFullName = ''
				self.AddAttribute( attribute, rootOwlCategory )
			    else:
				reset = True
			else:
			    attribute.ParentFullName = ''
			    self.AddAttribute( attribute, rootOwlCategory )
		else:
		    attribute.ParentFullName = ''
		    self.AddAttribute( attribute, rootOwlCategory )
	
	return attributes