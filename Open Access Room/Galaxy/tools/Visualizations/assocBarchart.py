#!/usr/bin/env python

# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   assocBarchart.py - Function that creates a barchart of the number of adverse events in the associations  
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
#For the creation of the barchart of Adverse Events
#import pylab as p

import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as p


from galaxy import util
from l2s import DBconnection

conn = None;

def main():
	adverseEvents = sys.argv[1]
	adverseEvents = adverseEvents.split(",")
	output_path = sys.argv[2]
	#CONNECT TO THE DATABASE
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#SET THE DICTIONARY WITH THE ADVERSE EVENTS OF INTEREST
	AdverseEventsDict = {}	
	#For all the lines of the result of select query (records)
	for ae in adverseEvents:
		AdverseEventsDict[ae] = 0;

	#GET THE ASSOCIATIONS FROM THE DATABASE
	#Define the parameters
	select_field, input_table, where_field = "association", "association", "visible=TRUE";
	#Call the query
	records = SelectFromDB(cursor, select_field, input_table, where_field)

	#Update the counts of each adverse event using the DB associations returned
	for line in records:
		tokens = splitAssociation(line[0])
		#Only keep the words that are adverse events (increase the value of that key (word) by 1)
		if AdverseEventsDict.has_key(tokens[1]):
			AdverseEventsDict[tokens[1]] = AdverseEventsDict[tokens[1]] + 1;
	
	#GET THE SELF REPORTED ASSOCIATIONS FROM THE DATABASE
	#Define the parameters
	select_field, input_table, where_field = "association","selfreportedassociation", "visible=TRUE";
	#Call the query
	records = SelectFromDB(cursor, select_field, input_table, where_field)

	#Update the counts of each adverse event using the DB associations returned
	for line in records:
		tokens = splitAssociation(line[0])
		#Only keep the words that are adverse events (increase the value of that key (word) by 1)
		if AdverseEventsDict.has_key(tokens[1]):
			AdverseEventsDict[tokens[1]] = AdverseEventsDict[tokens[1]] + 1;
	
	#Remove from the dictionary the adverse events with no associations found
	items = AdverseEventsDict.items()
	for item in items:
		if item[1] == 0:
			del AdverseEventsDict[item[0]]

	#Create the Barchart
	BarChart(AdverseEventsDict,output_path)


##########	FUNCTIONS	###################

#Split the association in two tokens using "=>" 
def splitAssociation(association):
	pos = association.find("=>")
	tokens = []
	tokens.append(association[0:pos].strip())
	tokens.append(association[pos+2:].strip())
	return tokens

#Select an item from a table in the database
def SelectFromDB(cursor, select_field, input_table, where_field):		
	# execute the Query
	if where_field == None:
		cursor.execute("SELECT " + select_field + " FROM " + input_table)
	else:
		cursor.execute("SELECT " + select_field + " FROM " + input_table + " WHERE " + where_field)
	
	# retrieve the records from the database
	records = cursor.fetchall()

	return records

def BarChart(AdverseEventsDict,output_file):

		#List with the keys of the dictionary
		AdverseEvents_keys = AdverseEventsDict.keys()
		#List with the values of the dictionary
		AdverseEventsDict_values = AdverseEventsDict.values()

		maxVal=0
		for value in AdverseEventsDict_values:
			if value>maxVal:
				maxVal=value

		#Create figure
		fig = p.figure()
		ax = fig.add_subplot(1,1,1)

		# Calculate how many bars there will be
		N = len(AdverseEventsDict_values)

		# Generate a list of numbers, from 0 to N -> This will serve as the (arbitrary) x-axis, which we will then re-label manually.
		ind = range(N)
		 
		#Change the limit of y axis
		p.ylim([0, maxVal+2])

		# Create bar
		ax.bar(ind, AdverseEventsDict_values, facecolor='#cc0000', align='center', ecolor='red', width = 0.5)

		#Create a y label
		ax.set_ylabel('Number of Associations')

		# Create a title, in italics
		ax.set_title('Number of associations of each adverse event',fontstyle='normal')

		# This sets the ticks on the x axis to be exactly where we put the center of the bars.
		ax.set_xticks(ind)

		# Set the x tick labels to the AdverseEvents_keys defined above.
		ax.set_xticklabels(AdverseEvents_keys)		
		
		#Auto-rotate the x axis labels to show them more clearly
		fig.autofmt_xdate()

		#Create a y label
		ax.set_xlabel('Adverse Event')
	
		#Save the barchart as .pdf or .png or .eps
		output_pdf = output_file + ".pdf"
		p.savefig(output_pdf)
		
		# shuffle it back and clean up
		data = file(output_pdf, 'rb').read() 
		fp = open(output_file, 'wb')
		fp.write(data)
		fp.close()
		os.remove(output_pdf)


if __name__ == "__main__":
	main()

if conn:
	conn.close()
