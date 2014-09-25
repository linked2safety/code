/* RandomForest.java - uses the random forest algorithm to build a random forest using percentage split or cross validation and extracts association rules from the random forest

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
import java.lang.Float;

public class RandomForest
{
 public final static void main(String[]args) throws Exception
 {
    String datacube_file_name=null;
    String categories_meaning_file_name="";
    String meaningful_datacube_file_name;
    String class_index=null;
    String no_of_trees=null;
    String percentage_or_fold=null;
    String weka_class_path=null;
    String cmd=null;
    String data_matrix_arff_file_name;
    String results_file_temp;
    Random randomGenerator;
    ProcessBuilder pb;
    Process p=null;   
    String target_variable=null;
    HashMap<String,Integer> hash_attributes_col_nos=null;
    String data_matrix[][]=null;	
    String xml_desc_file_name=null;
    String option = null;
    String random_forest_file_name="";
    String association_rules_file_name="";
    AlgorithmParameters ap;
    boolean random_forest_built=false;
    AssociationRules ar = null;
    LinkedList<LinkedList<String>> rules;//all association rules
    HashMap<String,String> items_hashtable; //key: item value: id
    HashMap<LinkedList<String>,String> itemsets_hashtable;//key: itemset, value: id
    String delete_missing_values=null; //0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
    String datacube_no_missing_file_name=null;
    int number_of_instances_removed=0;
    String missing_value=null;
    DecisionTrees dt;
    RandomForest RF;
    FileWriter out=null;

    RF = new RandomForest();
    
    if(args.length == 10)//target variable not specified, the variable with the largest index of data set is the target variable
    {   
     option = args[0]; //train_test_split or cross_validation
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2];
     categories_meaning_file_name = args[3];
     no_of_trees = args[4]; 
     percentage_or_fold = args[5];//split percentage or cross validation folds
     weka_class_path=args[6];
     random_forest_file_name = args[7];
     association_rules_file_name = args[8];
     xml_desc_file_name = args[9];
     ap = new AlgorithmParameters(datacube_file_name);
     class_index = String.valueOf(ap.get_index_of_variable_with_largest_index());//class_index starts from 1
    }
    else if(args.length == 11)//target variable is specified. valid formats: 3:_diabetes or diabetes
    {
     option = args[0]; //train_test_split or cross_validation
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2];
     categories_meaning_file_name = args[3]; 
     target_variable = args[4];  // target variable: valid formats: 3:_diabetes or diabetes 
     no_of_trees = args[5]; 
     percentage_or_fold = args[6];//split percentage or cross validation folds
     weka_class_path = args[7];
     random_forest_file_name = args[8];
     association_rules_file_name = args[9];
     xml_desc_file_name = args[10];
     //get index of target variable
     //valid formats: 3:_diabetes or diabetes
     ap = new AlgorithmParameters(datacube_file_name);
     class_index = ap.get_index_of_target_variable(target_variable);//class_index starts from 1
    }
    else //wrong number of input arguments, outor message
    {
     System.out.println("Wrong number of input arguments.\nUsage: java RandomForest <train_test_split or cross_validation> <delete_missing_values (0 or 1)> <data cube file name> <categories meaning file name> <target variable (optional)> <no. of trees> <split percentage or number of folds> <weka-3-7-7.jar path> <random forest file name> <association rules file name> <random forest XML file>\n");
     System.exit(1);
    }

    randomGenerator = new Random();
    data_matrix_arff_file_name = "data_matrix_arff_file"+randomGenerator.nextInt(1000000)+".arff";
    results_file_temp = "results_file_temp"+Integer.toString(randomGenerator.nextInt(9000000));
    meaningful_datacube_file_name = "meaningful_datacube_file"+Integer.toString(randomGenerator.nextInt(9000000));
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
     System.exit(1);
    }

    try
    {
     DataCube.transform_data_cube_to_data_matrix(datacube_no_missing_file_name,categories_meaning_file_name,meaningful_datacube_file_name,data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.out.println("IOException thrown by data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(1);
    }     

    dt = new DecisionTrees();
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
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.classifiers.trees.RandomForest -print -t "+data_matrix_arff_file_name+" -c "+class_index+" -I "+no_of_trees+" -split-percentage "+percentage_or_fold+" > "+results_file_temp;
    }
    else if(option.equals("cross_validation"))
    {
     cmd = "java -Xmx1024m -cp "+weka_class_path+" weka.classifiers.trees.RandomForest -print -t "+data_matrix_arff_file_name+" -c "+class_index+" -I "+no_of_trees+" -x "+percentage_or_fold+" > "+results_file_temp;
    }
    else
    {
     System.out.println("RandomForest option is wrong.\n Valid options: train_test_split or cross_validation\nUsage: java RandomForest <train_test_split or cross_validation> <delete_missing_values (0 or 1)> <data cube file name> <categories meaning file name> <target variable (optional)> <no. of trees> <split percentage or number of folds> <weka-3-7-7.jar path> <random forest file name> <association rules file name> <random forest XML file>");
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
    
    
    try
    {
     random_forest_built = RF.process_results_file(target_variable,results_file_temp,random_forest_file_name,association_rules_file_name,"Random Forest","results_file_exist");
    }
    catch (IOException e)
    {
     e.printStackTrace();
     System.out.println("IOException thrown by process_results_file method.");
     System.exit(1);
    } 
    
    hash_attributes_col_nos = DataMatrix.find_attrs_col_nos(data_matrix_arff_file_name);
    try
    {
     data_matrix = DataMatrix.load_data_into_array(data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.out.println("IOException thrown by load_data_into_matrix method.");
     System.exit(1);
    }

    if(random_forest_built)
    {
     rules = new LinkedList<LinkedList<String>>();//all association rules
     items_hashtable = new HashMap<String,String>();//all items of all rules
     itemsets_hashtable = new HashMap<LinkedList<String>,String>();//all itemsets of all rules
         
     ar = new AssociationRules();

     try
     {
      //Pass a data cube into extract_rules method to compute support, confidence and lift from data cube
      dt.extract_rules(rules,items_hashtable,itemsets_hashtable,random_forest_file_name,meaningful_datacube_file_name,hash_attributes_col_nos,target_variable,ar);     

     }
     catch(IOException e)
     {
      System.out.println("IOException thrown by extract_rules method.");
     }
     try
     {
      ar.rank_rules("lift",association_rules_file_name,"results_file_exist","=>");
     }
     catch(IOException e)
     {
      System.out.println("IOException thrown by rank_rules method.");
      System.exit(-1);
     }      
     try
     {
      AssociationRules.xml_description(rules,items_hashtable,itemsets_hashtable,data_matrix,"Random Forest Algorithm","delete_missing_values="+delete_missing_values+" no_of_trees="+no_of_trees+" "+option+"="+percentage_or_fold,xml_desc_file_name);
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
     if(Float.parseFloat(no_of_trees) == 1.0)
       System.exit(9);
     else
       System.exit(0);
    }
    else
    {
    //A random forest is not built. Delete temporary files
    
     cmd="rm "+datacube_no_missing_file_name+" "+meaningful_datacube_file_name+" "+data_matrix_arff_file_name+" "+results_file_temp;
     pb.command("bash", "-c", cmd);
     p = pb.start();
     p.waitFor();
     System.exit(4);
    }
  }

  boolean process_results_file(String target_variable, String results_file_name, String random_forest_file_name, String association_rules_file_name, String classifier, String association_rules_file_exist) throws IOException
 {//get the forest from the weka output file and extract association rules from the forest 
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
 
  boolean random_forest_built=true;
  DecisionTrees DT=null;

  p1=Pattern.compile("^Correctly Classified Instances\\s+[0-9]+\\s*([0-9.]+)\\s+%$");//matches "Correctly Classified Instances       14           60.8696% "  
  p2=Pattern.compile("^Number of Leaves\\s+:\\s+1$");//no random forest is built
  p3=Pattern.compile("^=== Confusion Matrix ===$");//confusion matrix
  
  in=new BufferedReader(new FileReader(results_file_name));
  out=new FileWriter(random_forest_file_name);
  out.write(classifier+":\n\n");
  if(association_rules_file_exist.equals("results_file_exist"))
     out2=new FileWriter(association_rules_file_name,true);
  else
     out2=new FileWriter(association_rules_file_name);
  
  DT = new DecisionTrees();
  line = in.readLine();
 //get the decision trees' branches and write the branches into a file
  while(line!=null)
  {
   if(line.equals("RandomTree"))
   {
    out.write("\n\ndecision tree\n\n");
   }
   else
   {
    m3 = p3.matcher(line);
    if(m3.matches())//confusion matrix
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
     first_condition=DT.get_1st_condition(line);
     if(first_condition!=null) // check if line is a 1st condition e.g. a21 <= 16.76
       out.write(line+"\n");
     else 
     {
      only_one_condition_and_RHS = DT.get_only_one_condition_and_consequent(line);
      if(only_one_condition_and_RHS!=null)
        out.write(line+"\n");  
      else
      {
        last_condition_and_RHS=DT.get_last_condition_and_consequent(line);
        if(last_condition_and_RHS!=null) //check if line contains a rule condition and consequent e.g. a28 <= 0.1108: 1 (100.0)
           out.write(line+"\n");
        else
        {
         middle_condition=DT.get_middle_condition(line);
         if(middle_condition!=null)    //check if line is a middle condition e.g. |   a28 > 0.1108
      	   out.write(line+"\n");
         else
         {
	      m2=p2.matcher(line);
          if(m2.matches())
	      {
        	  out.write("A random forest can not be built using the input data cube.\nThe input data cube is unsuitable for building random forest.\n");
        	  out2.write("A random forest can not be built using the input data cube.\nThe input data cube is unsuitable for building random forest.\n");
        	  random_forest_built=false;
        	  break;
	      }
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
 return random_forest_built;
 }
}
