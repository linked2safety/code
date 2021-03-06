#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   retrieveAssocBarchartSelectValues.py -  Retrieves the possible values that can be given as input in the tool
#   that creates a barchart for associations regarding Adverse Events  
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
from l2s import DBconnection

conn = None;

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

#Returns the variables that represent Adverse Events
def getVariables():
	#CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#GET THE LINKED2SAFETY ID OF THE USER AND IF THE USER IS ALLOWED TO USE THIS TOOL
	#Define the parameters
	select_field = "name";
	input_table = "categoriesmeaning";
	where_clause = "type = 'AE'"
	#Execute the query
	result = SelectFromDB(cursor, select_field, input_table, where_clause);
	cursor.close();
	conn.close();

	variables = [];
	for i in range(0,len(result)):
		variables.append(list(result[i])[0].rstrip());

	variables = list(set(variables))
	for i in range(0,len(variables)):
		variables[i] = tuple([variables[i],variables[i],False]);
	return variables;

