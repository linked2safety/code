"""
CSV datacube datatype

"""
import pkg_resources
pkg_resources.require( "bx-python" )

import logging
import data
from galaxy import util
from cgi import escape
from galaxy.datatypes import metadata
from galaxy.datatypes.metadata import MetadataElement
import galaxy_utils.sequence.vcf
from sniff import *
from galaxy.util.json import to_json_string
from l2s import DBconnection

log = logging.getLogger(__name__)

class CSVdatacube( data.Text ):
    """Comma delimited data"""
    CHUNK_SIZE = 50000

    """Add metadata elements"""
    MetadataElement( name="comment_lines", default=0, desc="Number of comment lines", readonly=False, optional=True, no_value=0 )
    MetadataElement( name="columns", default=0, desc="Number of columns", readonly=True, visible=False, no_value=0 )
    MetadataElement( name="column_types", default=[], desc="Column types", param=metadata.ColumnTypesParameter, readonly=True, visible=False, no_value=[] )
    MetadataElement( name="column_names", default=[], desc="Column names", readonly=True, visible=False, optional=True, no_value=[] )
	#MetadataElement( name="column_names", default=[], desc="Column names", param=metadata.ColumnTypesParameter, readonly=True, visible=False, no_value=[] )

    def init_meta( self, dataset, copy_from=None ):
        data.Text.init_meta( self, dataset, copy_from=copy_from )
    def set_meta( self, dataset, overwrite = True, skip = None, max_data_lines = 100, max_guess_type_data_lines = None, **kwd ):
        """
        Tries to determine the number of columns as well as those columns
        that contain numerical values in the dataset.  A skip parameter is
        used because various tabular data types reuse this function, and
        their data type classes are responsible to determine how many invalid
        comment lines should be skipped. Using None for skip will cause skip
        to be zero, but the first line will be processed as a header. A
        max_data_lines parameter is used because various tabular data types
        reuse this function, and their data type classes are responsible to
        determine how many data lines should be processed to ensure that the
        non-optional metadata parameters are properly set; if used, optional
        metadata parameters will be set to None, unless the entire file has
        already been read. Using None (default) for max_data_lines will
        process all data lines.

        Items of interest:
        1. We treat 'overwrite' as always True (we always want to set tabular metadata when called).
        2. If a tabular file has no data, it will have one column of type 'str'.
        3. We used to check only the first 100 lines when setting metadata and this class's
           set_peek() method read the entire file to determine the number of lines in the file.
           Since metadata can now be processed on cluster nodes, we've merged the line count portion
           of the set_peek() processing here, and we now check the entire contents of the file.
        """
        # Store original skip value to check with later
        requested_skip = skip
        if skip is None:
            skip = 0
        column_type_set_order = [ 'int', 'float', 'list', 'str'  ] #Order to set column types in
        default_column_type = column_type_set_order[-1] # Default column type is lowest in list
        column_type_compare_order = list( column_type_set_order ) #Order to compare column types
        column_type_compare_order.reverse()
        def type_overrules_type( column_type1, column_type2 ):
            if column_type1 is None or column_type1 == column_type2:
                return False
            if column_type2 is None:
                return True
            for column_type in column_type_compare_order:
                if column_type1 == column_type:
                    return True
                if column_type2 == column_type:
                    return False
            #neither column type was found in our ordered list, this cannot happen
            raise "Tried to compare unknown column types"
        def is_int( column_text ):
            try:
                int( column_text )
                return True
            except:
                return False
        def is_float( column_text ):
            try:
                float( column_text )
                return True
            except:
                if column_text.strip().lower() == 'na':
                    return True #na is special cased to be a float
                return False
        def is_list( column_text ):
            return "," in column_text
        def is_str( column_text ):
            #anything, except an empty string, is True
            if column_text == "":
                return False
            return True
        is_column_type = {} #Dict to store column type string to checking function
        for column_type in column_type_set_order:
            is_column_type[column_type] = locals()[ "is_%s" % ( column_type ) ]
        def guess_column_type( column_text ):
            for column_type in column_type_set_order:
                if is_column_type[column_type]( column_text ):
                    return column_type
            return None
        data_lines = 0
        comment_lines = 0
        column_types = []
        first_line_column_types = [default_column_type] # default value is one column of type str
        if dataset.has_data():
            #NOTE: if skip > num_check_lines, we won't detect any metadata, and will use default
            dataset_fh = open( dataset.file_name )
            i = 0
            while True:
                line = dataset_fh.readline()
                if i == 0:
					dataset.metadata.column_names = line.split( ',' )
                if not line: break
                line = line.rstrip( '\r\n' )
                if i < skip or not line or line.startswith( '#' ):
                    # We'll call blank lines comments
                    comment_lines += 1
                else:
                    data_lines += 1
                    if max_guess_type_data_lines is None or data_lines <= max_guess_type_data_lines:
                        fields = line.split( ',' )
                        for field_count, field in enumerate( fields ):
                            if field_count >= len( column_types ): #found a previously unknown column, we append None
                                column_types.append( None )
                            column_type = guess_column_type( field )
                            if type_overrules_type( column_type, column_types[field_count] ):
                                column_types[field_count] = column_type
                    if i == 0 and requested_skip is None:
                        # This is our first line, people seem to like to upload files that have a header line, but do not
                        # start with '#' (i.e. all column types would then most likely be detected as str).  We will assume
                        # that the first line is always a header (this was previous behavior - it was always skipped).  When
                        # the requested skip is None, we only use the data from the first line if we have no other data for
                        # a column.  This is far from perfect, as
                        # 1,2,3	1.1	2.2	qwerty
                        # 0	0		1,2,3
                        # will be detected as
                        # "column_types": ["int", "int", "float", "list"]
                        # instead of
                        # "column_types": ["list", "float", "float", "str"]  *** would seem to be the 'Truth' by manual
                        # observation that the first line should be included as data.  The old method would have detected as
                        # "column_types": ["int", "int", "str", "list"]
                        first_line_column_types = column_types
                        column_types = [ None for col in first_line_column_types ]
                if max_data_lines is not None and data_lines >= max_data_lines:
                    if dataset_fh.tell() != dataset.get_size():
                        data_lines = None #Clear optional data_lines metadata value
                        comment_lines = None #Clear optional comment_lines metadata value; additional comment lines could appear below this point
                    break
                i += 1
            dataset_fh.close()

        #we error on the larger number of columns
        #first we pad our column_types by using data from first line
        if len( first_line_column_types ) > len( column_types ):
            for column_type in first_line_column_types[len( column_types ):]:
                column_types.append( column_type )
        #Now we fill any unknown (None) column_types with data from first line
        for i in range( len( column_types ) ):
            if column_types[i] is None:
                if len( first_line_column_types ) <= i or first_line_column_types[i] is None:
                    column_types[i] = default_column_type
                else:
                    column_types[i] = first_line_column_types[i]
        # Set the discovered metadata values for the dataset
        dataset.metadata.data_lines = data_lines
        dataset.metadata.comment_lines = comment_lines
        dataset.metadata.column_types = column_types
        dataset.metadata.columns = len( column_types ) - 1
		
    def make_html_table( self, dataset, **kwargs ):
        """Create HTML table, used for displaying peek"""
        out = ['<table cellspacing="0" cellpadding="3">']
        try:
            out.append( self.make_html_peek_header( dataset, **kwargs ) )
            out.append( self.make_html_peek_rows( dataset, **kwargs ) )
            out.append( '</table>' )
            out = "".join( out )
        except Exception, exc:
            out = "Can't create peek %s" % str( exc )
        return out

    def make_html_peek_header( self, dataset, skipchars=None, column_names=None, column_number_format='%s', column_parameter_alias=None, **kwargs ):
        if skipchars is None:
            skipchars = []
        if column_names is None:
            column_names = []
        if column_parameter_alias is None:
            column_parameter_alias = {}
        out = []
        try:
            if not column_names and dataset.metadata.column_names:
                column_names = dataset.metadata.column_names

            column_headers = [None] * dataset.metadata.columns

            # fill in empty headers with data from column_names
            for i in range( min( dataset.metadata.columns, len( column_names ) ) ):
                if column_headers[i] is None and column_names[i] is not None:
                    column_headers[i] = column_names[i]

            # fill in empty headers from ColumnParameters set in the metadata
            for name, spec in dataset.metadata.spec.items():
                if isinstance( spec.param, metadata.ColumnParameter ):
                    try:
                        i = int( getattr( dataset.metadata, name ) ) - 1
                    except:
                        i = -1
                    if 0 <= i < dataset.metadata.columns and column_headers[i] is None:
                        column_headers[i] = column_parameter_alias.get(name, name)

            out.append( '<tr>' )
            for i, header in enumerate( column_headers ):
                out.append( '<th>' )
                if header is None:
                    out.append( column_number_format % str( i + 1 ) )
                else:
                    out.append( '%s.%s' % ( str( i + 1 ), escape( header ) ) )
                out.append( '</th>' )
            out.append( '</tr>' )
        except Exception, exc:
            raise Exception, "Can't create peek header %s" % str( exc )
        return "".join( out )

    def make_html_peek_rows( self, dataset, skipchars=None, **kwargs ):
        if skipchars is None:
            skipchars = []
        out = []
        try:
            if not dataset.peek:
                dataset.set_peek()
            lineNum = 0
            for line in dataset.peek.splitlines():
                if line.startswith( tuple( skipchars ) ):
                    out.append( '<tr><td colspan="100%%">%s</td></tr>' % escape( line ) )
                elif line:
                    elems = line.split( ',' )
                    
                    out.append( '<tr>' )
                    
                    if lineNum !=0:    
                        combinationCount = int(elems[-1])
                        combinationCount = combinationCount/10
                        combinationMinCount = combinationCount*10
                        combinationMaxCount = combinationMinCount + 10
                        elems[-1] = str(combinationMinCount)+ " - " + str(combinationMaxCount)
                    lineNum = lineNum + 1    
                        
                    for elem in elems:
                        out.append( '<td>%s</td>' % escape( elem ) )
                    out.append( '</tr>' )
        except Exception, exc:
            raise Exception, "Can't create peek rows %s" % str( exc )
        return "".join( out )

    def get_chunk(self, trans, dataset, chunk):
        ck_index = int(chunk)
        f = open(dataset.file_name)
        f.seek(ck_index * self.CHUNK_SIZE)
        # If we aren't at the start of the file, seek to next newline.  Do this better eventually.
        if f.tell() != 0:
            cursor = f.read(1)
            while cursor and cursor != '\n':
                cursor = f.read(1)
        ck_data = f.read(self.CHUNK_SIZE)
        cursor = f.read(1)
        while cursor and ck_data[-1] != '\n':
            ck_data += cursor
            cursor = f.read(1)
        return to_json_string({'ck_data': ck_data, 'ck_index': ck_index+1})

	#L2S file saving without exact data cube counts
    def _serve_raw(self, trans, dataset, to_ext):
		trans.response.headers['Content-Length'] = int( os.stat( dataset.file_name ).st_size )
		valid_chars = '.,^_-()[]0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
		fname = ''.join(c in valid_chars and c or '_' for c in dataset.name)[0:150]
		trans.response.set_content_type( "application/octet-stream" ) #force octet-stream so Safari doesn't append mime extensions to filename
		trans.response.headers["Content-Disposition"] = 'attachment; filename="Galaxy%s-[%s].%s"' % (dataset.hid, fname, to_ext)
		
		datacubeContent = "No data retrieved"    
		if dataset.has_data():
			dataset_fh = open( dataset.file_name )
			i=0;
			for line in dataset_fh:
				line = line.rstrip( '\r\n' )
				if i == 0:
					datacubeContent = line
				else:
					lineContent = line.split(",")
					combinationCount = int(lineContent[-1])
					combinationCount = combinationCount/10
					combinationMinCount = combinationCount*10
					combinationMaxCount = combinationMinCount + 10
					lineContent[-1] = str(combinationMinCount)+ " - " + str(combinationMaxCount)
					datacubeContent = datacubeContent + "\n" + ",".join(lineContent)
				i=i+1
			dataset_fh.close()    
		return datacubeContent;

	#L2S file display without exact data cube content
    def display_data(self, trans, dataset, preview=False, filename=None, to_ext=None, chunk=None):
        if to_ext:
            return self._serve_raw(trans, dataset, to_ext)
        #if chunk:
        #    return self.get_chunk(trans, dataset, chunk)
        else:
            column_names = 'null'
            if dataset.metadata.column_names:
                column_names = dataset.metadata.column_names
            elif hasattr(dataset.datatype, 'column_names'):
                column_names = dataset.datatype.column_names
            
            catMeanings = getVariableMeanings(column_names[:-1])
            
            padding = getMaxPadding(catMeanings)
            
            
            datacubeContent = "No data retrieved"    
            if dataset.has_data():
				dataset_fh = open( dataset.file_name )
				i=0;
				for line in dataset_fh:
					line = line.rstrip( '\r\n' )
					if i == 0:
						lineContent = line.split(",")
						for dimIndx,dimVal in enumerate(lineContent):
							if dimIndx != len(lineContent)-1:
								lineContent[dimIndx] = dimVal.ljust(padding[column_names[dimIndx]],' ');
						datacubeContent = "<pre>" + "   ".join(lineContent) + "</pre>"
					else:
						lineContent = line.split(",")
						for dimIndx,dimVal in enumerate(lineContent):
							if dimIndx != len(lineContent)-1:
								lineContent[dimIndx] = catMeanings[column_names[dimIndx]][dimVal].ljust(padding[column_names[dimIndx]],' ')
							else:
								combinationCount = int(lineContent[-1])
								combinationCount = combinationCount/10
								combinationMinCount = combinationCount*10
								combinationMaxCount = combinationMinCount + 10
								lineContent[-1] = str(combinationMinCount)+ " - " + str(combinationMaxCount)
								
						datacubeContent = datacubeContent +  "<pre>" + "   ".join(lineContent) + "</pre>"
					i=i+1
				dataset_fh.close()    
            return datacubeContent;


    def set_peek( self, dataset, line_count=None, is_multi_byte=False):
        super(CSVdatacube, self).set_peek( dataset, line_count=line_count, is_multi_byte=is_multi_byte)
        if dataset.metadata.comment_lines:
            dataset.blurb = "%s, %s comments" % ( dataset.blurb, util.commaify( str( dataset.metadata.comment_lines ) ) )
    def display_peek( self, dataset ):
        """Returns formatted html of peek"""
        return self.make_html_table( dataset )
    def displayable( self, dataset ):
        try:
            return dataset.has_data() \
                and dataset.state == dataset.states.OK \
                and dataset.metadata.columns > 0 \
                and dataset.metadata.data_lines != 0
        except:
            return False
    def as_gbrowse_display_file( self, dataset, **kwd ):
        return open( dataset.file_name )
    def as_ucsc_display_file( self, dataset, **kwd ):
        return open( dataset.file_name )      

class CSV( CSVdatacube ):
	file_ext='csv'
	allow_datatype_change = False

	def sniff( self, filename ):
		f = open( filename, "r" )
		firstline = f.readline()
		secondline = f.readline()
		f.close()

		if "," in firstline and "," in secondline:
			return True
		else:
			return False


#L2S FUNCTIONS FOR GETTING THE MEANINGS OF THE CATEGORICAL VALUES OF THE VARIABLES
#Select an entry from a table in the database
def SelectFromDB(cursor, select_field, input_table, where_field):		
	# execute the Query
	if where_field == None:
		cursor.execute("SELECT " + select_field + " FROM " + input_table)
	else:
		cursor.execute("SELECT " + select_field + " FROM " + input_table + " WHERE " + where_field)
	
	# retrieve the records from the database
	records = cursor.fetchall()

	return records



#Return the categories meanings of the variables
def getVariableMeanings(varNames):	
	
	varNamesStr = "','".join(varNames);

	#-CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#GET THE MEANINGS OF THE VALUES OF THE VARIABLES OF INTEREST
	#Define the parameters
	select_field = "name,values,type,info";
	input_table = "categoriesmeaning";
	where_field = "name IN ('" + varNamesStr + "')";
	#Execute the query
	varMeanings = SelectFromDB(cursor, select_field, input_table, where_field);

	varMeaningsDic = {}
	for varname in varNames:
		varMeaningsDic[varname] = {}

	#Trim any whitespaces at the end of each field and remove the ' characters
	trimmedVarMeanings = [];
	for i,record in enumerate(varMeanings):
		meanings = record[1]
		varName = record[0].rstrip();
		varName = varName.replace("'","");
		meanings = meanings.rstrip();
		meanings = meanings.replace("'","");
		meanings = meanings.split(",");
		for meaning in meanings:
			meaning = meaning.split(":");
			varMeaningsDic[varName][meaning[0]] = meaning[1] 

	return varMeaningsDic

#Calculate the max length for each column
def getMaxPadding(catMeanings):
	varNames = catMeanings.keys();
	maxPadding = {}
	for varName in varNames:
		maxPadding[varName] = len(varName)
		for value in catMeanings[varName].values():
			if len(value) > maxPadding[varName]:
				maxPadding[varName] = len(value)
	return maxPadding

