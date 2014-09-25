/* AssociationRules.java - uses the Aprioi association rules algorithm to discover association rules

    Copyright (C) 2014 The University of Manchester  tiand@cs.man.ac.uk
  
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
    This component links to weka.jar of weka-3-7-7 which is under GNU GPL version 3. 
    You may download the source code and associated license at: http://sourceforge.net/projects/weka/

    You may download the source code and associated license at  https://github.com/linked2safety/code
*/ 
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.System;
import java.lang.ProcessBuilder;
import java.sql.Timestamp;
import java.lang.Float;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class AssociationRules
{ 

  public LinkedList<LinkedList<String>> rules;//all association rules
  public HashMap<String,String> items_hashtable; //<item,id> pairs
  public HashMap<LinkedList<String>,String> itemsets_hashtable;//<itemset,id> pairs
  
 public final static void main(String[]args) throws Exception
 {
    String datacube_file_name=null;
    String meaningful_datacube_file_name;
    String association_rules_file_name=null;
    String categories_meaning_file_name="";
    String conf_threshold=null;
    String no_of_rules=null;	
    String weka_class_path=null;
    String cmd=null;
    String data_matrix_arff_file_name=null;
    String Apriori_results_file_name=null;	
    Random randomGenerator;
    ProcessBuilder pb;
    Process p=null;
    HashMap<String,Integer> hash_attributes_col_nos=null;
    String data_matrix[][]=null;
    String xml_desc_file_name=null;
    String option=null;
    String class_index=null;
    String delete_missing_values=null; //0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
    String datacube_no_missing_file_name=null;
    int number_of_instances_removed=0;
    String missing_value=null;

    if(args.length == 9)
    {
     option = args[0];//option: multi_adverse_events or class_index:_variable (e.g. 3:_diabetes)
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2]; //data cube file
     categories_meaning_file_name = args[3];//categories meaning file
     conf_threshold = args[4];//mini value threshold for confidence
     no_of_rules = args[5];//no. of rules
     weka_class_path=args[6];// e.g. /home/tiand/Downloads/weka-3-7-7/weka.jar 
     association_rules_file_name = args[7];//results file  
     xml_desc_file_name = args[8];//xml description of association rule
    }
    else//wrong number of input arguments, error message
    {
     System.err.println("Wrong number of input arguments.\nUsage: java AssociationRules <option: multi_adverse_events or <target variable index>:_<target variable>> <delete_missing_values (0 or 1)>  <datacube_file_name> <categories meaning file name> <confidence threshold> <no. of rules> <weka_class_path> <association_rules_file_name> <xml description file>\n");
     System.exit(-1);
    }
    randomGenerator = new Random();
    data_matrix_arff_file_name= "data_matrix_arff_file"+randomGenerator.nextInt(1000000)+".arff";
    meaningful_datacube_file_name = "meaningful_datacube_file"+Integer.toString(randomGenerator.nextInt(9000000));
    Apriori_results_file_name= "apriori_results_file"+Integer.toString(randomGenerator.nextInt(9000000));
    datacube_no_missing_file_name = "datacube_no_missing_file"+Integer.toString(randomGenerator.nextInt(9000000));

    //delete missing values from the data cube or replace missing values withh most frequent values of variables
    missing_value = "888";//888 represents the missing value category in categories meaning file
    number_of_instances_removed = DataCube.handle_missing_values(missing_value,delete_missing_values,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);

    //write information about missing values handling to association rules results file
    try {
         DataCube.record_information_on_missing_values(missing_value,delete_missing_values,categories_meaning_file_name,datacube_file_name,number_of_instances_removed,association_rules_file_name);
    }
    catch(IOException e)
    {
     e.printStackTrace();
     System.exit(-1);
    }

    try {
         DataCube.transform_data_cube_to_data_matrix(datacube_no_missing_file_name,categories_meaning_file_name,meaningful_datacube_file_name,data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(-1);
    }     
    if(option.equals("multi_adverse_events"))
    {
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.associations.Apriori -t "+data_matrix_arff_file_name+" -N "+no_of_rules+" -C "+conf_threshold+" > "+Apriori_results_file_name;
    }
    else //option = <target variable index>:_<target variable name> e.g. 3:_diabetes
    { //class association rule mining
     class_index = option.split(":")[0]; //valid format of target variable: 3:_diabetes
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.associations.Apriori -t "+data_matrix_arff_file_name+" -N "+no_of_rules+" -A -c "+class_index+" -C "+conf_threshold+" > "+Apriori_results_file_name;
    }
    
    pb = new ProcessBuilder();
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor();
   
    hash_attributes_col_nos = DataMatrix.find_attrs_col_nos(data_matrix_arff_file_name); 
    try
    {
     data_matrix = DataMatrix.load_data_into_array(data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by load_data_into_matrix method.");
     System.exit(-1);
    }

    AssociationRules ar = new AssociationRules();

    try
    {
     ar.support_confidence_lift(Apriori_results_file_name, data_matrix, hash_attributes_col_nos);
    }
    catch(IOException e)
    {
     System.err.println("IOException thrown by support_confidence_lift method.");
     System.exit(-1);
    }
    try
    {
     ar.rank_rules("lift", association_rules_file_name,"results_file_exist","=>");
    }
    catch(IOException e)
    {
     System.err.println("IOException thrown by rank_rules method.");
     System.exit(-1);
    }
    //create XML description of association rule
    try
    {
     xml_description(ar.rules,ar.items_hashtable,ar.itemsets_hashtable,data_matrix,"Apriori Association Rules Algorithm","association_rule_type="+option+" delete_missing_values="+delete_missing_values+" confidence_threshold="+conf_threshold+" no_of_rules="+no_of_rules,xml_desc_file_name);
    }
    catch(IOException e)
    {
     e.printStackTrace();
     System.exit(-1);
    }
    //delete temporary files
    
    cmd="rm "+datacube_no_missing_file_name+" "+meaningful_datacube_file_name+" "+data_matrix_arff_file_name+" "+Apriori_results_file_name;
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor();

  }

 AssociationRules()//constructor
 {
  rules = new LinkedList<LinkedList<String>>();//all association rules
  items_hashtable = new HashMap<String,String>();//all items of all rules
  itemsets_hashtable = new HashMap<LinkedList<String>,String>();//all itemsets of all rules
 }

 void support_confidence_lift(String Apriori_results_file_name, String [][]data_matrix, HashMap<String,Integer> hash_attributes_col_nos) throws IOException
 {
   BufferedReader in; 
   Matcher m1;
   Matcher m2;
   Matcher m3;
   Matcher m4;
   
   Pattern p1;
   Pattern p2;
   Pattern p3;
   Pattern p4;
   
   String LHS=null;
   String RHS=null;
   String rule_supp=null;
   String conf=null;
   String line=null;
   float lift=0f;
   StringTokenizer st;
   String item;
   String attr;
   String attr_val=null;
   String val=null;
   int attr_col;
   float RHS_supp;
   int k;
   int n;
   int first_RHS_item_index;
   int rule_supp_index;
   int last_index;
   int rule_no=1;
   float rule_support_count=0f;
   float rule_support=0f;
   float LHS_support_count=0f;
   float RHS_support_count=0f;
   float RHS_support=0f;
   LinkedList<String> rule;//represents a rule as a list of items, support, confidence, lift
   LinkedList<String> rule2;//represents a rule as a list: LHS itemset, RHS itemset, support, confidence, lift
   int item_id=0;
   int itemset_id=0;
   LinkedList<String> itemset=null;
   String itemset2="";
   String support=null; //rule support
   float support2=0;
   String lift2=null;
   DecimalFormat df; //format to round rule support

   rule=new LinkedList<String>();//a rule
   itemset = new LinkedList<String>();//an itemset
  
   df = new DecimalFormat("###.##########");//4 decimal places
   df.setRoundingMode(RoundingMode.HALF_UP);//e.g. 0.25672 to rounded to 0.2567, 0.53255 is rounded to 0.5326
   
   p1=Pattern.compile("^(.+)==>(.+)$");//association rule	
   p2 = Pattern.compile("^.+[=<>]+.+$");//an item
   p3 = Pattern.compile("^[0-9]+$");//support count
   p4 = Pattern.compile("^<conf:\\(([\\.0-9]+)\\)>$");//confidence

   in=new BufferedReader(new FileReader(Apriori_results_file_name));

   line=in.readLine();
   while(line!=null)
   {
    m1=p1.matcher(line);
    if(m1.matches())//matches a rule
    {
     rule2 = new LinkedList<String>();//initialize LinkedList 
     RHS_supp=0f;
     LHS=m1.group(1);
     RHS=m1.group(2);
     st=new StringTokenizer(LHS," "); //process LHS
     n=st.countTokens();
     for(int i=0;i<n;i++)
     {
      	 item=st.nextToken();
      	 m2=p2.matcher(item);     
      	 if(m2.matches())//an item
         {
      	   rule.add(item);
           if(!items_hashtable.containsKey(item))
           {
            items_hashtable.put(item,Integer.toString(item_id));//add item to items_hashtable
            item_id++;
           }
	   itemset.add(item);//add item to itemset
	   itemset2=itemset2+item+" ";//add item to itemset2
	 }
         else
         {
  	   m3=p3.matcher(item);
	   if(m3.matches())//matches support count of LHS
	   {
              LHS_support_count=Float.parseFloat(item);
	   }
	 }       
     }//for
     rule2.add(itemset2);//add itemset to rule2
     if(!itemsets_hashtable.containsKey(itemset))
     {
      itemsets_hashtable.put(itemset,Integer.toString(itemset_id));//put itemset in hashtable 
      itemset_id++;
     }
     itemset=new LinkedList<String>();   
     itemset2="";     //remove all items from String itemset2    
     rule.add("=>");
     first_RHS_item_index=rule.size();
     st=new StringTokenizer(RHS," ");//process RHS
     n=st.countTokens();
     for(int i=0;i<n;i++)
     {
    	  item=st.nextToken();
      	  m2=p2.matcher(item);     
      	  if(m2.matches())
          {
	   rule.add(item);
	   if(!items_hashtable.containsKey(item))
           {
            items_hashtable.put(item,Integer.toString(item_id));
            item_id++;
	   }
 	   itemset.add(item);//add item to itemset
	   itemset2=itemset2+item+" ";//add item to itemset2
	  }
	  else
          {
 	    m3 = p3.matcher(item);
            if(m3.matches())//matches support count of rule
	    { 
	      rule_support_count = Float.parseFloat(item); 
              support2 = rule_support_count/(float)data_matrix.length;//support: rule_support_count/total_no_of_records
	      support2 = Float.parseFloat(df.format(support2))*100f;//round to the specified decimal format and covert to percentage                
	      support = Float.toString(support2);
              rule.add(support); 
  	    }	
	    else
	    {
              m4 = p4.matcher(item);
	      if(m4.matches())//matches rule confidence
	      {
               conf = m4.group(1);
	       rule.add(conf);
	      }
            }
     	  }
      }
      rule2.add(itemset2);//add RHS itemset to rule2
      rule2.add(support);//add rule support
      rule2.add(conf);//add confidence
      if(!itemsets_hashtable.containsKey(itemset))
      {  
       itemsets_hashtable.put(itemset,Integer.toString(itemset_id)); //put RHS itemset in hashtable  
       itemset_id++;    
      }
      itemset = new LinkedList<String>();
      itemset2 = "";   //remove all items from String itemset2
      rule_supp_index = rule.size()-2;
      //compute support of RHS
      RHS_support_count = RHS_support_count(rule,data_matrix,first_RHS_item_index,rule_supp_index,hash_attributes_col_nos);
      //lift
      RHS_support = RHS_support_count/(float)data_matrix.length;
      lift = Float.parseFloat(conf)/RHS_support;
      lift2 = Float.toString(lift);
      rule.add(lift2); //add lift to rule
      rule2.add(lift2);//add lift to rule2
      rules.add(rule2);//add rule2 to rules
      rule.clear();//remove all elements from the rule linkedlist
   }  
   line=in.readLine();
  }	
  in.close();
 }

 float RHS_support_count(LinkedList<String> rule, String [][] data_matrix, int first_RHS_item_index, int rule_supp_index, HashMap<String, Integer> hash_attributes_col_nos)
  {
   Matcher m1;
   Pattern p1;
   String attr;
   String attr_val;
   int attr_col;
   String val;
   float RHS_supp=0f;
   String item;
   boolean instance_conditions_match;

   p1=Pattern.compile("^(.+)[=<>]+(.+)$");
   
    for(int i=0;i<data_matrix.length;i++)
     {  
        instance_conditions_match=true;  	        
     	for(int a=first_RHS_item_index;a<rule_supp_index;a++)
     	{
      	  item=rule.get(a);
      	  m1=p1.matcher(item);     
      	  if(m1.matches())
      	  {
       	   attr=m1.group(1);     
      	   attr_val=m1.group(2);
           attr_col=hash_attributes_col_nos.get(attr);
           val=data_matrix[i][attr_col];
           if(!attr_val.equals(val))
           {
              instance_conditions_match=false;
              break;
	   }
        }
      }//for
      if(instance_conditions_match == true)
      {
        RHS_supp++;
      }
   }
   return RHS_supp;
 }

 void rank_rules(String quality_metric, String results_file_name, String results_file_exist, String implication_sign) throws IOException
 {
  TreeSet<Float> scores;//set of scores of rules in ascending order e.g. lift
  HashMap<Float,String> scores_hashtable; //key: score of rule, value: indices of rules having the score in LinkedList rules
  ListIterator<LinkedList<String>> iter;
  LinkedList<String> rule = null;
  FileWriter out = null;
  Float score = null;
  int rule_no = 1;
  int k = 0;
  int last_index = 0;
  Integer rule_index = null;
  Iterator iter_desc;
  String rules_indices = "";
  String [] indices;
  
  if(results_file_exist.equals("results_file_exist"))
       out = new FileWriter(results_file_name,true);
  else if(results_file_exist.equals("results_file_not_exist"))
       out = new FileWriter(results_file_name);
  else
  {
   System.err.println("wrong option for results_file_exist in rank rules method");
   System.exit(-1);
  }
  scores = new TreeSet<Float>();
  scores_hashtable = new HashMap<Float,String>();

  //System.out.println("no. of rules: "+rules.size());
  //System.out.println("rules: "+rules);
  //get scores of the rules
  if(quality_metric.equals("lift"))
  {
   iter = rules.listIterator(0);   
   while(iter.hasNext())
   {
    rule = (LinkedList<String>)iter.next();

    //System.out.println(rule);

    score = new Float((String)rule.get(rule.size()-1));//get lift of rule
    scores.add(score);
    if(scores_hashtable.containsKey(score))
    {
      rules_indices = (String)scores_hashtable.get(score);
      rules_indices = rules_indices+","+rules.indexOf(rule);
      scores_hashtable.put(score,rules_indices);
    }
    else
      scores_hashtable.put(score,Integer.toString(rules.indexOf(rule)));
   }
  }
  else
  {
   System.err.println("The quality metric specified is not lift");
   System.exit(-1);
  }
  //rank rule in descending order of their scores
  iter_desc = scores.descendingIterator();
  
  while(iter_desc.hasNext())
  {
   score = (Float)iter_desc.next();
   rules_indices = (String) scores_hashtable.get(score);
   indices = rules_indices.split(",");
   for(int index = 0; index < indices.length; index++)
   { 
    rule = (LinkedList<String>)rules.get(Integer.parseInt(indices[index]));
    //write rule to file
    last_index = rule.size()-1;
    k = last_index-3;
    out.write(rule_no+". ");
    //System.out.print(rule_no+". ");

    if(implication_sign.equals("none")) 
    {	
     for(int i=0;i<k; i++)
     {
      //System.out.print(i+": "+rule.get(i));
      out.write(rule.get(i)+" ");
     }

     //System.out.print(k+": "+rule.get(k)+" "+last_index+": "+rule.get(last_index)+", "+(last_index-1)+": "+rule.get(last_index-1)+" "+(last_index-2)+": "+rule.get(last_index-2)+"%\n");
   
     out.write(rule.get(k)+" lift: "+rule.get(last_index)+", confidence: "+rule.get(last_index-1)+", support: "+rule.get(last_index-2)+"%\n");
    }
    else if(implication_sign.equals("=>"))
    {
     out.write(rule.get(0)+" ");
     out.write("=> ");
     out.write(rule.get(1)+" lift: "+rule.get(4)+", confidence: "+rule.get(3)+", support: "+rule.get(2)+"%\n");
    }
    else
    {
     System.err.println("wrong option in rank rules method");
     System.exit(-1);
    }
    rule_no++;
   }//for
  }//while
  out.close();
 }

 static void xml_description(LinkedList<LinkedList<String>> rules,HashMap<String,String> items_hashtable, HashMap<LinkedList<String>,String> itemsets_hashtable, String [][] data_matrix, String algorithm, String paramsSetting, String xml_desc_file_name)throws IOException
 { 
/*<DataMiningAlgorithm algorithm_name=”Apriori Association Rules Algorithm”, algorithmParamsSetting=”confidence_threshold=0.9 no_of_rules=10”/> 
<PMML version="4.0" xsi:schemaLocation="http://www.dmg.org/PMML-4_0 http://www.dmg.org/v4-0/pmml-4-0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.dmg.org/PMML-4_0">
  <Header copyright="Copyright (c) 2011 Zementis Inc. (www.zementis.com)" description="Association Rules Model">
    <Timestamp>Apr 8, 2011</Timestamp>
  </Header>
  <DataDictionary numberOfFields="4">
<DataField dataType="string" name="Drugs=A" optype="categorical"/>
<DataField dataType="string" name="Age>60" optype="categorical"/>
<DataField dataType="string" name="BMI>10" optype="categorical"/>
<DataField dataType="string" name="Adverse_Events=diabetes" optype="categorical"/>
  </DataDictionary>
  <AssociationModel functionName="associationRules" minimumConfidence="0.63" minimumSupport="0.2" modelName="Rectangular_AR" numberOfItems="4" numberOfItemsets="2" numberOfRules="3" numberOfTransactions="435">
    <MiningSchema>
      <MiningField name="Drugs=A" usageType="active"/>
      <MiningField name="Age>60" usageType="active"/>
      <MiningField name="BMI>10" usageType="active"/>
      <MiningField name=”Adverse_Events=diabetes" usageType="active"/>
    </MiningSchema>
    <Item id="1" value="Drugs=A"/>
    <Item id="4" value="Age>60"/>
    <Item id="5" value="BMI>10"/>
    <Item id="10" value="Adverse_Events=diabetes"/>
    <Itemset id="1" numberOfItems="3">
      <ItemRef itemRef="1"/>
      <ItemRef itemRef="4"/>
      <ItemRef itemRef="5"/>
    </Itemset>
    <Itemset id="2" numberOfItems="1">
      <ItemRef itemRef="10"/>
    </Itemset>
    <AssociationRule antecedent="1" confidence="0.9" consequent="2" lift="1.6" support="500"/>
</AssociationModel>
</PMML>
*/
  String antecedent=null;
  String consequent=null;
  String support=null;
  String conf=null;
  String lift=null;
  Iterator keys=null;
  FileWriter out=null;
  String item=null;
  LinkedList<String> itemset=null;
  ListIterator list_iter = null;
  Date date=null;
  LinkedList<String> rule = null;
  out = new FileWriter(xml_desc_file_name);

  out.write("<log>");
  out.write("<DataMiningAlgorithm algorithm_name=\""+algorithm+"\" algorithmParamsSetting=\""+paramsSetting+"\">\n"); 
  out.write("<PMML>\n");
  //output header
  out.write("<Header description=\"Association Rules Model\">\n");
  date= new Date();
  out.write("  <Timestamp>"+new Timestamp(date.getTime()) +"</Timestamp>\n");
  out.write("</Header>\n");
  //output Datadictionary
  out.write("<DataDictionary numberOfFields=\""+items_hashtable.size()+"\">\n");
  keys = (items_hashtable.keySet()).iterator();
  while(keys.hasNext())
  {
	   try
	   {
	    item = (String)keys.next();
	   }
	   catch (NoSuchElementException e)
	   {
	    e.printStackTrace();
	    System.exit(-1);
	   }
	   out.write("<DataField dataType=\"string\" name=\""+item+"\" optype=\"categorical\"/>\n");
  }
  out.write("</DataDictionary>\n");
  out.write("<AssociationModel functionName=\"associationRules\" modelName=\"Rectangular_AR\" numberOfItems=\""+items_hashtable.size()+"\" numberOfItemsets=\""+itemsets_hashtable.size()+"\" numberOfRules=\""+rules.size()+"\" numberOfTransactions=\""+data_matrix.length+"\">\n");
  //output MiningSchema
  out.write("<MiningSchema>\n");
  keys = (items_hashtable.keySet()).iterator();
  while(keys.hasNext())
  { 
	   try
	   {
	    item = (String)keys.next();
	   }
	   catch (NoSuchElementException e)
	   {
	    e.printStackTrace();
	    System.exit(-1);
	   }
	   out.write("<MiningField name=\""+item+"\" usageType=\"active\"/>\n");
  }
  out.write("</MiningSchema>\n");
  //output items
  keys = (items_hashtable.keySet()).iterator(); 
  while(keys.hasNext())
  { 
	   try
	   {
	    item = (String)keys.next();
	   }
	   catch (NoSuchElementException e)
	   {
	    e.printStackTrace();
	    System.exit(-1);
	   }
	   try
	   {
	    out.write("<Item id=\""+items_hashtable.get(item)+"\" value=\""+item+"\"/>\n");
	   }
	   catch(NullPointerException e)
	   {
	    e.printStackTrace();
	    System.exit(-1);
	   }
  }
  //output itemsets
 keys = (itemsets_hashtable.keySet()).iterator();
 while(keys.hasNext())
 { 
	   try
	   {
	    itemset = (LinkedList<String>)keys.next();
	   }
	   catch (NoSuchElementException e)
	   {
	    e.printStackTrace();
	    System.exit(0);
	   }
	   try
	   {
	    out.write("<Itemset id=\""+itemsets_hashtable.get(itemset)+"\" numberOfItems=\""+itemset.size()+"\">\n");
	   }
	   catch(NullPointerException e)
	   {
	    e.printStackTrace();
	    System.exit(0);
	   }
	   list_iter = itemset.listIterator(0);
	   while(list_iter.hasNext())
	   {
		    try
		    {
		     item = (String)list_iter.next();
		    }
		    catch (NoSuchElementException e)
		    {
		     e.printStackTrace();
		     System.exit(-1);
		    }
		    try
		    {
		     out.write("\t <ItemRef itemRef=\""+items_hashtable.get(item)+"\""+"/>\n");
		    }
		    catch(NullPointerException e)
		    {
		     e.printStackTrace();
		     System.exit(-1);
		    }
   	}
   	out.write("</Itemset>\n");
 }
 /* output association rules
 <AssociationRule antecedent="1" confidence="0.9" consequent="2" lift="1.6" support="500"/>
 </AssociationModel>
 </PMML>
 */
 list_iter = rules.listIterator(0);
 while(list_iter.hasNext())
 {
	  try
	  {
	   rule = (LinkedList<String>)list_iter.next();
	  }
	  catch (NoSuchElementException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   antecedent = rule.get(0);
	  }
	  catch (IndexOutOfBoundsException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   consequent = rule.get(1);
	  }
	  catch (IndexOutOfBoundsException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   support = rule.get(2);
	  }
	  catch (IndexOutOfBoundsException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   conf = rule.get(3);
	  }
	  catch (IndexOutOfBoundsException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   lift = rule.get(4);
	  }
	  catch (IndexOutOfBoundsException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
	  try
	  {
	   out.write("<AssociationRule antecedent=\""+antecedent+"\" confidence=\""+conf+"\" consequent=\""+consequent+"\" lift=\""+lift+"\" support=\""+support+"\"/>\n");
	  }
	  catch(NullPointerException e)
	  {
	   e.printStackTrace();
	   System.exit(-1);
	  }
  }
  out.write("</AssociationModel>\n</PMML>\n</DataMiningAlgorithm>\n");
  out.write("</log>");
  out.close();
 }
} 
