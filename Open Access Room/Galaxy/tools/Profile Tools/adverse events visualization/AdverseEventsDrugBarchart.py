#    AdverseEventsDrugBarchart.py - creates a barchart to display the number of adverse events of each drug in the knowledge base
#
#    Copyright (C) 2014 The University of Manchester  tiand@cs.man.ac.uk
#  
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

	#get adverse events from adverseevent table
	select_field, input_table, where_field = "distinct name", "categoriesmeaning", "type='AE'";
	AEs = SelectItem(cursor, select_field, input_table, where_field)

 	#get drugs from drugs table
	select_field, input_table, where_field = "distinct name", "categoriesmeaning", "type='D'";
	drugs = SelectItem(cursor, select_field, input_table, where_field)
	#initialize dictionary: key=drug, value = empty list of AEs
	DrugAdverseEventsDict = {}
	for drug in drugs:
		DrugAdverseEventsDict[drug[0]]=[]

	select_field, input_table, where_field = "lefthandside, righthandside", "AssociationRule", "visible=true";
	rules = SelectItem(cursor, select_field, input_table, where_field)
	select_field, input_table, where_field = "lefthandside, righthandside", "SelfReportedAssociationRule","visible=true";
	rules2 = SelectItem(cursor,select_field,input_table,where_field)
	conn.close()
	rules = rules + rules2
	for rule in rules:
		lefthandside = rule[0]
		righthandside = rule[1]
		drugs_of_rule = get_drugs_or_AEs_of_rule(lefthandside,drugs)
		AEs_of_rule = get_drugs_or_AEs_of_rule(righthandside,AEs)
		
		if drugs_of_rule !=[] and AEs_of_rule !=[]:#rule contains drugs and AEs					
			for drug in drugs_of_rule:
				if DrugAdverseEventsDict.has_key(drug):
					 AEs_of_drug = DrugAdverseEventsDict[drug]
					 for AE_of_rule in AEs_of_rule:
						 if AE_of_rule not in AEs_of_drug:
								AEs_of_drug.append(AE_of_rule)
								DrugAdverseEventsDict[drug] = AEs_of_drug
				else:
					print "key: "+drug+" is not found in DrugAdverseEventsDict: "+DrugAdverseEventsDict
					break	  
	#count adverses events of each drug
	drugs = list(DrugAdverseEventsDict.keys())
	for drug in drugs:
		DrugAdverseEventsDict[drug]=len(DrugAdverseEventsDict[drug])
	
	BarChart(DrugAdverseEventsDict,output_path)

def get_drugs_or_AEs_of_rule(lefthandside_or_righthandside,drugs_or_AEs):	
	drugs_or_AEs_of_rule=[]
	pairs = lefthandside_or_righthandside.split('&')
        for pair in pairs:
	      variable_val = pair.split('=')
	      var = variable_val[0]
	      for drug_or_AE in drugs_or_AEs:
		if var == drug_or_AE[0]:	
	      		drugs_or_AEs_of_rule.append(var)
			break
	return drugs_or_AEs_of_rule
				
def SelectItem(cursor, select_field, input_table, where_field):		
	if where_field == None:
		cursor.execute("SELECT " + select_field + " FROM " + input_table)
	else:
		cursor.execute("SELECT " + select_field + " FROM " + input_table + " WHERE " + where_field)
	records = cursor.fetchall()

	return records

def BarChart(AdverseEventsDict,output_file):
		#List with the keys of the dictionary
		AdverseEvents_keys = list(AdverseEventsDict.keys())
		
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
		ax.set_ylabel('Number of Adverse Events of Each Drug')

		# Create a title, in italics
		ax.set_title('Number of Adverse Events of Drugs',fontstyle='normal')

		# This sets the ticks on the x axis to be exactly where we put the center of the bars.
		ax.set_xticks(ind)

		# Set the x tick labels to the AdverseEvents_keys defined above.
		ax.set_xticklabels(AdverseEvents_keys)		
		
		#Auto-rotate the x axis labels to show them more clearly
		fig.autofmt_xdate()

		#Create a y label
		ax.set_xlabel('Drugs')
	
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
