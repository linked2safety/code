#    ProfilesOfEachAdverseEventBarchart.py - creates a barchart to display the number of profiles of a user which contain each adverse event
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

import psycopg2
import sys, os
sys.path.insert(0,'/home/galaxy/galaxy-dist/lib/l2s')
import DBconnection
import pylab as p
import re


def main():
	user_email = sys.argv[1]
	output_path = sys.argv[2]
	
	ConnClass = DBconnection.connection()
	conn = ConnClass.connectMethod()
	cursor = conn.cursor()

	AdverseEventsDict = {}#dictionary: key=adverse event,value=number of profiles which contain the adverse event
	AdverseEventsProfilesDict = {}#dictionary: key=profile name, value=a list of AEs of a profile
	AdverseEventsDict = get_AEs_from_database(AdverseEventsDict,cursor)
	
	#count profiles of each adverse event 
	user_id = get_user_id(user_email,cursor)
	records = get_profiles_of_user(user_id,cursor)
	records2 = get_safety_alerts_of_profiles_of_user(user_id,cursor)
	AdverseEventsProfilesDict = store_AEs_of_each_profile(user_id,records,records2,cursor,AdverseEventsProfilesDict)

	AEs = AdverseEventsDict.keys()
	for AE in AEs:
		AE_str = AE[0]#AE is a tuple e.g. ('Diabetes',)
		count=0		
     		profile_names = AdverseEventsProfilesDict.keys()
		for profile_name in profile_names:
			AEs_of_profile = AdverseEventsProfilesDict[profile_name]
			if AE_str in AEs_of_profile:
				count = count+1
		AdverseEventsDict[AE] += count

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

def get_user_id(user_email,cursor):
	select_field, input_table, where_field ="userid", "users", "email_address='"+user_email+"'"
	record = SelectItem(cursor, select_field, input_table, where_field)
	user_id = record[0]
	user_id = user_id[0]
	user_id = str(user_id)
	return user_id

def get_profiles_of_user(user_id,cursor):
	select_field, input_table, where_field = "profile", "SafetyAlertNotificationSubscription", "userid="+user_id
        records = SelectItem(cursor, select_field, input_table, where_field)
	return records

def get_safety_alerts_of_profiles_of_user(user_id,cursor):
	select_field, input_table, where_field = "profileid, safetyalert", "SafetyAlert", " profileid in (select profileid from SafetyAlertNotificationSubscription where userid="+user_id+")"
        records2 = SelectItem(cursor, select_field, input_table, where_field)
	return records2

def store_AEs_of_each_profile(user_id,records,records2,cursor,AdverseEventsDict):
	#input: an empty dictionary with no keys and no values
	#output: a dictionary: key=profile name, value=list of AEs of a profile
	for record in records:
		profile = record[0]
		profile_name = get_profile_name(profile)
		AdverseEventsDict[profile_name]=[]

	for record in records:
		profile = record[0]
		profile_name = get_profile_name(profile)
		AEs = get_AEs_history(profile)
		if AEs != "'Adverse Events History' field is empty or missing.":
		   store_adverse_events_of_profile(profile_name,AEs,AdverseEventsDict)
	
	for record2 in records2:
		profile_id = record2[0]	
		safety_alert = record2[1]
		select_field, input_table, where_field ="profile", "SafetyAlertNotificationSubscription", "profileid='"+str(profile_id)+"' and userid='"+user_id+"'"
	        record = SelectItem(cursor, select_field, input_table, where_field)
		profile = record[0]
		profile = profile[0]
		profile_name = get_profile_name(profile)
		AEs = get_AEs_of_safety_alerts(cursor,safety_alert)
		if AEs !=[]:
		   store_adverse_events_of_profile(profile_name,AEs,AdverseEventsDict)
	return AdverseEventsDict

def get_profile_name(profile):
	matchObj = re.match('Profile Name[:](.+)#Current Drug Treatments[:].*', profile, re.M|re.I)
	if matchObj:
		return matchObj.group(1)
	else:
		return "'Profile Name' field is empty or missing."

def get_AEs_history(profile):
	AEs=[]
	matchObj = re.match('.+#Adverse Events History[:](.+)#Date of Profile Creation[:].*', profile, re.M|re.I)
	if matchObj!= None and matchObj.group(1)!='':
		pairs = matchObj.group(1)
		pairs = pairs.split(',')		
		for pair in pairs:
			AE_val = pair.split('=')
			AE = AE_val[0]
			if AE not in AEs:
				AEs.append(AE)
	else:
		AEs ="'Adverse Events History' field is empty or missing."
	return AEs

def get_AEs_of_safety_alerts(cursor, safety_alert):
	AEs_of_safety_alert = []
	select_field, input_table, where_field = "distinct name", "categoriesmeaning","type='AE'"
	records = SelectItem(cursor, select_field, input_table, where_field)
	for record in records:
		AE = record[0]	
		pattern = ".+"+AE+".+"
		matchObj = re.match(pattern, safety_alert, re.M|re.I)
		if matchObj:
			AEs_of_safety_alert.append(AE)			
	return AEs_of_safety_alert	
	
def store_adverse_events_of_profile(profile_name,AEs,AdverseEventsDict):
	for AE in AEs:		
		profile_AEs = AdverseEventsDict[profile_name]
		if AE not in profile_AEs:
			profile_AEs.append(AE)
			AdverseEventsDict[profile_name] = profile_AEs

def SelectItem(cursor, select_field, input_table, where_field):		
	if where_field == None:
		cursor.execute("SELECT " + select_field + " FROM " + input_table)
	else:
		cursor.execute("SELECT " + select_field + " FROM " + input_table + " WHERE " + where_field)	
	records = cursor.fetchall()
	return records
							
def BarChart(AdverseEventsDict,output_file):
		#List with the keys of the dictionary
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
		ax.set_ylabel('Number of Profiles of Each Adverse Event')

		# Create a title, in italics
		ax.set_title('Number of profiles containing each adverse event',fontstyle='normal')

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
