/* 
   ReduceDataRSFS.java - reduces dimensionality of a data cube using rough set feature selection algorithm

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

public class ReduceDataRSFS
{    
 public final static void main(String[]args) throws Exception
 {
    String datacube_file_name=null;
    String data_matrix_arff_file_name=null;
    String reduced_data_arff_file_name=null;
    String class_index;
    String reduced_data_cube_file_name=null;
    String line;
    int class_index0=0;
    String class_attr=null;
    String [][] data_matrix=null;
    LinkedList<String> all_variables=null;
    RSys sys;
    String reduct_indices=null;
    StringTokenizer st;
    int k;
    int []reduct_indices_array;
    String cmd;
    ProcessBuilder pb=null;
    Process p=null;
    Random randomGenerator;
    String xml_desc_file_name=null;
    String target_variable=null;
    String algorithm_params_setting=null;
    AlgorithmParameters ap;
    String delete_missing_values=null; //0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
    String datacube_no_missing_file_name=null;
    int number_of_instances_removed=0;
    String missing_value=null;
    String categories_meaning_file_name="";
    DecisionTrees dt;
    int class_index1;
    FileWriter out=null;

    if(args.length == 5)//target variable not specified, the variable with the largest index of data set is the target variable
    {
     categories_meaning_file_name = args[0];
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2]; //data cube file
     reduced_data_cube_file_name = args[3];//results file  
     xml_desc_file_name = args[4];//xml description of association rule
     ap = new AlgorithmParameters(datacube_file_name);
     class_index0 = ap.get_index_of_variable_with_largest_index()-1;
    }
    else if(args.length == 6)//target variable is specified. valid formats: 3:_diabetes or diabetes
    {
     categories_meaning_file_name = args[0];
     delete_missing_values = args[1];//0 or 1: 0 means replacing missing values with the most frequent values of the variables; 1 means removing the instances with missing values
     datacube_file_name = args[2]; //data cube file
     target_variable = args[3];  // target variable: valid formats: 3:_diabetes or diabetes 
     reduced_data_cube_file_name = args[4];//results file  
     xml_desc_file_name = args[5];//xml description of association rule   
     ap = new AlgorithmParameters(datacube_file_name);
     class_index = ap.get_index_of_target_variable(target_variable);//class_index starts from 1
     class_index0=Integer.parseInt(class_index)-1; //class_index0 starts from 0
    }
    else //wrong number of input arguments, error message
    {
     System.err.println("Wrong number of input arguments.\nUsage: java ReduceDataRSFS <categories meaning file name> <delete_missing_values (0 or 1)> <datacube_file_name> <target_variable (optional)> <reduced_data_cube_file_name> <xml_file>\n");
     System.exit(1);
    }

    randomGenerator = new Random();
    data_matrix_arff_file_name=randomGenerator.nextInt(1000000)+".arff";
    reduced_data_arff_file_name=randomGenerator.nextInt(9000000)+".arff";
    datacube_no_missing_file_name = "datacube_no_missing_file"+Integer.toString(randomGenerator.nextInt(9000000));

    //delete missing values from the data cube or replace missing values withh most frequent values of variables
    missing_value = "888";//888 represents the missing value category in categories meaning file
    number_of_instances_removed = DataCube.handle_missing_values(missing_value,delete_missing_values,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);

    try
    {
     DataCube.transform_data_cube_to_data_matrix(datacube_no_missing_file_name, data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by transform_data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(1);
    }

    class_index1 = class_index0+1;
    target_variable = DataMatrix.find_class_attr(data_matrix_arff_file_name,Integer.toString(class_index1));
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
 
     try
    {
     data_matrix = DataMatrix.load_data_into_array(data_matrix_arff_file_name);
    }
    catch (IOException e)
    {
     System.err.println("IOException thrown by load_data_into_array method");
     e.printStackTrace();
     System.exit(1);
    }   
    
    sys = new RSys(data_matrix, data_matrix.length, data_matrix[0].length, class_index0);

    reduct_indices=sys.quickReduct();

    st=new StringTokenizer(reduct_indices,",");
    reduct_indices_array = new int [st.countTokens()];
    k = 0;
    while(st.hasMoreTokens())
    {
     reduct_indices_array[k]=Integer.parseInt(st.nextToken());
     k++; 
    }

    try
    { 
     all_variables = DataCube.variables_names(datacube_file_name);
    } 
    catch (IOException e)
    { 
     System.err.println("IOException thrown by variables_names method");
     e.printStackTrace();
     System.exit(1);
    }
        
    DataMatrix.reduce_data(all_variables,data_matrix,reduct_indices_array,reduced_data_arff_file_name);

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

    target_variable = all_variables.get(class_index0);
    algorithm_params_setting = "delete_missing_values="+delete_missing_values;

    try
    {
     XMLdescription("Rough Set Dimensionality Reduction",algorithm_params_setting,xml_desc_file_name);
    }
    catch(IOException e)
    {
     e.printStackTrace();
     System.exit(1);
    }
    //delete temporary file 
    cmd="rm "+datacube_no_missing_file_name+" "+data_matrix_arff_file_name+" "+reduced_data_arff_file_name;
    pb = new ProcessBuilder();
    pb.command("bash", "-c", cmd);
    p = pb.start();
    p.waitFor();
    System.exit(0);  
  }

 static void XMLdescription(String algorithm_name,String algorithm_params_setting,String xml_desc_file_name)throws IOException
 {
  //<DimensionalityReductionAlgorithm algorithmParamsSetting="target_variable=diabetes" algorithm_name="Rough Set Dimensionality Reduction" />
  FileWriter out=null;
  out = new FileWriter(xml_desc_file_name);
  out.write("<log>");
  out.write("<DimensionalityReductionAlgorithm algorithmParamsSetting= \""+algorithm_params_setting+"\" algorithm_name=\""+algorithm_name+"\" />");
  out.write("</log>");
  out.close();
 }
}
