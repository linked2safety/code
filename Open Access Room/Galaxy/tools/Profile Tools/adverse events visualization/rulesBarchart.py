#    rulesBarchart.py - creates a barchart to display the total number of association rules which detect each adverse event in the knowledge base
#
#    Copyright (C) 2014 The University of Manchester  tiand@cs.man.ac.uk 
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version. 
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.  
#    You may download the source code and associated license at  https://github.com/linked2safety/code


import matplotlib
matplotlib.use('Agg')
import psycopg2
import sys, os
import pylab as p
sys.path.insert(0,'/home/galaxy/galaxy-dist/lib/l2s')
import DBconnection
import re

def main():

	output_path = sys.argv[1]
    	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	#get adverse events from AdverseEvents table and put them into a dictionary (key=adverse event,value=count of rules of the adverse event)
	AdverseEventsDict = {}
	AdverseEventsDict = get_AEs_from_database(AdverseEventsDict,cursor)

	#count association rules in AssociationRule table
	select_field, input_table, where_field = "righthandside", "AssociationRule", "visible=true";
        records = SelectItem(cursor, select_field, input_table, where_field)

	for record in records: 				
		righthandside = record[0] 
		count_adverse_event_rules(cursor,righthandside,AdverseEventsDict)
	
	#count association rules in SelfReportedAssociationRule table
	select_field, input_table, where_field = "righthandside", "SelfReportedAssociationRule", "visible=true";
	records = SelectItem(cursor, select_field, input_table, where_field)

	for record in records:				 
		righthandside = record[0]
		count_adverse_event_rules(cursor,righthandside,AdverseEventsDict)
	
        #Remove from the dictionary the adverse events with no association rules of the adverse events
	items = list(AdverseEventsDict.items())#get a list of (key,value) tuple pairs (items)
	for item in items:
		if item[1] == 0:
			del AdverseEventsDict[item[0]]# delete the item i.e. (key,value) pair with key item[0]

	conn.close()
	BarChart(AdverseEventsDict,output_path)

def get_AEs_from_database(AdverseEventsDict,cursor): 
	#input: an empty dictionary with no keys and no values
	#output: dictionary (key=adverse event,value=count of rules of the adverse event (value = 0))
	select_field, input_table, where_field = "distinct name", "categoriesmeaning", "type ='AE'";
	AEs = SelectItem(cursor, select_field, input_table, where_field)
	for AE in AEs:
		AdverseEventsDict[AE] = 0;
	return AdverseEventsDict

def count_adverse_event_rules(cursor,righthandside,AdverseEventsDict):
	variables=[]
	pairs = righthandside.split('&')
        for pair in pairs:
	      variable_val = pair.split('=')
	      variables.append(variable_val[0])

	for variable in variables:
		#check whether variable is an adverse event
		select_field, input_table, where_field = "name", "categoriesmeaning", "name=\'"+variable+"\' and type='AE'";
		records = SelectItem(cursor, select_field, input_table, where_field)
		#count the rule if variable is an adverse event
		if records !=[]:
			AdverseEventsDict[records[0]] = AdverseEventsDict[records[0]] + 1
							
def SelectItem(cursor, select_field, input_table, where_field):		
	if where_field == None:
		cursor.execute("SELECT " + select_field + " FROM " + input_table)
	else:
		cursor.execute("SELECT " + select_field + " FROM " + input_table + " WHERE " + where_field)
	records = cursor.fetchall()
	return records

def BarChart(AdverseEventsDict,output_file):
		AdverseEvents_keys = AdverseEventsDict.keys()
		keys=[]
		for key in AdverseEvents_keys:#remove brackets, comma and 's from keys e.g. ('Diabetes',)
		      key=list(key)
		      keys.append(key[0])

		AdverseEvents_keys = keys
		
		#List with the values of the dictionary
		AdverseEventsDict_values = list(AdverseEventsDict.values())

		#Create figure
		fig = p.figure()
		ax = fig.add_subplot(1,1,1)

		# Calculate how many bars there will be
		N = len(AdverseEventsDict_values)

		# Generate a list of numbers, from 0 to N -> This will serve as the (arbitrary) x-axis, which we will then re-label manually.
		ind = range(N)
		 
		#Change the limit of y axis
		p.ylim([0, max(AdverseEventsDict_values)+2])

		# Create bar
		ax.bar(ind, AdverseEventsDict_values, facecolor='#cc0000', align='center', ecolor='red', width = 0.5)

		#Create a y label
		ax.set_ylabel('Number of Association Rules')

		# Create a title, in italics
		ax.set_title('Number of association rules of each adverse event',fontstyle='normal')

		# This sets the ticks on the x axis to be exactly where we put the center of the bars.
		ax.set_xticks(ind)

		# Set the x tick labels to the AdverseEvents_keys defined above.
		ax.set_xticklabels(AdverseEvents_keys)		
		
		#Auto-rotate the x axis labels to show them more clearly
		fig.autofmt_xdate()

		#Create a y label
		ax.set_xlabel('Adverse Events')
	
		#Save the barchart as .pdf or .png or .eps
		output_pdf = output_file+'.pdf'
		p.savefig(output_pdf)
		
		# shuffle it back and clean up
		data = file(output_pdf, 'rb').read() 
		fp = open(output_file, 'wb')
		fp.write(data)
		fp.close()
		os.remove(output_pdf)

if __name__ == "__main__":
	main()
