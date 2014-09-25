#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   getCategoriesMeaning.py - Finds the meanings of the categorical values of the variables
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
sys.path.insert(0,'/home/galaxy/galaxy-dist/lib/l2s')
import DBconnection

conn = None;

def main():

	inpFile = sys.argv[1]
	outFile = sys.argv[2];

	input = open(inpFile);
	varNames = input.readline();

	#Remove the name of the last column which contains the counts
	#for the data cube dimensions
	vars=varNames.split(",");
	lastElemSize = len(vars[-1]) + 1;
	varNames = varNames[:-lastElemSize];
	varNames = varNames.replace(",","','");

	#-CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#GET THE MEANINGS OF THE VALUES OF THE VARIABLES OF INTEREST
	#Define the parameters
	select_field = "name,values,type,info";
	input_table = "categoriesmeaning";
	where_field = "name IN ('" + varNames + "')";
	#Execute the query
	varMeanings = SelectFromDB(cursor, select_field, input_table, where_field);

	#Check if all variables where found in the DB
	if((len(vars)-1) != len(varMeanings)):
		sys.exit("Could not find the meanings of all variables.\nPlease inform the system administrator.");

	#Trim any whitespaces at the end of each field and remove the ' characters
	trimmedVarMeanings = [];
	for i,record in enumerate(varMeanings):
		newString = "";
		for j,field in enumerate(record):
			if(field==None):
				newString = newString + "|-";
			else:
				field = field.rstrip();
				field = field.replace("'","");
				if j>0:
					newString = newString + "|" + field;
				else:
					newString = newString + field;
		trimmedVarMeanings.append(newString);

	output = file(outFile, "w");
	for line in trimmedVarMeanings:
		output.write(line+"\n");

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



if __name__ == "__main__":
	main()

if conn:
	cursor.close();
	conn.close();
