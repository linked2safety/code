/* 
   ReduceDataInformationGainFeatureSelection.java - reduces dimensionality of a data cube using information gain feature selection algorithm

   Copyright 2014 The University of Manchester tiand@cs.man.ac.uk

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   This component links to weka.jar of weka-3-6-7, DataCube.java, DataMatrix.java, AlgorithmParameters.java and DecisionTrees.java which are under GNU GPL version 3. You may download the source code and associated license at: http://sourceforge.net/projects/weka/ and https://github.com/linked2safety/code under the DataMining directory.

   The source files are available at: https://github.com/linked2safety/code
*/
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.System;
import java.lang.Runtime;
import java.lang.ProcessBuilder;

public class ReduceDataInformationGainFeatureSelection
{    
 public final static void main(String[]args) throws Exception
 {
    String line;
    String datacube_file_name="";
    String no_of_features="";
    String reduced_data_cube_file_name="";
    String class_index="";
    String weka_class_path="";
    String cmd;
    String data_matrix_arff_file_name;
    String reduced_data_arff_file_name;
    Random randomGenerator;
    ProcessBuilder pb;
    Process p=null;
    String xml_desc_file_name=null;
    String algorithm_params_setting=null;
    String target_variable=null;
    AlgorithmParameters ap = null;
    int largest_index;
    int max_number_of_features;
    String delete_missing_values=null; //0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
    String datacube_no_missing_file_name=null;
    int number_of_instances_removed=0;
    String missing_value=null;
    String categories_meaning_file_name="";
    DecisionTrees dt=null;
    FileWriter out=null;

    if(args.length == 7)//target variable not specified, the variable with the largest index of data set is the target variable
    {
     categories_meaning_file_name = args[0];
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2]; 
     no_of_features=args[3];
     weka_class_path=args[4];
     reduced_data_cube_file_name = args[5];
     xml_desc_file_name = args[6];
     ap = new AlgorithmParameters(datacube_file_name);
     largest_index = ap.get_index_of_variable_with_largest_index();
     max_number_of_features = largest_index - 1;
     if(Integer.parseInt(no_of_features) > max_number_of_features || Integer.parseInt(no_of_features) < 1)
     {
      System.err.println("The number of features to select is invalid.\nThe number of variables to select must be >= 1 and <= "+max_number_of_features); 
      System.exit(1);
     }
     else
     {
      class_index = String.valueOf(largest_index);//class_index starts from 1
      //System.out.println("class_index: "+class_index);
     }
    }
    else if(args.length == 8)//target variable is specified. valid formats: 3:_diabetes or diabetes
    {
     categories_meaning_file_name = args[0];
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2]; 
     target_variable = args[3];  // target variable: valid formats: 3:_diabetes or diabetes 
     no_of_features=args[4];
     weka_class_path=args[5];
     reduced_data_cube_file_name = args[6];  
     xml_desc_file_name = args[7];
     //get index of target variable
     //valid formats: 3:_diabetes or diabetes
     ap = new AlgorithmParameters(datacube_file_name);
     largest_index = ap.get_index_of_variable_with_largest_index();
     max_number_of_features = largest_index - 1;
     if(Integer.parseInt(no_of_features) > max_number_of_features || Integer.parseInt(no_of_features) < 1)
     {
      System.err.println("The number of features to select is invalid.\nThe number of variables to select must be >= 1 and <="+max_number_of_features); 
      System.exit(1);
     }
     else
     {
      class_index = ap.get_index_of_target_variable(target_variable);//class_index starts from 1
     }
    }
    else //wrong number of input arguments, error message
    {
     System.err.println("Wrong number of input arguments.\nUsage: java ReduceDataInformationGainFeatureSelection <categories meaning file name> <delete_missing_values (0 or 1)> <datacube_file_name> <target variable index (optional)> <no of features> <weka_class_path> <reduced_data_cube_file_name> <xml tool description file>\n");
     System.exit(1);
    }

    randomGenerator = new Random();
    data_matrix_arff_file_name=randomGenerator.nextInt(1000000)+".arff";
    reduced_data_arff_file_name=Integer.toString(randomGenerator.nextInt(9000000))+".arff";
    datacube_no_missing_file_name = "datacube_no_missing_file"+Integer.toString(randomGenerator.nextInt(9000000));

    //delete missing values from the data cube or replace missing values withh most frequent values of variables
    missing_value = "888";//888 represents the missing value category in categories meaning file
    number_of_instances_removed = DataCube.handle_missing_values(missing_value,delete_missing_values,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);

    try
    {
     DataCube.transform_data_cube_to_data_matrix(datacube_no_missing_file_name,data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(1);
    }

    target_variable = DataMatrix.find_class_attr(data_matrix_arff_file_name,class_index);
    dt = new DecisionTrees();

    //check whether target variable has one value only.
    if(dt.oneClassDataSet(data_matrix_arff_file_name))
    {
        System.out.println("target variable: "+target_variable+"\n\nThis tool failed to run on the input data cube. \nThis tool requires that the target variable contains at least two values but the target variable of the input data cube contains one value.\nTo use this tool, set a different variable as the target variable or use a different data cube.\n");
        try
        {
         out = new FileWriter(reduced_data_cube_file_name);
        }
        catch(IOException e)
        {
         e.printStackTrace();
         System.exit(1);
        }
        out.write("target variable: "+target_variable+" This tool failed to run on the input data cube. This tool requires that the target variable contains at least two values but the target variable of the input data cube contains one value. To use this tool, set a different variable as the target variable or use a different data cube.\n");
        out.close();
        System.exit(2);  
    }    

    cmd="java -Xmx1024m -cp "+weka_class_path+" weka.filters.supervised.attribute.AttributeSelection -E \" weka.attributeSelection.InfoGainAttributeEval\" -c \""+class_index+"\" -i \""+data_matrix_arff_file_name+"\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N "+no_of_features+"\" >"+reduced_data_arff_file_name;
    pb = new ProcessBuilder();
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor();
    try
    {
     DataCube.create_data_cube(reduced_data_arff_file_name,reduced_data_cube_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by create_data_cube method");
     e.printStackTrace();
     System.exit(1);
    }

    algorithm_params_setting = "delete_missing_values="+delete_missing_values+" no_of_dimensions_selected="+no_of_features;

    try
    {
     ReduceDataRSFS.XMLdescription("Information Gain Dimensionality Reduction",algorithm_params_setting,xml_desc_file_name);
    }
    catch(IOException e)
    {
     e.printStackTrace();
     System.exit(1);
    }

    //delete temporary files
    cmd="rm "+datacube_no_missing_file_name+" "+data_matrix_arff_file_name+" "+reduced_data_arff_file_name;
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor(); 
    System.exit(0);
 }
}
  
                  
 	      
