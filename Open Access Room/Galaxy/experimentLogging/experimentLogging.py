# experimentLogging.py 
# Creates a log file in an XML format for each experiment performed.
# After each tool in Galaxy is executed, its settings and results are
# stored in the log file of the current experiment
# Author:   Aristos Aristodimou (aris.aristodimou@gmail.com)

import psycopg2
import sys, os
import xml.etree.ElementTree as ET

conn = None;

def main():
	
	HOST = '127.0.0.1';
	DATABASE = 'galaxy_prod';
	USERNAME = 'galaxy';
	PASSWORD = 'password';

	userEmail = sys.argv[1]
	toolInputFile = sys.argv[2];
	toolOutputFile = sys.argv[3];
	toolXMLDescription = sys.argv[4];
	
	#CONNECT TO THE DATABASE
	#The connection string
	conn_string = ("host="+HOST +" dbname="+DATABASE +" user="+USERNAME +" password="+PASSWORD)
	#get a connection, if a connection cannot be made an exception will be raised here
	conn = psycopg2.connect(conn_string)
	#cursor is used to perform queries
	cursor = conn.cursor()

	#GET THE LINKED2SAFETY ID OF THE USER
	#Define the parameters
	select_field = "userid";
	input_table = "users";
	where_field = "email_address='" +userEmail + "'";
	#Execute the query
	userID = SelectFromDB(cursor, select_field, input_table, where_field);

	if len(userID) == 0:
		message = "You are not a registered user.\n Please Register before using this tool.\n"
		sys.stderr.write("error: %s\n" % message)
		sys.exit(-1)

	userID = userID[0][0]

	#GET THE CURRENT LOG FILES OF THE USER THAT HAVE THE CURRENT INPUT FILE IN THEM FROM THE DATABASE
	#Define the parameters
	select_field = "filecontent, remainingdatasetfilenames, datasetfilenames, l2sxml_id";
	input_table = "templ2sxml";
	where_field = "userid='" + str(userID) + "' and remainingdatasetfilenames ~ '.*" + toolInputFile + ".*'";
	#Execute the query
	records = SelectFromDB(cursor, select_field, input_table, where_field)

	#Read the XML log file of the tool
	xmlFile = open(toolXMLDescription,'r');
	XMLdescription = xmlFile.read();
	
	#If there is no XML content then there is no need to insert anything in the DB
	#This is possible in case a tool did not finish its execution 
	if XMLdescription == "":
		sys.exit(-2)

	if len(records)== 0:
		UpdateTempL2SXML(conn,cursor,["","","",""],toolInputFile,toolOutputFile,XMLdescription,userID,3);
		sys.exit(1)
		
	#CHECK IF A NEW LOG WILL BE CREATED OR IF AN EXISTING ONE WILL BE UPDATED
	found = False;
	justInclude = False;
	rIndx = 0;
	recordIndx=0;
	#Check if the input file of the tool is listed in an XML log file already
	for record in records:
		fIndx = 0;
		filenames = record[1].split(",");
		for filename in filenames:
			fIndx = fIndx + 1;
			if filename == toolInputFile:
				found = True;
				recordIndx = rIndx;
				if fIndx == len(filenames):
					justInclude = True;
					break;
		if justInclude:
			break;
		rIndx = rIndx + 1;
			
	if justInclude:
		UpdateTempL2SXML(conn,cursor,records[recordIndx],toolInputFile,toolOutputFile,XMLdescription,userID,1);
	#Copy the XML content from the record up to the tool that outputted the input of the current tool and add the XML description of the tool
	elif found:
		#print records[recordIndx]
		UpdateTempL2SXML(conn,cursor,records[recordIndx],toolInputFile,toolOutputFile,XMLdescription,userID,2);
	#Create a new XML log file in the table
	else:
		UpdateTempL2SXML(conn,cursor,["","","",""],toolInputFile,toolOutputFile,XMLdescription,userID,3);
		

	


##########	FUNCTIONS	###################

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

#Update the tempL2SXML table by inserting or updating its records
def UpdateTempL2SXML(conn,cursor,record,inputDataset,newDataset,newContent,userID,insertType):
	
	#Update an existing record
	if insertType == 1:
		#Update the content of the record
		root = ET.fromstring(record[0])
		root2 = ET.fromstring(newContent)
		for elem in root2:
			root.append(elem)
		root.attrib['workflows'] = root.attrib['workflows'] + "," + root2[0].attrib['algorithm_name']
		updatedContent = ET.tostring(root)
		updatedFilenames = record[2]+","+newDataset;
		updatedRemainingFilenames = record[1]+","+newDataset;
		
		#UPDATE THE DB
		sql_string = "UPDATE templ2sxml SET datasetfilenames=%s,remainingdatasetfilenames=%s,filecontent=%s WHERE l2sxml_id=%s";
		cursor.execute(sql_string,(updatedFilenames,updatedRemainingFilenames,updatedContent,record[3]));
		conn.commit();	
		
		
	#Insert new record using info from an existing one
	elif insertType == 2:
		fIndx = 0;
		filenames = record[2].split(",");
		for filename in filenames:
			fIndx = fIndx + 1;
			if filename == inputDataset:
				break;
			
		root = ET.fromstring(record[0])
		del root[fIndx:];

		root2 = ET.fromstring(newContent)
		for elem in root2:
			root.append(elem)
		
		workflow = root.attrib['workflows'].split(",");
		filenames = record[2].split(",");
		remainingFilenames = record[1].split(",");

		del filenames[fIndx:]
		del workflow[fIndx:]

		fIndx = 0;
		for filename in remainingFilenames:
			fIndx = fIndx + 1;
			if filename == inputDataset:
				break;	
		del remainingFilenames[fIndx:];

		
		updatedWorkflow = workflow[0];
		updatedRemainingFilenames = remainingFilenames[0];
		updatedFilenames = filenames[0];
		for i in range(1,len(filenames)):
			updatedFilenames = updatedFilenames + "," + filenames[i];
			
		for i in range(1,len(workflow)):
			updatedWorkflow = updatedWorkflow + "," + workflow[i];
			
		for i in range(1,len(remainingFilenames)):
			updatedRemainingFilenames = updatedRemainingFilenames + "," + remainingFilenames[i];
		
		updatedFilenames = updatedFilenames + "," + newDataset;
		root.attrib['workflows'] = updatedWorkflow + "," + root2[0].attrib['algorithm_name']
		updatedContent = ET.tostring(root)
		updatedRemainingFilenames = updatedRemainingFilenames + "," + newDataset;
		
		#INSERT IN DB
		sql_string = "INSERT INTO templ2sxml (datasetfilenames,remainingdatasetfilenames,filecontent,userid) VALUES (%s,%s,%s,%s);";
		cursor.execute(sql_string,(updatedFilenames,updatedRemainingFilenames,updatedContent,userID));
		conn.commit();		
		
	#Insert a new record
	else:
		updatedContent = newContent;
		updatedFilenames = newDataset;
		updatedRemainingFilenames = newDataset;
		
		#INSERT IN DB
		sql_string = "INSERT INTO templ2sxml (datasetfilenames,remainingdatasetfilenames,filecontent,userid) VALUES (%s,%s,%s,%s);";
		cursor.execute(sql_string,(updatedFilenames,updatedRemainingFilenames,updatedContent,userID));
		conn.commit();	

if __name__ == "__main__":
	main()

if conn:
	cursor.close();
	conn.close();
