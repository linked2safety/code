
# # # # # # # # # # # # # # #  # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
#
#   insertAssociationRule.py - Retrieve all XML log files from LogFile table and parse each log file and insert contents into database tables 
#   
#   Copyright 2014 - University of Manchester - john.keane@manchester.ac.uk
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

#example log file:
#<Workflow date_of_workflow="21 Mar 2013" description_of_workflow="input_data,Missing Data Test">
#<Dataset SPARQL_query="Select From Where Clause Goes Here" time="14:00pm 1 Jan 2013" />
#<QualityControl algorithmParamsSetting="threshold=0.1" algorithm_name="Missing Data Test" />
#</Workflow>

import psycopg2
import sys, os
import re
from xml.dom.minidom import parse
import xml.dom.minidom

#Insert data in the database
def parse_and_insert(conn,cursor,logfilecontent,user_id):
	
 #Open XML document using minidom parser
 dom = xml.dom.minidom.parseString(logfilecontent)
 workflow = dom.documentElement
 
 #get Dataset table fields
 dataset = workflow.getElementsByTagName("Dataset")[0]
 sparql_query = dataset.getAttribute("SPARQL_query")
 retrieval_date = dataset.getAttribute("time")
 #insert into Dataset table if not already in DB
 select_field = "datasetid";
 input_table = "datasetused";
 where_field = "SPARQL_query='" +sparql_query + "' AND retrieval_date='" + retrieval_date + "'" ;
 #Execute the query
 datasetID="";
 rec = SelectFromDB(cursor, select_field, input_table, where_field);
 if cursor.rowcount != 1:
   sql = "INSERT INTO DatasetUsed (SPARQL_query,retrieval_date) VALUES (%s,%s) RETURNING datasetID"
   cursor.execute(sql,(sparql_query,retrieval_date))
   conn.commit()
   datasetID = cursor.fetchone()[0]
 else:
   datasetID = rec[0][0];
   
 #get Workflow table fields
 workflow = dom.documentElement
 date_of_workflow = workflow.getAttribute("date_of_workflow")
 workflow_description = workflow.getAttribute("description_of_workflow")
 
 #insert into PeriodicPB table
 sql = "INSERT INTO periodicbatchprocess (days,months,years,previous_date_of_execution) VALUES (%s,%s,%s,%s) RETURNING PeriodicBP_ID;"
 cursor.execute(sql,(0,1,0,date_of_workflow))
 conn.commit()
 PeriodicBP_ID = cursor.fetchone()[0]

 #insert into Workflow table
 sql = "INSERT INTO WorkflowUsed (periodicbp_id,userid,datasetid,workflow) VALUES (%s,%s,%s,%s) RETURNING workflowID;"
 cursor.execute(sql,(PeriodicBP_ID,user_id,datasetID,workflow_description))
 conn.commit()
 workflow_id = cursor.fetchone()[0]
 #get the rest of the Algorithm table fields
 set_targets = workflow.getElementsByTagName("SetTarget")
 quality_controls = workflow.getElementsByTagName("QualityControl")
 single_hypothesis_testings = workflow.getElementsByTagName("SingleHypothesisTesting")
 associations = workflow.getElementsByTagName("Association")
 data_minings = workflow.getElementsByTagName("DataMiningAlgorithm")
 association_rules = workflow.getElementsByTagName("AssociationRule")
 dimensionality_reductions = workflow.getElementsByTagName("DimensionalityReductionAlgorithm")

 #SET TARGET VARIABLE TOOL
 if(set_targets !=[]):
  for set_target in set_targets:
    algorithm_params_setting = set_target.getAttribute("algorithmParamsSetting")
    #insert into QualityControlAlgorithm table
    sql = "INSERT INTO SetTargetVariable (algorithmParamsSetting,workflowid) VALUES (%s,%s);"
    cursor.execute(sql,(algorithm_params_setting,workflow_id))
    conn.commit()
 
 #QUALITY CONTROL TOOLS   
 if(quality_controls !=[]):
  for quality_control in quality_controls:
    algorithm_params_setting = quality_control.getAttribute("algorithmParamsSetting")
    algorithm_name = quality_control.getAttribute("algorithm_name")
    #insert into QualityControlAlgorithm table
    sql = "INSERT INTO QualityControl (algorithmParamsSetting,algorithm_name,workflowid) VALUES (%s,%s,%s);"
    cursor.execute(sql,(algorithm_params_setting,algorithm_name,workflow_id))
    conn.commit()
    
 #SINGLE HYPOTHESIS TESTING TOOLS   
 if(single_hypothesis_testings != []):
  for single_hypothesis_testing in single_hypothesis_testings: 
    algorithm_params_setting = single_hypothesis_testing.getAttribute("algorithmParamsSetting")
    algorithm_name = single_hypothesis_testing.getAttribute("algorithm_name")
    #insert into StatisticalAlgorithm table
    sql = "INSERT INTO StatisticalAlgorithm (algorithmParamsSetting,algorithm_name,workflowid) VALUES (%s,%s,%s);"
    cursor.execute(sql,(algorithm_params_setting,algorithm_name,workflow_id))
    conn.commit()
    
 #ASSOCIATIONS FROM SINGLE HYPOTHESIS TESTING TOOLS   
 if(associations !=[]):
  for association in associations:
    description = association.getAttribute("Description")
    description = description.replace(" ","")
    
    #sort the variables of the left side of the association based on their name
    assocSplit = description.split("=>");
    leftSide = assocSplit[0].split("&");
    leftSide.sort();
    assocSplit[0] = "&".join(leftSide);
    description = "=>".join(assocSplit);
    
    p_value = association.getAttribute("p_value")
    odds_ratio= association.getAttribute("odds_ratio")
    LD = association.getAttribute("LD")
    if(p_value ==""): #no p-value given
      p_value = None #unknown value
    else:
      p_value = float(p_value)
    if(odds_ratio ==""):
      odds_ratio = None
    else:
      odds_ratio = float(odds_ratio)
    if(LD == ""):
      LD = None
    else:
      LD = float(LD)
    #insert into Association table
    if ((p_value != None and p_value <=0.05) or LD != None):
	  sql = "INSERT INTO Association (association,workflowid,p_value,odds_ratio,LD) VALUES (%s,%s,%s,%s,%s);"
	  cursor.execute(sql,(description,workflow_id,p_value,odds_ratio,LD))
	  conn.commit()
    elif (p_value == None and odds_ratio != None and (odds_ratio >= 1.25 or odds_ratio <= 0.75)):
	  sql = "INSERT INTO Association (association,workflowid,p_value,odds_ratio,LD) VALUES (%s,%s,%s,%s,%s);"
	  cursor.execute(sql,(description,workflow_id,p_value,odds_ratio,LD))
	  conn.commit()
	  		
 #DIMENSIONALITY REDUCTION TOOLS
 if(dimensionality_reductions !=[]):
  for dimensionality_reduction in dimensionality_reductions:
    algorithm_params_setting = dimensionality_reduction.getAttribute("algorithmParamsSetting")
    algorithm_name = dimensionality_reduction.getAttribute("algorithm_name")
    print ("dimensionality reduction "+algorithm_name+" "+algorithm_params_setting+"\n")
    #insert into DimensionalityReduction table
    sql = "INSERT INTO DimensionalityReductionAlgorithm (algorithmParamsSetting,algorithm_name,workflowid) VALUES (%s,%s,%s);"
    cursor.execute(sql,(algorithm_params_setting,algorithm_name,workflow_id))
    conn.commit()
    
 #DATA MINING TOOLS   
 if(data_minings != []):
  for data_mining in data_minings:
    algorithm_params_setting = data_mining.getAttribute("algorithmParamsSetting")
    algorithm_name = data_mining.getAttribute("algorithm_name")
    #insert into DataMiningAlgorithm table
    sql = "INSERT INTO DataMiningAlgorithm (algorithmParamsSetting,algorithm_name,workflowid) VALUES (%s,%s,%s);"
    cursor.execute(sql,(algorithm_params_setting,algorithm_name,workflow_id))
    conn.commit()
    if(association_rules !=[]):
      rulesFound = set();
      for association_rule in association_rules:
        left_hand_side = association_rule.getAttribute("antecedent").strip()
        left_hand_side = insert_ampersand(left_hand_side) #insert '&' between items 
        left_hand_side = left_hand_side.replace(" ","")
        right_hand_side = association_rule.getAttribute("consequent").strip()
        right_hand_side = insert_ampersand(right_hand_side) #insert '&' between items
        right_hand_side = right_hand_side.replace(" ","")
        support = association_rule.getAttribute("support")
        confidence = association_rule.getAttribute("confidence")
        lift = association_rule.getAttribute("lift")
        if support == 'null':
          s = 0
        else:
          s=float(support)
        if confidence == 'null':
          c = 0 
        else:
          c=float(confidence)
        if lift == 'null':
	      l = 0
        else:
          l=float(lift)
          
        #sort the rule values so that their order does not affect the identification
        #of duplicate rules  
        leftSide = left_hand_side.split("&");
        leftSide.sort();
        left_hand_side = '&'.join(leftSide)
 
        rightSide = right_hand_side.split("&");
        rightSide.sort();
        right_hand_side = '&'.join(rightSide)
          
        fullRule = left_hand_side+right_hand_side+support+confidence+lift;
          
        #insert into AssociationRule table
        if c >= 0.7 and fullRule not in rulesFound:
          sql = "INSERT INTO AssociationRule (lefthandside,righthandside,support,confidence,lift,workflowid) VALUES (%s,%s,%s,%s,%s,%s);"
          cursor.execute(sql,(left_hand_side,right_hand_side,s,c,l,workflow_id))
          conn.commit()
        rulesFound.add(fullRule);  

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

def insert_ampersand(itemset): 
   pattern ='^(.+)&+$'
   itemset = replaceMultipleSpaces(itemset)
   itemset = itemset.replace(' ','&')
   itemset = itemset.replace('&=&','=')
   matchObj = re.search(pattern,itemset,re.M|re.I)
   if matchObj:
      itemset = matchObj.group(1)
   return itemset



