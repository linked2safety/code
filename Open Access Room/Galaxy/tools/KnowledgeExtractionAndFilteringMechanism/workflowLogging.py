#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   workflowLogging.py - Creates a log file in an XML format for each experiment performed and 
#   stores it in the DB. After each tool in Galaxy is executed, its settings and results are
#   stored in the log file of the current experiment in the DB  
#   
#   Copyright 2014 - University of Cyprus - aristodimou.aristos@ucy.ac.cy
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
# 
#   You may download the source code and associated license at  https://github.com/linked2safety/code
#
# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 


import psycopg2
import sys, os
import xml.etree.ElementTree as ET
sys.path.append('/home/galaxy/galaxy-dist/lib')
from galaxy import util
from l2s import DBconnection
import insert_workflow

conn = None;

def main():

	userEmail = sys.argv[1]
	toolInputFile = sys.argv[2];
	toolOutputFile = sys.argv[3];
	toolXMLDescription = sys.argv[4];
	storeWorkflow = sys.argv[5];
	
	#-CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
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
	select_field = "filecontent, datasetfilenames, logfile_id";
	input_table = "logfile";
	where_field = "userid='" + str(userID) + "' and datasetfilenames ~ '.*" + toolInputFile + ".*'";
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
		UpdateLogFile(conn,cursor,["","",""],toolInputFile,toolOutputFile,XMLdescription,userID,3);
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
			
	newLogFileContent = ""
	if justInclude:
		newLogFileContent = UpdateLogFile(conn,cursor,records[recordIndx],toolInputFile,toolOutputFile,XMLdescription,userID,1);
	#Copy the XML content from the record up to the tool that outputted the input of the current tool and add the XML description of the tool
	elif found:
		#print records[recordIndx]
		newLogFileContent = UpdateLogFile(conn,cursor,records[recordIndx],toolInputFile,toolOutputFile,XMLdescription,userID,2);
	#Create a new XML log file in the table
	else:
		newLogFileContent = UpdateLogFile(conn,cursor,["","",""],toolInputFile,toolOutputFile,XMLdescription,userID,3);
		
	#Store the workflow in the DB
	if storeWorkflow == "1":
		insert_workflow.parse_and_insert(conn,cursor,newLogFileContent,userID)


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

#Update the LogFile table by inserting or updating its records
def UpdateLogFile(conn,cursor,record,inputDataset,newDataset,newContent,userID,insertType):
	
	#Update an existing record
	if insertType == 1:
		#Update the content of the record
		root = ET.fromstring(record[0])
		root2 = ET.fromstring(newContent)
		for elem in root2:
			root.append(elem)
		root.attrib['description_of_workflow'] = root.attrib['description_of_workflow'] + "," + root2[0].attrib['algorithm_name']
		updatedContent = ET.tostring(root)
		updatedFilenames = record[1]+","+newDataset;
		
		#UPDATE THE DB
		sql_string = "UPDATE logfile SET datasetfilenames=%s,filecontent=%s,dateused=%s WHERE logfile_id=%s";
		cursor.execute(sql_string,(updatedFilenames,updatedContent,"now()",record[2]));
		conn.commit();	
		return updatedContent;
		
	#Insert new record using info from an existing one
	elif insertType == 2:
		fIndx = 0;
		filenames = record[1].split(",");
		for filename in filenames:
			fIndx = fIndx + 1;
			if filename == inputDataset:
				break;
			
		root = ET.fromstring(record[0])
		del root[fIndx:];

		root2 = ET.fromstring(newContent)
		for elem in root2:
			root.append(elem)
		
		workflow = root.attrib['description_of_workflow'].split(",");
		filenames = record[1].split(",");

		del filenames[fIndx:]
		del workflow[fIndx:]

		updatedWorkflow = workflow[0];
		updatedFilenames = filenames[0];
		for i in range(1,len(filenames)):
			updatedFilenames = updatedFilenames + "," + filenames[i];
			
		for i in range(1,len(workflow)):
			updatedWorkflow = updatedWorkflow + "," + workflow[i];
		
		updatedFilenames = updatedFilenames + "," + newDataset;
		root.attrib['description_of_workflow'] = updatedWorkflow + "," + root2[0].attrib['algorithm_name']
		updatedContent = ET.tostring(root)
		
		#INSERT IN DB
		sql_string = "INSERT INTO logfile (datasetfilenames,filecontent,userid,dateused) VALUES (%s,%s,%s,%s);";
		cursor.execute(sql_string,(updatedFilenames,updatedContent,userID,"now()"));
		conn.commit();
		return updatedContent		
		
	#Insert a new record
	else:
		updatedContent = newContent;
		updatedFilenames = newDataset;
		
		#INSERT IN DB
		sql_string = "INSERT INTO logfile (datasetfilenames,filecontent,userid) VALUES (%s,%s,%s);";
		cursor.execute(sql_string,(updatedFilenames,updatedContent,userID));
		conn.commit();	
		return updatedContent

if __name__ == "__main__":
	main()

if conn:
	cursor.close();
	conn.close();
