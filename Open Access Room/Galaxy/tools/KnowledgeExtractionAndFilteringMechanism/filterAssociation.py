#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   filterAssociation.py - Filters out the associations that match the association that the user defined
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
import sys
from galaxy import util
from l2s import DBconnection

conn = None;

def main():
	
	association = util.restore_text(sys.argv[1])
	reasoning = util.restore_text(sys.argv[2]);
	userEmail = sys.argv[3];
	
	#CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#GET THE LINKED2SAFETY ID OF THE USER AND IF THE USER IS ALLOWED TO USE THIS TOOL
	#Define the parameters
	select_field = "userid, is_expert";
	input_table = "users";
	where_field = "email_address='" +userEmail + "'";
	#Execute the query
	res = SelectFromDB(cursor, select_field, input_table, where_field);

	if len(res) == 0:
		message = "You are not a registered user.\nPlease Register before using this tool."
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)

	userID = res[0][0]
	isExpert = res[0][1]
	
	if isExpert != True:
		message = "You are not authorised for using this tool.\nPlease contact the system administrator for requesting access to this tool."
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)

	#Check if the association provided is valid
	if not isValidAssociation(association):
		message = "The association provided is not valid.\nThe association should be in the form: a & b => c"
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)		

	#Remove any whitespaces from the association
	association = association.replace(" ","")

	#Insert the filtering request in the DB
	insertFilteringRequestInDB(conn,cursor,association,reasoning,userID)
	
	#Perform the filtering of the associations
	filterAssociationsInDB(conn,cursor,association)

	with open(sys.argv[4], 'w') as outp:
		outpString = "The association '" + association + "' was filtered out from the Linked2Safety knowledge database"
		outp.write(outpString)

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

#Insert the filtering request in the DB
def insertFilteringRequestInDB(conn,cursor,association,reasoning,userID):
	#INSERT IN DB
	sql_string = "INSERT INTO filteringaction (association,reasoning,userID) VALUES (%s,%s,%s);";
	cursor.execute(sql_string,(association,reasoning,userID));	
	conn.commit();	
	
	
#Set as invinsible all associations that match the given association
def filterAssociationsInDB(conn,cursor,association):
	association = association.lower()
	#UPDATE IN DB
	sql_string = "UPDATE association SET visible = %s WHERE lower(association) LIKE %s;";
	cursor.execute(sql_string, ("False",association));	
	
	sql_string = "UPDATE selfreportedassociation SET visible = %s WHERE lower(association) LIKE %s;";
	cursor.execute(sql_string, ("False",association));	
	conn.commit();	

#Check if value is numeric
def is_numeric(value):
    try:
        float(value)
        return True
    except ValueError:
        return False

#Check if the association is in a valid format
def isValidAssociation(association):
	pos = association.find("=>")
	pos2 = association.rfind("=>")
	if pos == -1 or pos != pos2:
		return False
	leftHandSide = association[0:pos].strip()
	rightHandSide = association[pos+2:].strip()

	if isValidFormat(leftHandSide) and isValidFormat(rightHandSide):
		return True
	return False
	
#Check if the data are in the form "a & b & c ..."
def isValidFormat(data):
	tokens = data.split('&')
	for token in tokens:
		token = token.strip()
		if token == "":
			return False
		if token.find(" ") != -1:
			return False
	return True
	
#Replace multiple spaces with a single space
def replaceMultipleSpaces(data):
	newData = ""
	#replace multiple spaces with single space
	i=0
	while i < len(data):
		if data[i] == " ":
			newData = newData + " "
			while i<len(data) and data[i] == " ":i = i + 1
		if i<len(data):
			newData = newData + data[i]
		i=i+1
	return newData

if __name__ == "__main__":
	main()

if conn:
	cursor.close();
	conn.close();

