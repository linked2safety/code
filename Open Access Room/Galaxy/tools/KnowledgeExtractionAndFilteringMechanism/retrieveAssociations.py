#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   retrieveAssociations.py - Retrieves the visible associations from the L2S database
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

#Returns visible associations from the knowledge base
def getAssociations():
	#CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#Define the parameters
	select_field = "association";
	input_table = "association";
	where_clause = "visible = true"
	#Execute the query
	assoc = SelectFromDB(cursor, select_field, input_table, where_clause);
	input_table = "selfreportedassociation";
	assoc = assoc + SelectFromDB(cursor, select_field, input_table, where_clause);
		
	cursor.close();
	conn.close();

	assoc = list(set(assoc));
	for i in range(0,len(assoc)):
		association = list(assoc[i])[0];
		association = association.replace("&"," & ");
		association = association.replace("=>"," => ");
		assoc[i] = tuple([association,association,False]);

	return assoc;

