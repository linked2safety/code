#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   insertAssociation.py - Inserts an association in the DB 
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
	print sys.argv[2]
	p_value = sys.argv[2];
	odds_ratio = sys.argv[3];
	ld = sys.argv[4];
	reference = util.restore_text(sys.argv[5]);
	notes = util.restore_text(sys.argv[6]);
	userEmail = sys.argv[7];
	
	#Check if the values are valid
	if is_numeric(p_value):
		tempVal = float(p_value)
		if tempVal<0 or tempVal>1:
			message = "The p_value must be in the range [0,1]."
			sys.stderr.write("error: \n%s\n" % message)
			sys.exit(-1)
	else:
		p_value = None
	
	if is_numeric(odds_ratio):
		tempVal = float(odds_ratio)
		if tempVal<0:
			message = "The odds ratio value must be larger than zero."
			sys.stderr.write("error: \n%s\n" % message)
			sys.exit(-1)	
	else:
		odds_ratio = None

	if is_numeric(ld):
		tempVal = float(ld)
		if tempVal<-1 or tempVal>1:
			message = "The value of the LD must be in the range [-1,1]."
			sys.stderr.write("error: \n%s\n" % message)
			sys.exit(-1)	
	else:
		ld = None						
	
	#CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#GET THE LINKED2SAFETY ID OF THE USER AND IF THE USER IS ALLOWED TO USE THIS TOOL
	#Define the parameters
	select_field = "userid, is_expert";
	input_table = "users";
	where_field = "email_address='" +userEmail + "'";
	res = SelectFromDB(cursor, select_field, input_table, where_field);

	#Check if the user is allowed to use this tool
	if len(res) == 0:
		message = "You are not a registered user.\nPlease Register before using this tool."
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)

	userID = res[0][0]
	isExpert = res[0][1]
	
	if isExpert != True:
		message = "You are not authorised for using this tool.\n Please contact the system administrator for requesting access to this tool."
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)

	#Check if the association provided by the user is in a valid format
	association = replaceMultipleSpaces(association)
	if not isValidAssociation(association):
		message = "The association provided is not valid.\nThe association should be in the form: a & b => c"
		sys.stderr.write("error: \n%s\n" % message)
		sys.exit(-1)

	#Insert the new association in the DB
	association = association.replace(" ", "")
	insertAssociationInDB(conn,cursor,association,p_value,odds_ratio,ld,reference,notes,userID)

	with open(sys.argv[8], 'w') as outp:
		outpString = "The association '" + association + "' has been added in the Linked2Safety knowledge database"
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

#Insert a new association in the DB
def insertAssociationInDB(conn,cursor,association,p_value,odds_ratio,ld,reference,notes,userID):
	#INSERT IN DB
	sql_string = "INSERT INTO selfreportedassociation (association,p_value,odds_ratio,ld,reference,notes,userid) VALUES (%s,%s,%s,%s,%s,%s,%s);";
	cursor.execute(sql_string,(association,p_value,odds_ratio,ld,reference,notes,userID));	
	conn.commit();

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

#Check if value is numeric
def is_numeric(value):
    try:
        float(value)
        return True
    except ValueError:
        return False

if __name__ == "__main__":
	main()

if conn:
	cursor.close();
	conn.close();

