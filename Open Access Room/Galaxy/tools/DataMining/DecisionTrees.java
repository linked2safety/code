/* DecisionTrees.java - uses the C4.5 decision trees algorithm to build a decision tree using percentage split or cross validation and extracts association rules from the decision tree

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
import java.lang.Runtime;
import java.lang.ProcessBuilder;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class DecisionTrees
{  
 static int item_id=0;
 static int itemset_id=0;

 public final static void main(String[]args) throws Exception
 {
    String datacube_file_name = "";
    String meaningful_datacube_file_name;
    String categories_meaning_file_name="";
    String class_index ="";
    String weka_class_path = "";
    String cmd=null;
    String data_matrix_arff_file_name;
    String decision_tree_file_name=null;
    String association_rules_file_name=null;
    String results_file_temp=null;
    Random randomGenerator;
    ProcessBuilder pb;
    Process p=null;
    String target_variable=null;
    HashMap<String,Integer> hash_attributes_col_nos=null;
    String data_matrix[][]=null;
    String xml_desc_file_name=null;
    DecisionTrees dt;
    boolean tree_built = true;
    String option=null;
    String percentage_or_fold=null;
    AlgorithmParameters ap;
    AssociationRules ar = null;
    String delete_missing_values=null; //0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
    String datacube_no_missing_file_name=null;
    int number_of_instances_removed=0;
    String missing_value=null;
    FileWriter out=null;
    LinkedList<LinkedList<String>> rules;//stores all association rules, each rule is a list which consists of lhs itemset, rhs itemset, support, confidence and lift
        
    HashMap<String,String> items_hashtable; ////all items of all rules, key: item, value: id
    HashMap<LinkedList<String>,String> itemsets_hashtable;//all itemsets of all rules, key: itemset represented as LinkedList<String>, value: id
    
    dt = new DecisionTrees();
    
    if(args.length == 9)//target variable not specified, the variable with the largest index of data set is the target variable
    {   
     option = args[0]; //train_test_split or cross_validation
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2];
     categories_meaning_file_name = args[3];
     percentage_or_fold = args[4];//split percentage or cross validation folds
     weka_class_path=args[5];
     decision_tree_file_name = args[6];
     association_rules_file_name = args[7];
     xml_desc_file_name = args[8];
     ap = new AlgorithmParameters(datacube_file_name);
     class_index = String.valueOf(ap.get_index_of_variable_with_largest_index());//class_index starts from 1
     //System.out.println("class_index: "+class_index);
    }
    else if(args.length == 10)//target variable is specified. valid formats: 3:_diabetes or diabetes
    {
     option = args[0]; //train_test_split or cross_validation
     delete_missing_values = args[1];//0 or 1: 1 removes the instances with missing values; 0 replaces the missing values with the most frequent values of the variables; 
     datacube_file_name = args[2]; 
     categories_meaning_file_name = args[3];
     target_variable = args[4];  // target variable: valid formats: 3:_diabetes or diabetes 
     percentage_or_fold = args[5];//split percentage or cross validation folds
     weka_class_path = args[6];
     decision_tree_file_name = args[7];
     association_rules_file_name = args[8];
     xml_desc_file_name = args[9];
     //get index of target variable
     //valid formats: 3:_diabetes or diabetes
     ap = new AlgorithmParameters(datacube_file_name);
     class_index = ap.get_index_of_target_variable(target_variable);//class_index starts from 1
    }
    else //wrong number of input arguments, error message
    {
     System.out.println("Wrong number of input arguments.\nUsage: java DecisionTrees <option (train_test_split or cross_validation)> <delete missing values (0 or 1)> <data cube file name> <categories meaning file name> <target variable(optional)> <split percentage or folds> <weka-3-7-7.jar path> <decision tree file name> <association rules file name> <decision trees XML file>\n");
     System.exit(1);
    }

    randomGenerator = new Random();
    data_matrix_arff_file_name = "data_matrix_arff_file_name"+randomGenerator.nextInt(1000000)+".arff";
    results_file_temp = "results_file_temp"+Integer.toString(randomGenerator.nextInt(9000000));
    meaningful_datacube_file_name = "meaningful_datacube_file"+Integer.toString(randomGenerator.nextInt(9000000));
    datacube_no_missing_file_name = "datacube_no_missing_file"+Integer.toString(randomGenerator.nextInt(9000000));

    
    //delete missing values from the data cube or replace missing values with most frequent values of variables
    //if there are categories in the data cube csv file which have no meaning in the knowledge base, these categories are treated as missing values  
    missing_value = "888";//888 represents the missing value category in categories meaning file
    number_of_instances_removed = DataCube.handle_missing_values(missing_value,delete_missing_values,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);

    //write information about missing values handling to association rules results file
    try {
    DataCube.record_information_on_missing_values(missing_value,delete_missing_values,categories_meaning_file_name,datacube_file_name,number_of_instances_removed,association_rules_file_name);
    }
    catch(IOException e)
    {
     e.printStackTrace();
     System.exit(1);
    }
    try
    {
     meaningful_datacube_file_name = DataCube.transform_data_cube_to_data_matrix(datacube_no_missing_file_name,categories_meaning_file_name,meaningful_datacube_file_name,data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(1);
    }
    
    target_variable = DataMatrix.find_class_attr(data_matrix_arff_file_name,class_index);
    
    //check whether target variable has one value only.
    if(dt.oneClassDataSet(data_matrix_arff_file_name))
    {
    	System.out.println("target variable: "+target_variable+"\n\nThis tool failed to run on the input data cube.\nThis tool requires that the target variable contains at least two values but the target variable of the input data cube contains one value.\nTo use this tool, set a different variable as the target variable or use a different data cube.");
        try
        {
         out = new FileWriter(association_rules_file_name,true);
        }
        catch(IOException e)
        {
         e.printStackTrace();
         System.exit(1);
        }
        out.write("target variable: "+target_variable+"\n\nThis tool failed to run on the input data cube. \nThis tool requires that the target variable contains at least two values but the target variable of the input data cube contains one value.\nTo use this tool, set a different variable as the target variable or use a different data cube.\n");
        out.close();
        System.exit(2);  
     }    
    
    if(option.equals("train_test_split"))
    {
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.classifiers.trees.J48 -t "+data_matrix_arff_file_name+" -c "+class_index+" -split-percentage "+percentage_or_fold+" > "+results_file_temp;
    }
    else if(option.equals("cross_validation"))
    {
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.classifiers.trees.J48 -t "+data_matrix_arff_file_name+" -c "+class_index+" -x "+percentage_or_fold+" > "+results_file_temp;
    }
    else
    {
     System.err.println("C4.5 decision tree option is wrong.\n Valid options: train_test_split or cross_validation\nUsage: java DecisionTrees <option (train_test_split or cross_validation)> <delete missing values (0 or 1)> <data cube file name> <target variable(optional)> <split percentage or folds> <weka-3-7-7.jar path> <results file name> <decision trees XML file>");
     System.exit(1);
    }
    pb = new ProcessBuilder();
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor();
    //check if weka results file exists.
    if(!dt.checkFileExist(results_file_temp))
    {
     System.out.println(results_file_temp+" does not exist.");
     System.exit(3);
    }
    
    rules = new LinkedList<LinkedList<String>>();//all association rules
    items_hashtable = new HashMap<String,String>();//all items of all rules
    itemsets_hashtable = new HashMap<LinkedList<String>,String>();//all itemsets of all rules
    

    try
    {
     tree_built = dt.process_results_file(target_variable,results_file_temp,decision_tree_file_name,association_rules_file_name,"Decision Tree","results_file_exist");
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.out.println("IOException thrown by process_results_file method.");
     System.exit(1);
    }    
    
    //find column indices of variables in the data cube
    hash_attributes_col_nos = DataCube.find_attrs_col_nos(datacube_file_name);
    try
    {
     data_matrix = DataMatrix.load_data_into_array(data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.err.println("IOException thrown by load_data_into_matrix method.");
     System.exit(1);
    }  
   
    if(tree_built==true)
    {//extract rules from tree and add the rules to results file
     ar = new AssociationRules();
     try
     { 
     //Pass a data cube into extract_rules method to compute support, confidence and lift from the data cube
     dt.extract_rules(rules,items_hashtable,itemsets_hashtable,decision_tree_file_name,meaningful_datacube_file_name,hash_attributes_col_nos,target_variable,ar);     
     }
     catch(IOException e)
     {
      e.printStackTrace();
      System.err.println("IOException thrown by extract_rules method.");
      System.exit(1);
     }

     try
     {
      ar.rank_rules("lift",association_rules_file_name,"results_file_exist","=>");
     }
     catch(IOException e)
     {
      System.err.println("IOException thrown by rank_rules method.");
      System.exit(1);
     }    
     try
     {
      AssociationRules.xml_description(rules,items_hashtable,itemsets_hashtable,data_matrix,"C4.5 Decision Trees Algorithm","delete_missing_values="+delete_missing_values+" "+option+"="+percentage_or_fold,xml_desc_file_name);
     }
     catch(IOException e)
     {
      e.printStackTrace();
      System.exit(1);
     }
     //delete temporary files
    
     cmd="rm "+datacube_no_missing_file_name+" "+meaningful_datacube_file_name+" "+data_matrix_arff_file_name+" "+results_file_temp;
     pb.command("bash", "-c", cmd);
     p = pb.start();
     p.waitFor();
     System.exit(0);  
    }
    else
    {
     //no decision tree is built. delete temporary files
      
     cmd="rm "+datacube_no_missing_file_name+" "+meaningful_datacube_file_name+" "+data_matrix_arff_file_name+" "+results_file_temp;
     pb.command("bash", "-c", cmd);
     p = pb.start();
     p.waitFor();
     System.exit(4);
   }   
  }
/*
 void display_file(String filename, int no_of_lines)
 { 
    BufferedReader in=null;
    String line;

    try {
     in = new BufferedReader(new FileReader(filename));
    }
    catch (FileNotFoundException e)
    {
     System.err.println(filename+" is not found.");
     System.exit(1);
    }
    try {
      line = in.readLine();
      for(int i=0; i < no_of_lines; i++)
      {
       System.out.println(line);
       line = in.readLine();
      }
      in.close();
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.exit(1);
    }
 }

 void display_file(String filename)
 {
    BufferedReader in=null;
    String line;
    
    try {
         in=new BufferedReader(new FileReader(filename));
    }
    catch (FileNotFoundException e)
    {
     System.err.println(filename+" is not found.");
     System.exit(1);
    }
    try {
         line = in.readLine();
         while(line!=null)
         {
     	   System.out.println(line);
           line = in.readLine();
         }
         in.close();
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.exit(1);
    }
 }
*/
 boolean checkFileExist(String file)
 { 
    File f;
    try
    {
       f = new File(file);
       if(!f.exists())
       { 
        System.err.println(file+" does not exist. Weka failed to output results to the file.");
	return false;
       }
    }
    catch(Exception e){
       e.printStackTrace();
       System.exit(1);
    }
    return true; 
 }

 boolean oneClassDataSet(String data_matrix_arff_file_name)
 {
   String [][] data_matrix=null;
   String c;
   HashSet<String> classes;

    try
    {
     data_matrix = DataMatrix.load_data_into_array(data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.err.println("IOException thrown by load_data_into_matrix method called by oneclassDataSet.");
     System.exit(1);
    }
    classes = new HashSet<String>();
    for(int i=0; i < data_matrix.length; i++)
    {
     c = data_matrix[i][data_matrix[0].length-1];
     classes.add(c);
    }
    if(classes.size()>1)
     return false;
    else if(classes.size()<1)
    {
    	System.out.println("number of classes: "+classes.size());
    	return false;
    }
    else
     return true;     
 }

 boolean process_results_file(String target_variable, String results_file_name, String decision_tree_file_name, String association_rules_file_name, String classifier, String association_rules_file_exist) throws IOException
 {//get the tree from weka output file and extract association rules from the tree
  BufferedReader in; 
  FileWriter out;
  FileWriter out2;
  String line;
  String first_condition=null;
  String middle_condition=null;
  String last_condition_and_RHS=null;
  String only_one_condition_and_RHS = null;
  Pattern p1;
  Pattern p2;
  Pattern p3;

  Matcher m1;
  Matcher m2;
  Matcher m3;

  boolean tree_built=true;

  p1=Pattern.compile("^Correctly Classified Instances\\s+[0-9]+\\s*([0-9.]+)\\s+%$");//matches "Correctly Classified Instances       14           60.8696% "  
  p2=Pattern.compile("^Number of Leaves\\s+:\\s+1$");//no decision tree generated
  p3=Pattern.compile("^=== Confusion Matrix ===$");//confusion matrix

  in=new BufferedReader(new FileReader(results_file_name));
  out=new FileWriter(decision_tree_file_name);
  if(association_rules_file_exist.equals("results_file_exist"))
     out2=new FileWriter(association_rules_file_name,true);
  else
     out2=new FileWriter(association_rules_file_name);
  out.write(classifier+":\n\n"); 
  line = in.readLine();
  //get the decision tree branches and write them into a file
  while(line!=null)
  {
    m3 = p3.matcher(line);
   if(m3.matches())//confusiom matrix
   {
    line=in.readLine();
    m1=p1.matcher(line);
    while(m1.matches()==false)
    {
     line=in.readLine();
     m1=p1.matcher(line);
    }
    out.write("\n\n target variable: "+target_variable+"\n\n classification accuracy of "+classifier+": "+m1.group(1)+"%\n\n");
    out2.write("\n\n target variable: "+target_variable+"\n\n classification accuracy of "+classifier+": "+m1.group(1)+"%\n\n");
    break;
   }
   else
   {
    first_condition=get_1st_condition(line);
    if(first_condition!=null) // check if line is a 1st condition e.g. a21 <= 16.76
    {
      //System.out.println("line is first condition: "+line);
      out.write(line+"\n");
    }
    else 
    {
     only_one_condition_and_RHS = get_only_one_condition_and_consequent(line);
     if(only_one_condition_and_RHS!=null)
     {
	//System.out.println("line is only one condition and RHS:"+line);
        out.write(line+"\n");
     }
     else
     {	   
       last_condition_and_RHS=get_last_condition_and_consequent(line);
       if(last_condition_and_RHS!=null) //check if line contains a rule condition and consequent e.g. a28 <= 0.1108: 1 (100.0)
	{
         //System.out.println("line is last_condition_and_RHS");         
	 out.write(line+"\n");
	}
       else
       {
        middle_condition=get_middle_condition(line);
        if(middle_condition!=null)    //check if line is a middle condition e.g. |   a28 > 0.1108
      	{
	 //System.out.println("line is middle condition");  
	 out.write(line+"\n");
	}
        else
        {
	  m2=p2.matcher(line);
          if(m2.matches())
	  {
       out.write("A decision tree can not be built using the input data cube.\nThe input data cube is unsuitable for building a decision tree.\n");
	   out2.write("A decision tree can not be built using the input data cube.\nThe input data cube is unsuitable for building a decision tree.\n");
	   tree_built=false;
           break;
	  }
        }
       }
     }
    }
   }
   line=in.readLine();
 }
 in.close();
 out.close();
 out2.close();
 return tree_built;
 }

void extract_rules(LinkedList<LinkedList<String>> rules,HashMap<String,String> items_hashtable, HashMap<LinkedList<String>,String> itemsets_hashtable, String decision_tree_file_name, String datacube_file_name, HashMap<String,Integer> hash_attributes_col_nos, String class_attr, AssociationRules ar) throws IOException
 {
 BufferedReader in; 
 LinkedList<String> rule;  //store the items of rule only e.g. rule[0]='a21 <= 16.76' rule[1]='a28 > 0.1108' rule[2]='a20 <= 0.004492' rule[3]='a7 <= 0.09061'
 LinkedList<String> rule2;//stores the items of rule, support, confidence and lift
 String line;
 String current_line;
 String last_condition_and_RHS = null;
 //String only_one_condition_and_RHS;
 String [][] datacube=null;
 float total_number_of_instances;

 LinkedList<String> rule_elements_and_next_line_and_rule_case=null;//store rule elements, next line and rule case
 String rule_case=null; /*rule case 1: this line contains the 1st condition of a rule e.g. a21 <= 16.76
			          rule case 2: this line contains a rule which has only one condition e.g. a28 <= 0.1108: 1 (100.0)
			          rule case 3: this line contains the last condition of a rule and the RHS of the rule e.g. a28 <= 0.1108: 1 (100.0)
			          rule case 4: this line contains a middle condition of a rule e.g. |   a28 > 0.1108 */

 rule = new LinkedList<String>();
 
 in=new BufferedReader(new FileReader(decision_tree_file_name));
 line = in.readLine();
 try
 {
  datacube = DataCube.load_data_cube_into_array(datacube_file_name);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(1);
 }

 total_number_of_instances = DataCube.get_total_number_of_instances(datacube);
 //System.out.println(total_number_of_instances);

 while(line!=null)
 {	
	  current_line = line;
          try
	  {
           rule_elements_and_next_line_and_rule_case = extract_elements_of_rule(in, line, rule);
          }
	  catch(IOException e)
	  {
	   e.printStackTrace();
	   System.exit(1);
	  }
	  if(rule_elements_and_next_line_and_rule_case.size()>1)//this line is a rule case
	  {
	    rule_case = rule_elements_and_next_line_and_rule_case.removeLast();
            line = rule_elements_and_next_line_and_rule_case.removeLast();
	    rule = rule_elements_and_next_line_and_rule_case;
            if(rule_case.equals("last_condition_and_RHS"))//rule case is last condition and RHS
	    {	      
	      last_condition_and_RHS = get_last_condition_and_consequent(current_line);
	      rule2 = (LinkedList<String>) rule.clone();
	      rule2.removeLast();
	      rule2.removeLast();
	      rule2 = support_confidence_lift(rule2,last_condition_and_RHS,datacube,total_number_of_instances,hash_attributes_col_nos,class_attr);   
              rule2 = add_items_to_items_hashtable_add_itemsets_to_itemsets_hashtable_add_rule_to_rules(rules,items_hashtable,itemsets_hashtable,class_attr,rule2);
	      //check if next line Control (110.0) also contains a last condition and a consequent
	      //if so compute support confidence and lift of rule
	      if(line != null)//next line is not end of file
	      {
	       last_condition_and_consequent(line, rule);	             
	      }
 	    }
	    else if(rule_case.equals("only_one_condition_and_RHS"))
	         {		  
	 	  //only_one_condition_and_RHS = get_only_one_condition_and_consequent(current_line);
	 	  get_only_one_condition_and_consequent(current_line);
	 	  rule2 = (LinkedList<String>) rule.clone();
		  rule2 = support_confidence_lift_of_single_condition_rule(rule2,datacube,total_number_of_instances,hash_attributes_col_nos,class_attr);   
		  //System.out.println(rule2);
        	  rule2 = add_items_to_items_hashtable_add_itemsets_to_itemsets_hashtable_add_rule_to_rules(rules,items_hashtable,itemsets_hashtable,class_attr,rule2);
		 }      	  
	    else //rule case is a first condition of rule or a middle condition of rule, just pass the rule and next line to the next iteration of while loop 
	    {}
          }
	  else //this line is not a rule case, so rule_elements_and_next_line_and_rule_case contains only one element: the next line; get the next line
	  {
  	    line = rule_elements_and_next_line_and_rule_case.get(0);
	  }          
  }//while
  in.close();
  //System.out.println(rules);
  ar.rules = rules;//add rules to AssociationRules object
 }

LinkedList<String> extract_elements_of_rule (BufferedReader in, String line, LinkedList<String> rule)
 throws IOException { 
     //extract elements of a rule from decision tree file
       LinkedList<String> rule_elements_and_next_line_and_rule_case;
       String rule_case=null;
	   String first_condition;
	   String only_one_condition_and_RHS;
	   String last_condition_and_RHS;
	   String middle_condition;
	   int number_of_conditions_before_middle_condition = 0;
	   String [] tokens;
           int last_index=0;
           //System.out.print(line);
           
	   first_condition = get_1st_condition(line); 
	   if(first_condition!=null) // check if line is a 1st condition e.g. a21 <= 16.76
	   {
	     rule.clear(); //remove the items of previous rule from the linkedlist
	     rule.add(first_condition);
	     //System.out.println(" matches first condition; rule: "+rule);
	     line = in.readLine();
	     rule_case = "first_condition";
	     rule_elements_and_next_line_and_rule_case = (LinkedList<String>)rule.clone();
  	     rule_elements_and_next_line_and_rule_case.add(line);
  	     rule_elements_and_next_line_and_rule_case.add(rule_case);
  	     //return rule elements, the next line and rule case
  	     return rule_elements_and_next_line_and_rule_case;
	   }
	   else
	   {
	     only_one_condition_and_RHS = get_only_one_condition_and_consequent(line);
	     if(only_one_condition_and_RHS!=null)
	     {
              rule.clear();//remove the items of previous rule from the linkedlist
	      rule = create_rule(rule,only_one_condition_and_RHS);
	      //System.out.println(" matches only one condition and RHS; rule: "+rule);    
	      line = in.readLine();
	      rule_case = "only_one_condition_and_RHS";	
	      rule_elements_and_next_line_and_rule_case = (LinkedList<String>)rule.clone();
  	      rule_elements_and_next_line_and_rule_case.add(line);
  	      rule_elements_and_next_line_and_rule_case.add(rule_case);
  	      //return rule elements, the next line and rule case
  	      return rule_elements_and_next_line_and_rule_case;       
	     }
	     else
             {
	       last_condition_and_RHS = get_last_condition_and_consequent(line);
	      if(last_condition_and_RHS!=null)
	      { 
 	       rule = create_rule(rule, last_condition_and_RHS);
	       //System.out.println(" matches last condition and consequent; rule: "+rule); 	     
               line = in.readLine();
	       rule_case = "last_condition_and_RHS";
    	       rule_elements_and_next_line_and_rule_case = (LinkedList<String>)rule.clone();
  	       rule_elements_and_next_line_and_rule_case.add(line);
  	       rule_elements_and_next_line_and_rule_case.add(rule_case);
  	       //return rule elements, the next line and rule case
  	       return rule_elements_and_next_line_and_rule_case;
	      }
	      else
	      {
		middle_condition = get_middle_condition(line);
	        if(middle_condition!=null)//check if line is a middle condition e.g. |   a28 > 0.1108
	        {
 		  //System.out.println(" matches middle condition; rule: "+rule);
	 	  //*** remove conditions from the linkedlist from the end of the linkedlist until the number of elements of the linkedlist equals to number_of_conditions_before_middle_condition
                  number_of_conditions_before_middle_condition=0;
                  tokens = line.split("\\s+");//matches the spaces on this line
	          for(int i=0;i<tokens.length;i++)
	          {
	           //System.out.println(tokens[i]);
	           if(tokens[i].equals("|"))
	             number_of_conditions_before_middle_condition++;
	          }
	   	  //remove conditions from the linkedlist from the end of the linkedlist until the number of elements of the linkedlist equals to number_of_conditions_before_middle_condition
                  while(rule.size()>number_of_conditions_before_middle_condition)
	          {
                   last_index = rule.size()-1;
	           rule.remove(last_index);//remove last element of linkedlist
	           //System.out.println(rule);
	          }
                  //***
 		  rule.add(middle_condition);
		  rule_case = "middle_condition";
		  line = in.readLine();
	 	  rule_elements_and_next_line_and_rule_case = (LinkedList<String>)rule.clone();
  	          rule_elements_and_next_line_and_rule_case.add(line);
  	          rule_elements_and_next_line_and_rule_case.add(rule_case);
  	          //return rule elemetns, the next line and rule case
  	          return rule_elements_and_next_line_and_rule_case;
	        }
	        else//if this line is not a rule case, add the next line to an empty list and return the new list 
		{
		 line = in.readLine();
	    	 rule_elements_and_next_line_and_rule_case = new LinkedList<String>();
	    	 rule_elements_and_next_line_and_rule_case.add(line);
		 //return the list containing the next line
	    	 return rule_elements_and_next_line_and_rule_case;
	 	}//else	        
	      }//else
	     }//else    
            }//else  
 }

void last_condition_and_consequent(String line, LinkedList<String> rule)
{      //check if this line below a line containing a last condition and a consequent, also contains a last condition and a consequent
	// if so update rule linkedlist accordingly

	String [] tokens;
    int number_of_conditions_before_last_condition;
	int last_index=0;
	String last_condition_and_RHS;
    //LinkedList<String> rule_elements_and_line_and_rule_case;
    //String rule_case;

	//System.out.println("rule: "+rule);

	last_condition_and_RHS = get_last_condition_and_consequent(line);
	if(last_condition_and_RHS!=null)
	{
	   //rule_case = "last_condition_and_RHS";
 	   number_of_conditions_before_last_condition=0;
           tokens = line.split("\\s+");//matches the spaces on this line
	   for(int i=0;i<tokens.length;i++)
	   {
	    if(tokens[i].equals("|"))
	      number_of_conditions_before_last_condition++;
	   }

	   //remove conditions from the linkedlist from the end of the linkedlist until the number of elements of the linkedlist equals to number_of_conditions_before_this_condition
       while(rule.size()>number_of_conditions_before_last_condition)
	   {
         last_index = rule.size()-1;
	     rule.remove(last_index);//remove last element of linkedlist
	   }
 	}	
 }

LinkedList<String> create_rule(LinkedList<String> rule, String last_condition_and_RHS)
  {
   //input arguments: a linkedlist 'rule' consisting of all the conditions of the rule except the last condition of the rule,
   //		      concatenation of last condition of the rule, right hand side of the rule and support of the rule
   //return: the linkedlist 'rule' consisting of all the conditions and the consequent of the rule

      	   Pattern p1;
	   Pattern p2;
	   Pattern p3;

  	   Matcher m1;
	   Matcher m2;
	   Matcher m3;
           int n=0,n2=0;

	   StringTokenizer st=null;
	   String RHS=null;
  	   String RHS_support=null;

	   String variable=null;
	   String variable2=null;
	   String last_condition="";

  	   p1=Pattern.compile("^\\s*(.+)\\s*\\((.+)/.+\\)$");
           p2=Pattern.compile("^\\s*(.+)\\s*\\((.+)\\)$");
	   p3=Pattern.compile("^\\s*([\\w\\p{Punct}]+)\\s*[=<>]+\\s*[\\w\\p{Punct}]+\\s*$");//condition pattern
	 
	   //if last_condition_and_RHS contains ":"s then last_condition also contains ":"s, so extract last_condition from last_condition_and_RHS by getting all terms before the last ":"  
	   //e.g. last_condition_and_RHS is cingBreastCA:weight = 3: 0 (117.0/36.0)
	   //     last_condition is "cingBreastCA:weight = 3"
	   //	  RHS_support is "0 (117.0/36.0)"
	   st = new StringTokenizer(last_condition_and_RHS,":");
	   n=st.countTokens();
	   if(n>2)
	   {	   
	      n2=n-1;
	      for(int i=1;i<n2;i++)//merge together strings of both sides of ":"  
	      {
	        last_condition=last_condition+":"+st.nextToken();
	      }	   
	   }
	   else //last_condition_and_RHS contains a single ":"
	   {
	      last_condition = st.nextToken();
	      RHS_support = st.nextToken();
	   } 
	    
	   m1=p1.matcher(RHS_support);
	   m2=p2.matcher(RHS_support);
	  
	   if(m1.matches())
	   {
	   	RHS=m1.group(1);//add RHS to rule where RHS is consequent of rule
	   }
	   else if(m2.matches())
	   {
	    	RHS=m2.group(1);//add RHS to rule where RHS is consequent of rule
	   }
	   //check whether the last element of the linkedlist is the same variable as the current variable
	   //if so, remove the last element from the linkedlist before adding the last condition to the linkedlist
	   if(rule.size()>0)
	   {
	     m3=p3.matcher(last_condition); 
             if(m3.matches())
	     {
	      variable = m3.group(1);//get variable of the last condition
	     }

	     m3=p3.matcher((String)rule.getLast());//get the last element of the LinkedList and match it against pattern
	     if(m3.matches())
	     {
	      variable2 = m3.group(1);
             }

             if(variable.equals(variable2))//the last element is same as the variable of the last condition
             {
	      rule.removeLast();
	      rule.add(last_condition);
             }
             else
             {
	      rule.add(last_condition);
             }
	     rule.add(RHS);
	   }
	   else //linkedlist is empty, so add last condition and RHS to create a single condition rule
	   {
	    rule.add(last_condition);
	    rule.add(RHS);
	   }
	   return rule;
  }


LinkedList<String> add_items_to_items_hashtable_add_itemsets_to_itemsets_hashtable_add_rule_to_rules(LinkedList<LinkedList<String>> rules,HashMap<String,String> items_hashtable,HashMap<LinkedList<String>,String> itemsets_hashtable,String class_attr,LinkedList<String>rule) throws IOException
{

        String item=null;
        String itemset2="";
	Pattern p1;
	Matcher m1;
	
	p1=Pattern.compile("^\\s*=>\\s*$");//match implication symbol

        LinkedList<String> itemset = new LinkedList<String>();
	LinkedList<String> rule2 = new LinkedList<String>();
	int last_index = rule.size()-1;
        
        //add items to items_hashtable
        for(int i=0;i<last_index-3;i++)
        { 
          item = rule.get(i);
	  m1 = p1.matcher(item);
	  if(!m1.matches())
	  {
	   if(!items_hashtable.containsKey(item))
   	   {
      	    items_hashtable.put(item,Integer.toString(DecisionTrees.item_id));//add item to items_hashtable
      	    DecisionTrees.item_id++;
   	   }
           itemset.add(item);
   	   itemset2 = itemset2+item+" ";
	  }
        }
        rule2.add(itemset2);
        //add last item to items_hashtable
        item = class_attr+" = "+rule.get(last_index-3);
        if(!items_hashtable.containsKey(item))
        {
         items_hashtable.put(item,Integer.toString(DecisionTrees.item_id));//add item to items_hashtable
         DecisionTrees.item_id++;
        }
        //add LHS itemset to itemsets_hashtable
        if(!itemsets_hashtable.containsKey(itemset))
        {
          itemsets_hashtable.put(itemset,Integer.toString(DecisionTrees.itemset_id)); //put itemset in hashtable
          DecisionTrees.itemset_id++;
        }
        //add RHS itemset to itemsets_hashtable
        itemset = new LinkedList<String>();
        itemset.add(item);
        if(!itemsets_hashtable.containsKey(itemset))
        {
         itemsets_hashtable.put(itemset,Integer.toString(DecisionTrees.itemset_id)); //put itemset in hashtable
         DecisionTrees.itemset_id++;
        }
        itemset2=item;
        rule2.add(itemset2);
        rule2.add(rule.get(last_index-2));//add support
        rule2.add(rule.get(last_index-1));//add confidence
        rule2.add(rule.get(last_index));//add lift
        rules.add(rule2);
        
	return rule;
}    

LinkedList<String> support_confidence_lift_of_single_condition_rule(LinkedList<String> rule, String [][] datacube, float total_number_of_instances, HashMap<String,Integer> hash_attributes_col_nos, String class_attr)
{
  String LHS_item;
  String RHS_item;
  float supp=0;
  float conf=0;
  float lift=0;
  float rule_supp_count;
  float LHS_supp_count;
  float RHS_supp_count;
  float RHS_supp;
  DecimalFormat df;

  df = new DecimalFormat("###.##########");//10 decimal places
  df.setRoundingMode(RoundingMode.HALF_UP);//e.g. 0.25672 to rounded to 0.2567, 0.53255 is rounded to 0.5326

  LHS_item = rule.get(0);//e.g. rs0 = AC
  RHS_item = rule.get(1);//e.g. Control
  
  rule_supp_count = rule_support_count_of_single_condition_rule(rule,datacube,class_attr, hash_attributes_col_nos);

  LHS_supp_count = LHS_support_count_of_single_condition_rule(LHS_item,datacube,hash_attributes_col_nos); 

  RHS_supp_count = RHS_support_count(datacube,RHS_item,class_attr,hash_attributes_col_nos);
  RHS_supp = RHS_supp_count/total_number_of_instances;
  
  //support
  supp = rule_supp_count/total_number_of_instances;
  supp = Float.parseFloat(df.format(supp))*100f;

  //confidence
  if(rule_supp_count == 0f)
   conf = 0f;
  else if(LHS_supp_count == 0f)
   conf = 0f;
  else
  {
   conf = rule_supp_count/LHS_supp_count;
   if(conf == Float.POSITIVE_INFINITY)
    conf = 0f;
  }
  //lift
  if(conf == 0f)
   lift=0f;
  else if(RHS_supp == 0f)
   lift=0f;
  else 
  {
   lift = conf/RHS_supp;
   if(lift == Float.POSITIVE_INFINITY)
    lift=0f;
  }
  rule.set(1,"=>");
  rule.add(RHS_item);
  rule.add(Float.toString(supp));
  rule.add(Float.toString(conf));
  rule.add(Float.toString(lift));

  return rule;
}   

float rule_support_count_of_single_condition_rule(LinkedList<String> rule, String [][] datacube, String class_attr, HashMap<String,Integer> hash_attributes_col_nos)
{
 //input arguments: a linkedlist: 1st element is the condition of rule, 2nd element is the consequent of rule
 //		      2-D array containing a data cube
 //		      hash table of attributes and column indices
 //return: support count of rule

  Pattern p1;
  Matcher m1;
  String condition;
  String rule_attr=null;
  String rule_attr_val=null;
  int attr_col=0;
  int class_attr_col=0;
  String val=null;
  String class_val=null;
  float supp=0;

  p1 = Pattern.compile("^\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*[=<>]+\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*$");//condition pattern
  class_attr_col = hash_attributes_col_nos.get(class_attr);

  for(int i=0;i<datacube.length;i++)
  {
    condition=rule.get(0);
    m1 = p1.matcher(condition);
    if(m1.matches())
    {
     rule_attr=m1.group(1);
     rule_attr_val=m1.group(2);
     attr_col=hash_attributes_col_nos.get(rule_attr);
     val=datacube[i][attr_col];
     if(rule_attr_val.trim().equals(val))
     { 
      class_val = datacube[i][class_attr_col];  
      if((rule.get(1).trim()).equals(class_val))
          supp = supp + Float.parseFloat(datacube[i][datacube[0].length-1]);//add up the count in last column of data cube
     }
    }
    else
      System.err.println("condition does not match pattern in rule_support_count_of_single_condition_rule method");  
  }//for 

  return supp;
}

float LHS_support_count_of_single_condition_rule(String condition, String [][] datacube, HashMap<String,Integer> hash_attributes_col_nos)
{  //input arguments: LHS item i.e. the condition of rule
   //		      2-D array containing a data cube
   //		      hash table of attributes and column indices
   //return: support count of LHS item

   Pattern p1;
   Matcher m1;
   String rule_attr=null;
   String rule_attr_val=null;
   int attr_col=0;
   String val=null;
   float supp=0;

   p1 = Pattern.compile("^\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*[=<>]+\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*$");//condition pattern
  
  for(int i=0;i<datacube.length;i++)
  {
    m1 = p1.matcher(condition);
    if(m1.matches())
    {
     rule_attr=m1.group(1);
     rule_attr_val=m1.group(2);
     attr_col=hash_attributes_col_nos.get(rule_attr);
     val=datacube[i][attr_col];
     if(rule_attr_val.trim().equals(val))
        supp = supp + Float.parseFloat(datacube[i][datacube[0].length-1]);//add up the count in last column of data cube
    }
  }
  return supp;
}

LinkedList<String> support_confidence_lift(LinkedList<String> rule, String last_condition_and_RHS, String[][] datacube, float total_number_of_instances, HashMap<String,Integer> hash_attributes_col_nos, String class_attr)
 {
   //input arguments: a linkedlist 'rule' that consists of all conditions of a rule except last condition,
   //                 concatenation of the last condition of rule, the right hand side of rule and support count of rule
   //  		      data matrix
   //		      hash table where key: attribute, value: column no. of attribute
   // 		      class attribute	
   //return: a linkedlist 'rule' consisting of all conditions of the rule, consequent of rule, support, confidence and lift

  //elements of rule: rule[0]='a21 <= 16.76' rule[1]='a28 > 0.1108' rule[2]='a20 <= 0.004492' rule[3]='a7 <= 0.09061: 1 (6.0/1.0)'
  String last_condition="";
  String RHS_support;
  String RHS=null;
  float rule_supp_count=0;
  float LHS_supp_count=0f;
  float RHS_supp=0f;
  float conf=0f;
  //float class_attr_supp_count=0f;
  float lift=0f;
  String last_condition_RHS_support="";
  Pattern p1,p2;
  Matcher m1,m2;
  //int class_attr_col;
  float support=0;
  DecimalFormat df;
  float RHS_supp_count;

  df = new DecimalFormat("###.##########");//10 decimal places
  df.setRoundingMode(RoundingMode.HALF_UP);//e.g. 0.25672 to rounded to 0.2567, 0.53255 is rounded to 0.5326

  //format of last_condition_consequent: rule[3]='a7 <= 0.09061: 1 (6.0/1.0)
  //get last condition and consequent plus support of rule
  last_condition_RHS_support = get_last_condition_and_get_RHS_and_support(last_condition_and_RHS);
  last_condition = last_condition_RHS_support.split("#")[0];

  RHS_support = last_condition_RHS_support.split("#")[1];
  //System.out.println("last_condition: "+last_condition+" RHS_support: "+RHS_support); 
  //if rule contains a condition which conflicts with the new condition to be inserted e.g. rs0=1, rs0=5 are conflicting conditions.
  //then remove the condition from the rule before inserting the new condition into the rule 
  //System.out.println("before remove_conflict_condition: "+rule);
  
  rule = remove_conflict_condition(rule,last_condition);

  //System.out.println("after remove_conflict_condition: "+rule);

  p1=Pattern.compile("^\\s*(.+)\\s*\\((.+)/.+\\)$");//matches right hand side value and support e.g. Control (110.0/50.0)
  p2=Pattern.compile("^\\s*(.+)\\s*\\((.+)\\)$");//matches right hand side value and support e.g. Control (110.0)
  m1=p1.matcher(RHS_support);
  m2=p2.matcher(RHS_support);

  if(m1.matches())
  {
   RHS=m1.group(1);
   RHS.trim();
  }
  else if(m2.matches())
  {
   RHS=m2.group(1);
   RHS.trim();
  }

  rule_supp_count = rule_support_count(rule,last_condition,RHS,class_attr,datacube,hash_attributes_col_nos);
  
  //compute support count of LHS itemset
  LHS_supp_count = LHS_support_count(rule,last_condition,datacube,hash_attributes_col_nos); 

  //System.out.println("rule_supp_count: "+rule_supp_count+" LHS_supp_count: "+LHS_supp_count);

  if(rule_supp_count == 0f)
   conf = 0f;
  else if(LHS_supp_count == 0f)
   conf = 0f;
  else
  {
   conf = rule_supp_count/LHS_supp_count;
   if(conf == Float.POSITIVE_INFINITY)
    conf = 0f;
  }
  //support count of the right hand side itemset which consists of the class attribute value e.g.  diabetes
  RHS_supp_count = RHS_support_count(datacube,RHS,class_attr,hash_attributes_col_nos); 

  //support of A = support count (A)/(total no. of instances)
 
  RHS_supp = RHS_supp_count/total_number_of_instances;

  //lift = (confidence of A=>B)/ support of B
  if(conf == 0f)
   lift=0f;
  else if(RHS_supp == 0f)
   lift=0f;
  else 
  {
   lift = conf/RHS_supp;
   if(lift == Float.POSITIVE_INFINITY)
    lift=0f;
  }
  //add last condition, consequent, support of rule, confidence of rule, lift
  rule.add(last_condition);
  rule.add("=>");
  rule.add(RHS);
  support = rule_supp_count/total_number_of_instances;
  support = Float.parseFloat(df.format((double)support))*100f;//format support to specified decimal places and convert percentage
  rule.add(Float.toString(support));//add rule support
  rule.add(Float.toString(conf));
  rule.add(Float.toString(lift));

  return rule;
 }

LinkedList<String> remove_conflict_condition(LinkedList<String>rule, String condition)
{
 //input arguments: a linkedlist 'rule'
 //		    a condition
 //return: the 'rule' with the conflicted condition removed if any

 //check whether the linkedlist 'rule' contains a condition which has same variable as the variable of the input condition.
//if so, the condition is removed from the 'rule'

 int last_index=rule.size()-1;
 String rule_condition=null;
 Pattern p=Pattern.compile("^\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*[=<>]+\\s*[a-zA-Z_0-9\\p{Punct}]+\\s*$");//condition pattern
 Matcher m = p.matcher(condition); 
 String variable=null;
 String rule_variable=null;

 if(m.matches())
 {
  variable = m.group(1);//get variable of the condition
 }

 for(int i=0; i<=last_index; i++)
 {
   rule_condition = (String)rule.get(i);
   m = p.matcher(rule_condition);
   if(m.matches())
   {
    rule_variable = m.group(1);//get variable of the rule
   }  
   if(rule_variable.equals(variable))
   {
    rule.remove(i);//delete the conflict condition from rule
    break;
   }
 }
 return rule;
}

String get_last_condition_and_get_RHS_and_support(String last_condition_and_RHS)
 {//remove any ':'s from a string which last condition of rule, right hand side of rule and support count of rule
  //and return a '#' concatenation of the last condition with concatenation of right hand side of rule and support count
  //
  //input argument: a string which contains the last condition of rule, right hand side of rule and support count e.g. 
  //               'rs1 = CG : Control (36/0)'
  //return: a '#' concatenation of the last condition with concatenation of right hand side of rule and support count

  StringTokenizer st;
  int n,n2;
  String last_condition="";
  String RHS_support;

  st = new StringTokenizer(last_condition_and_RHS,":");
  n=st.countTokens();
  //check if the last_condition_and_RHS contains many ':'s e.g. cingBreastCA:weight = 3: 0 (117.0/36.0)
  //if so, remove the ':' from it.
  if(n>2)
  {
   n2=n-1;
   for(int i=1;i<n2;i++)//merge together strings of both sides of ":"  
   {
     last_condition=last_condition+st.nextToken()+":";
   }
   last_condition=last_condition+st.nextToken();
   RHS_support=st.nextToken();
  }
  else//last_condition_and_RHS contains one ":" e.g. a7 <= 0.09061: 1 (6.0/1.0)
  {
   last_condition=st.nextToken();
   RHS_support=st.nextToken();
  }
  return last_condition+"#"+RHS_support;
 }

 float rule_support_count(LinkedList<String> rule, String last_condition, String RHS_itemset, String class_attr, String [][] datacube, HashMap<String,Integer> hash_attributes_col_nos)
 { //calculate support count of a rule using data cube
   //input arguments: a linkedlist 'rule' consisting of all the conditions of a rule except the last condition of rule
   //		      last condition
   //		      RHS itemset which consists of a class attribute value
   //		      2-D array containing a data cube
   //		      hash table where key=attribute, value=column index of attribute in data cube
   //return: support count of the complete rule

   float supp=0f;
   int attr_col;
   Pattern p1;
   Matcher m1;
   String val=null;
   String rule_attr=null;
   String rule_attr_val=null;
   String condition=null; 
   boolean all_conditions_so_far_match_all_values_of_this_instance_so_far;
   int class_attr_col;
   String class_val;

   p1 = Pattern.compile("^\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*[=<>]+\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*$");//matches a condition i.e. an item
   class_attr_col = hash_attributes_col_nos.get(class_attr);
   
   for(int i=0;i<datacube.length;i++)
   { //match the LHS itemset consisting of conditions of the rule with this instance
     all_conditions_so_far_match_all_values_of_this_instance_so_far = true; 
     for(int j=0;j<rule.size();j++)
     {
       condition = rule.get(j);    
       m1 = p1.matcher(condition);
       if(m1.matches())
       {
        rule_attr=m1.group(1);
        rule_attr_val=m1.group(2);
        attr_col=hash_attributes_col_nos.get(rule_attr);
        val=datacube[i][attr_col];
       } 
       if(!rule_attr_val.equals(val))
       {
        all_conditions_so_far_match_all_values_of_this_instance_so_far = false;  
        break; //this rule does not match this instance so stop matching next condition of this rule against next value of this instance
       }
     }//for 
     //match the last condition of rule with this instance
     if(all_conditions_so_far_match_all_values_of_this_instance_so_far)
     {
       m1 = p1.matcher(last_condition);
       if(m1.matches())
       {
        rule_attr = m1.group(1);
        rule_attr_val = m1.group(2);
        attr_col = hash_attributes_col_nos.get(rule_attr);
        val = datacube[i][attr_col];
       }
       else
       {
        System.out.println("condition pattern not matched");
	System.exit(-1);
       }
       if(rule_attr_val.equals(val))//if the last condition matches this instance 
       {
	//match RHS itemset with this instance
        class_val = datacube[i][class_attr_col];  
        if((RHS_itemset.trim()).equals(class_val))
        {  
         //System.out.println(datacube[i][datacube[0].length-1]);
         supp = supp + Float.parseFloat(datacube[i][datacube[0].length-1]);
        }
       }
     }//if     
   }//for
   return supp;
 }

 float RHS_support_count(String[][]datacube, String RHS_itemset, String class_attr, HashMap<String,Integer> hash_attributes_col_nos)
 {   
  //calculate support count of the rhs itemset using data cube
  //input arguments: data cube, 
  //                 RHS itemset which consists of a class attribute value, 
  //		     hash table of attributes column indices 
  //
  //return: support count of the RHS itemset

  int class_attr_col;
  String class_val=null;
  float RHS_supp=0f;

  class_attr_col = hash_attributes_col_nos.get(class_attr);

  for(int i=0; i<datacube.length; i++)
  {
   class_val = datacube[i][class_attr_col];  
   if((RHS_itemset.trim()).equals(class_val))
   {  
        RHS_supp = RHS_supp + Float.parseFloat(datacube[i][datacube[0].length-1]);//add the count of subjects in the last column of data cube
   } 
 }

  return RHS_supp;
 }


 float LHS_support_count(LinkedList<String> rule,String last_condition,String [][] datacube,HashMap<String,Integer> hash_attributes_col_nos)
 {
   //calculate support count of the lhs itemset using data cube
   //input arguments: linkedlist 'rule' consisting of all the conditions of a rule except the last condition of rule
   //		      last condition
   //		      2-D array containing a data cube
   //		      hash table where key=attribute, value=column index of attribute in data cube
   //return: support count of the lhs itemset

   float supp=0f;
   int attr_col;
   Pattern p1;
   Matcher m1;
   String val=null;
   String rule_attr=null;
   String rule_attr_val=null;
   String condition=null; 
   boolean all_conditions_so_far_match_all_values_of_this_instance_so_far;
  
   p1 = Pattern.compile("^\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*[=<>]+\\s*([a-zA-Z_0-9\\p{Punct}]+)\\s*$");//matches a condition i.e. an item

   for(int i=0;i<datacube.length;i++)
   {
     all_conditions_so_far_match_all_values_of_this_instance_so_far = true; 
     for(int j=0;j<rule.size();j++)
     {
       condition = rule.get(j);    
       m1 = p1.matcher(condition);
       if(m1.matches())
       {
        rule_attr=m1.group(1);
        rule_attr_val=m1.group(2);
        attr_col=hash_attributes_col_nos.get(rule_attr);
        val=datacube[i][attr_col];
       } 
       if(!rule_attr_val.equals(val))
       {
        all_conditions_so_far_match_all_values_of_this_instance_so_far = false;  
        break; //this rule does not match this instance so stop matching next condition of this rule against next value of this instance
       }
     }//for 
     //match the last condition of rule with this instance
     if(all_conditions_so_far_match_all_values_of_this_instance_so_far)
     {
       m1 = p1.matcher(last_condition);
       if(m1.matches())
       {
        rule_attr = m1.group(1);
        rule_attr_val = m1.group(2);
        attr_col = hash_attributes_col_nos.get(rule_attr);
        val = datacube[i][attr_col];
	//System.out.println("val: "+val+" rule_attr: "+rule_attr+" hash_attributes_col_nos: "+hash_attributes_col_nos);
       }
       else
       {
        System.out.println("condition pattern not matched");
	System.exit(-1);
       }
       if((rule_attr_val.trim()).equals(val)) 
       {
        //System.out.println(datacube[i][datacube[0].length-1]);
        supp = supp + Float.parseFloat(datacube[i][datacube[0].length-1]);
       }
     }//if     
   }//for
  return supp;
 }

 String get_1st_condition(String line)
 {
    //check if line is the 1st condition of a rule

    Pattern p1;
    Matcher m1;
    
    p1 = Pattern.compile("^([^\\|]+[=<>]+[^=\\(\\):]+)$");

    m1 = p1.matcher(line);

    if(m1.matches())
    {
      return m1.group(1); 
    }
    else
    {
     return null;
    }
 }

String get_middle_condition(String line)
 {
    //check if line is a middle condition of a rule
  
    Pattern p1;
    Matcher m1;
    
    p1 = Pattern.compile("^[\\|\\s]+(.+[=<>]+[^:]+)$");
   
    m1 = p1.matcher(line);

    if(m1.matches())
    {
      return m1.group(1); 
    }
    else
    {
     return null;
    }
 }

 String get_last_condition_and_consequent(String line)
 {
  //check if line contains the last condition of a rule and the consequent of the rule e.g. rs0 = CC: 1 (100.0)

    Pattern p1;
    Matcher m1;

    p1 = Pattern.compile("^[\\|\\s]+(.+[=<>]+[^:]+[:].+\\([0-9\\./]+\\))$");  

    m1 = p1.matcher(line);

    if(m1.matches())
    {
      return m1.group(1); 
    }
    else
    {
     return null;
    }
 }

 String get_only_one_condition_and_consequent(String line)
 {
  //check if the line is a single condition rule which consists of one condition e.g. rs0 = AA: 1 (100.0)

    Pattern p1;
    Matcher m1;    
    
    p1 = Pattern.compile("^([^\\|\\s].+[=<>]+[^:]+[:].+\\([0-9\\./]+\\))$"); 
    m1 = p1.matcher(line);

    if(m1.matches())
    {
      return m1.group(1); 
    }
    else
    {
     return null;
    }
 }
}


