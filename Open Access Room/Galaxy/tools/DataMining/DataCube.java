/* DataCube.java - defines the DataCube class

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
  
    You may download the source code and associated license at  https://github.com/linked2safety/code
*/ 
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.ProcessBuilder;

public class DataCube
{    
    static HashMap<String,HashMap<String,String>> categoriesMeanings=null; //key: variable, value: hastable (key:category, value: meaning of the category)
    static HashMap<String,String> missing_value_categories=new HashMap<String,String>();
    static HashMap<String,String> mostFrequentVals=new HashMap<String,String>();
 public static void main(String[]args) throws Exception
 {
    String data_matrix_arff_file_name;     
    String datacube_file_name;
    String categories_meanings_file_name;
    String meaningful_datacube_file_name;
    
    datacube_file_name = args[0]; 
    categories_meanings_file_name = args[1];
    data_matrix_arff_file_name = args[2];
    meaningful_datacube_file_name = "meaningful_datacube_file";

    try
    {
     transform_data_cube_to_data_matrix(datacube_file_name,categories_meanings_file_name,meaningful_datacube_file_name, data_matrix_arff_file_name);
    }
    catch(FileNotFoundException e)
    {
     System.err.println("FileNotFoundException thrown by transform_data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(-1);
    } 
    catch (IOException e)
    {
     System.err.println("IOException thrown by transform_data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(-1);
    }  
    catch (InterruptedException e)
    {
     System.err.println("InterruptedException thrown by transform_data_cube_to_data_matrix method");
     e.printStackTrace();
     System.exit(-1);
    } 
   

 }
 
 static HashMap<String,Integer> find_attrs_col_nos(String datacube_file_name)
 {//return: hash table: key=attribute, value=column index of attribute

  String all_variables=null;
  BufferedReader in=null;
  String [] variables;
  HashMap<String,Integer> hash_attributes_col_nos;

  hash_attributes_col_nos = new HashMap<String,Integer>();

  try
  {
   in = new BufferedReader(new FileReader(datacube_file_name));
  }
  catch (FileNotFoundException e)
  {
   System.err.println(datacube_file_name+" is not found.");
   System.exit(-1);
  }
  try
  {
   all_variables=in.readLine();
  }
  catch(IOException e)
  {
   System.err.println("IOException in readLine()");
   System.exit(1);
  }
  variables = all_variables.split(",");
  for(int i=0; i < variables.length; i++)//the last variable is 'count'
  {
    hash_attributes_col_nos.put(variables[i],i);
  }
  try
  {
    in.close();
  }
  catch(IOException e)
  {
   System.err.println("IOException in in.close()");
   System.exit(1); 
  }
  return hash_attributes_col_nos;
 }

static int handle_missing_values(String missing_value, String delete_missing_values, String datacube_file_name, String categories_meaning_file_name, String datacube_no_missing_file_name)throws IOException, FileNotFoundException
{
  //delete the instances with missing values or replace the missing values with most frequent values
  //output: data cube after missing values handled
  //        number of instances removed (returned) i.e. number of instances with missing values
  //Check if any category in the data cube file has no meaning in the categories meanings file 
  //If so the category is treated as a missing value category '888'
  
  int number_of_instances_removed=0;
  
  store_categories_meanings_in_hashtable(categories_meaning_file_name);
  
  handle_undefined_values(datacube_file_name,categories_meaning_file_name,missing_value);
  
  if(delete_missing_values.equals("0"))
  {
   replace_missing_values(missing_value,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);
   number_of_instances_removed = 0;//no instances are removed from data cube
  }
  else if(delete_missing_values.equals("1"))
  {
   try {
        number_of_instances_removed = delete_instances_with_missing_values(missing_value,datacube_file_name,categories_meaning_file_name,datacube_no_missing_file_name);
  }
   catch(IOException e)
   {
    e.printStackTrace();
    System.exit(1);
   }
  }
  else
  {
   System.err.println("Wrong option is entered for delete_missing_values of handle_missing_values method.\nValid options: 0 or 1\n");
   System.exit(1);
  }
  
 return number_of_instances_removed;
}

static void handle_undefined_values(String datacube_file_name, String categories_meanings_file_name, String missing_value)throws IOException, FileNotFoundException 
{//if the missing value of a variable is defined in categories meanings file
 // then replace the undefined categories of a variable in the data cube file with the missing value
 // else replace the undefined categories of a variable in the data cube file with the most frequent value of the variable
	 
 String variable;
 String missing_value_category;
 String datacube [][]=null;
 String most_freq_val;
 
 try
 {
    missing_value_categories = find_missing_value_categories(categories_meanings_file_name,missing_value);
 }
 catch(IOException e)
 {
   e.printStackTrace();
   System.exit(1);
 }	

 LinkedList<String> all_variables=null;

 try {
      all_variables = variables_names(datacube_file_name);
 }
  catch(FileNotFoundException e)
 {
  e.printStackTrace();
  System.exit(1);
 }
  catch(IOException e)
 {
  e.printStackTrace();
  System.exit(1);
 }
 
 try
 {
  datacube = DataCube.load_data_cube_into_array(datacube_file_name);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(1);
 }
 
 for(int j=0; j< datacube[0].length-1; j++)
 {
  variable = all_variables.get(j);
  if(missing_value_categories.containsKey(variable))//missing value is defined for this variable
  {	  
   missing_value_category = missing_value_categories.get(variable);
   for(int i=0; i< datacube.length; i++)
	   if (undefinedCategory(variable,datacube[i][j]))//the category is undefined in the knowledge base
	   {	 
	      missing_value_category = missing_value_categories.get(variable);
          datacube[i][j]=missing_value_category;     
	   }	  
  }
  else
  {
   for(int i=0; i< datacube.length; i++)  
   {
	 if (undefinedCategory(variable,datacube[i][j]))//the category is undefined in the knowledge base
	 {	
	  if(mostFrequentVals.containsKey(variable))//most fequent value has been found
	 	  most_freq_val = mostFrequentVals.get(variable);
	  else//find most frequent value
	      most_freq_val = get_most_frequent_value(variable,j,datacube,datacube[i][j]);          
	  datacube[i][j] = most_freq_val;
	 }
   }  
  }
 }
  
  try
  { 
   write_datacube_to_csv_file(all_variables,datacube,datacube_file_name);	
  }
  catch(IOException e)
  {
   e.printStackTrace();
   System.exit(1);
  }
}

static boolean undefinedCategory(String variable, String category)
{
  HashMap<String,String> categories_meanings_of_variable;
	
  if(categoriesMeanings.containsKey(variable))
  { 
	  categories_meanings_of_variable = categoriesMeanings.get(variable);
      if(!categories_meanings_of_variable.containsKey(category))
       	 return true;
      else
      	 return false;
  }
  else
  {
	  System.out.println("The variable: "+variable+" in the data cube file is not in the knowledge base.");
	  System.exit(1);
	  return false;
  }
}
		
static void replace_missing_values(String missing_value, String datacube_file_name, String categories_meaning_file_name, String datacube_no_missing_file_name)
{//replace missing values with the most frequent values of the variables
 //HashMap <String,String> missing_value_categories=null;
 LinkedList<String> all_variables=null;

 try {
      all_variables = variables_names(datacube_file_name);
 }
  catch(FileNotFoundException e)
 {
  e.printStackTrace();
  System.exit(1);
 }
  catch(IOException e)
 {
  e.printStackTrace();
  System.exit(1);
 }
 
 replace_missing_values_with_most_frequent_values(all_variables,datacube_file_name,datacube_no_missing_file_name);
}

static int delete_instances_with_missing_values(String missing_value, String datacube_file_name, String categories_meaning_file_name, String datacube_no_missing_file_name) throws IOException
{//remove instances with missing values; if no instances has missing values, no instances are removed
 //return: number of instances removed i.e. number of instances with missing values 

 String missing_value_category=null;
 String variable=null;
 BufferedReader in; 
 FileWriter out;
 String line;
 String [] categories=null;
 int number_of_instances_removed=0;
 boolean instances_contain_missing_values = false;
 LinkedList<String> all_variables=null;

 try {
      all_variables = variables_names(datacube_file_name);
 }
  catch(FileNotFoundException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
  catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
 
 in= new BufferedReader(new FileReader(datacube_file_name));
 out=new FileWriter(datacube_no_missing_file_name);
 line = in.readLine(); //1st line is attributes names
 out.write(line+"\n");

 line = in.readLine(); 
 while(line!=null)
 {
   categories = line.split(",");
   for(int i=0; i<categories.length-1; i++)
   {
     variable = all_variables.get(i);
     if(missing_value_categories.containsKey(variable))
     {
       missing_value_category = missing_value_categories.get(variable);
       if(categories[i].equals(missing_value_category))
       {   
        instances_contain_missing_values = true;
        number_of_instances_removed += Integer.parseInt(categories[categories.length-1]);
        break;
       }
     }
   }
   if(!instances_contain_missing_values)
     out.write(line+"\n");
   instances_contain_missing_values = false;
   line = in.readLine();      
 }    
 in.close();
 out.close();

 return number_of_instances_removed;
}

static int number_of_instances_with_missing_values(String missing_value, String datacube_file_name, String categories_meaning_file_name) throws IOException
{
 BufferedReader in; 
 String line;
 String [] categories=null;
 int number_of_instances_with_missing_vals=0;
 LinkedList<String> all_variables=null;
 String variable=null;
 String missing_value_category=null;
 
 try {
      all_variables = variables_names(datacube_file_name);
 }
  catch(FileNotFoundException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
  catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }

 if(missing_value_categories.isEmpty())
 {
	 try {
      missing_value_categories = find_missing_value_categories(categories_meaning_file_name,missing_value);
   }
   catch(IOException e)
  {
   e.printStackTrace();
   System.exit(-1);
  }
 }
	 

 in= new BufferedReader(new FileReader(datacube_file_name));
 line = in.readLine(); //1st line is attributes names
 
 line = in.readLine(); 
 while(line!=null)
 {
   categories = line.split(",");
   for(int i=0; i<categories.length-1; i++)
   {
     variable = all_variables.get(i);
     missing_value_category = missing_value_categories.get(variable);
     if(categories[i].equals(missing_value_category))
     {   
       number_of_instances_with_missing_vals += Integer.parseInt(categories[categories.length-1]);
       break;//stop looking at the value of next variable when this variable contains missing value category as the instances represented by this row have been detected as having missing values already
     }
   }
   line = in.readLine();      
 }    
 in.close();

 return number_of_instances_with_missing_vals;
}

static HashMap<String,String> find_missing_value_categories(String categories_meanings_file_name,String missing_value) throws IOException
{//find missing value categories of all variables
 //store missing value category of each variable into hash table missing_value_categories
 //return missing_value_categories
  BufferedReader in=null;
  String line=null;
  String [] str;
  String [] str2;
  String variable=null;
  String category=null;
  HashMap <String,String> missing_value_categories;
  String categories_meanings_str=null;
  String category_meaning_str=null;

  missing_value_categories = new HashMap<String,String>();
  in = new BufferedReader(new FileReader(categories_meanings_file_name));
  line = in.readLine();
  while(line!=null)
  {
   str = line.split("\\|");  
   variable = str[0];//1st column is variable name      
   categories_meanings_str = str[1];//2nd column is categories meanings
   str = categories_meanings_str.split(",");
   for(int i=0; i < str.length; i++)
   {
    category_meaning_str = str[i];
    str2 = category_meaning_str.split(":");    
    category = str2[0];   
    if(category.equals(missing_value))
    {
     missing_value_categories.put(variable,category);     
     break;
    }
   }
   //if(!found)
   //  System.out.println("missing value category of missing value: "+missing_value+" for variable "+variable+" is not found in "+categories_meanings_file_name);
  line = in.readLine();
  }
  in.close();

  return missing_value_categories;
}

static void replace_missing_values_with_most_frequent_values(LinkedList<String> all_variables, String datacube_file_name, String datacube_no_missing_file_name)
{
 //HashMap<String,String> most_frequent_vals= new HashMap<String,String>();
 String datacube [][]=null;
 String variable=null;
 String most_freq_val=null;
 String missing_value_category=null;

 try
 {
  datacube = DataCube.load_data_cube_into_array(datacube_file_name);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(1);
 }

 for(int j=0; j< datacube[0].length-1; j++)
 {
  variable = all_variables.get(j);
  if(missing_value_categories.containsKey(variable))
  {
   missing_value_category = missing_value_categories.get(variable);
   for(int i=0; i< datacube.length; i++)
    if(datacube[i][j].equals(missing_value_category))
    { 
      if(mostFrequentVals.containsKey(variable))//most fequent value has been found
          most_freq_val = mostFrequentVals.get(variable);
      else//find most frequent value
	      most_freq_val = get_most_frequent_value(variable,j,datacube,missing_value_category);          
      datacube[i][j] = most_freq_val;
    }
  }
 }
    
  //write datacube to csv file
  try
  { 
   write_datacube_to_csv_file(all_variables,datacube,datacube_no_missing_file_name);	
  }
  catch(IOException e)
  {
   e.printStackTrace();
   System.exit(1);
  } 
}

static String get_most_frequent_value(String variable, int variable_indx, String [][] datacube, String missing_value_category)
{// find the most frequent value of a variable and put <variable, most_freq_value> into hashtable
 // return the most_freq_value

 String most_freq_val=null;
 HashMap<String,Integer> values_freq;
 Iterator<String> keys= null;
 String val=null;
 int max_freq = -1;
 int freq = -1;
 int count=0;

 values_freq = new HashMap<String,Integer>();//key=value of variable, value=frequency of value

 for(int i=0;i< datacube.length; i++)
 {
  count = Integer.parseInt(datacube[i][datacube[0].length-1]);
  if(!datacube[i][variable_indx].equals(missing_value_category) && categoriesMeanings.get(variable).containsKey(datacube[i][variable_indx])==true)
  { 
   val = datacube[i][variable_indx];
   if(values_freq.containsKey(val))
   {
    values_freq.put(val,new Integer(count + values_freq.get(val).intValue())); 
   }
   else
    values_freq.put(val,new Integer(count));  
  }  
 }
 keys = (values_freq.keySet()).iterator();
 while(keys.hasNext())
 {
   try
   { 
    val = (String)keys.next();
   }
   catch (NoSuchElementException e)
   {
    e.printStackTrace();
    System.exit(1);
   }
   freq = values_freq.get(val).intValue();
   //System.out.println(val+": "+freq);
   if(freq > max_freq)
   {
    most_freq_val = val;
    max_freq = freq;
   }
 }
 mostFrequentVals.put(variable,most_freq_val);

 return most_freq_val;
}

static void  write_datacube_to_csv_file(LinkedList<String> all_variables,String [][] datacube, String datacube_no_missing_file_name) throws IOException
{
  FileWriter out;
  Object [] all_variables_array=null;

  out = new FileWriter(datacube_no_missing_file_name);
  //write variables and 'count' variable
  all_variables_array = all_variables.toArray();
  for(int k=0; k< all_variables_array.length;k++)
      out.write(all_variables_array[k]+",");
  out.write("count\n");
  //write data cube with missing values replaced
  for(int i=0; i< datacube.length; i++)
  {
   for(int j=0; j< datacube[0].length-1; j++)
       out.write(datacube[i][j]+",");
   out.write(datacube[i][datacube[0].length-1]+"\n");
  }
  out.close();
}
	
static void record_information_on_missing_values(String missing_value, String delete_missing_values, String categories_meaning_file_name, String datacube_file_name, int number_of_instances_removed, String association_rules_file_name) throws IOException
{
 //write the following to results file:
 //				  total number of instances,
 //				  number of instances with missing values,
 //			   	  number of instances after removing the instances with missing values

 String datacube [][]=null;
 FileWriter out=null;
 int total_number_of_instances=0;
 int number_of_instances_after_remove_missing_instances=0;
 int number_of_instances_with_missing_vals=0;

 try
 {
  out = new FileWriter(association_rules_file_name);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }

 try
 {
  datacube = DataCube.load_data_cube_into_array(datacube_file_name);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }

 total_number_of_instances = (int)get_total_number_of_instances(datacube);
 
 /*
 try {
 number_of_missing_vals = number_of_missing_values(missing_value,categories_meaning_file_name,datacube_file_name);
 } 
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
 */
 try {
  number_of_instances_with_missing_vals = number_of_instances_with_missing_values(missing_value, datacube_file_name, categories_meaning_file_name);
 } 
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
 out.write("Total number of instances: "+total_number_of_instances+"\n");
 out.write("Number of instances with missing values: "+Integer.toString(number_of_instances_with_missing_vals)+"\n");
if(number_of_instances_with_missing_vals == 0)
  out.write("The input data has no missing values.\n\n"); 
else if(delete_missing_values.equals("0"))
  out.write("No instances are removed from the input data.\nMissing values are replaced with most frequent values of the variables.\n\n"); 
else
 {
  number_of_instances_after_remove_missing_instances = total_number_of_instances - number_of_instances_removed;
  out.write("The instances with missing values are removed from the input data.\nNumber of instances remained after removing the instances with missing values: "+Integer.toString(number_of_instances_after_remove_missing_instances)+".\n\n");
 }
 out.close();
}
/*
static int number_of_missing_values(String missing_value, String categories_meaning_file_name, String datacube_file_name) throws IOException
{
 String missing_value_category=null;
 BufferedReader in; 
 String line;
 String [] categories=null;
 int number_of_missing_values=0;

 try {
      missing_value_category = find_missing_value_category(categories_meaning_file_name,missing_value);
 }
 catch(IOException e)
 {
  e.printStackTrace();
  System.exit(-1);
 }
   
 in= new BufferedReader(new FileReader(datacube_file_name));
 line = in.readLine(); //1st line is attributes names
 line = in.readLine(); 
 while(line!=null)
 {
   categories = line.split(",");
   for(int i=0; i<categories.length-1; i++)
     if(categories[i].equals(missing_value_category))
      	number_of_missing_values += Integer.parseInt(categories[categories.length-1]);
   line = in.readLine();      
 }    
 in.close();

 return number_of_missing_values;
}
*/
 static String transform_data_cube_to_data_matrix(String datacube_file_name, String categories_meanings_file_name, String meaningful_datacube_file_name, String data_matrix_arff_file_name) throws IOException, FileNotFoundException, InterruptedException
 { //map variables' categories in a data cube to their meanings and transform the meaningful data cube to a data matrix in weka arff format
   
   //outputs: a meaningful data cube file (returned) 
   //	      a meaningful data matrix in weka format            
   //int no_of_attributes;
   //Random randomGenerator;
   //String cmd=null;
   //Process p=null;
   //ProcessBuilder pb;
  
   map_categories_to_meaning(datacube_file_name,categories_meanings_file_name,meaningful_datacube_file_name);
   transform_data_cube_to_data_matrix(meaningful_datacube_file_name, data_matrix_arff_file_name);
  
   return meaningful_datacube_file_name;
 }

 static void map_categories_to_meaning(String datacube_file_name,String categories_meanings_file_name, String meaningful_datacube_file_name)throws IOException, FileNotFoundException 
 {
	 //store_categories_meanings_in_hashtable(categories_meanings_file_name);
	 create_meaningful_data_cube(datacube_file_name, meaningful_datacube_file_name);
 }
 
 static void store_categories_meanings_in_hashtable(String categories_meanings_file_name)throws IOException, FileNotFoundException
 {
	
  BufferedReader in; 
  
  String line;
 
  HashMap<String,HashMap<String,String>> categories_meanings; //key: variable, value: hastable (key:category, value: meaning of the category)
  HashMap<String,String> categories_meanings_of_variable;
  String [] str;
  String [] str2;
  String variable;
  String category;
  String meaning;
  String categories_meanings_str;
  String category_meaning_str;

  categories_meanings = new HashMap<String,HashMap<String,String>>();
  
  in = new BufferedReader(new FileReader(categories_meanings_file_name));
  line = in.readLine();
  while(line!=null)
  {
   categories_meanings_of_variable = new HashMap<String,String>();
   str = line.split("\\|");  
   variable = str[0];//1st column is variable name
   if(emptyString(variable))
    System.out.println("variable: "+variable+" is empty in the knowledge base.");
   else 
   {  
    categories_meanings_str = str[1];//2nd column is categories meanings
    str = categories_meanings_str.split(",");
    for(int i=0; i < str.length; i++)
    {
     category_meaning_str = str[i];
     str2 = category_meaning_str.split(":");    
     category = str2[0];
     meaning = str2[1];

     //if category is a sequence of spaces or empty string or meaning is a sequence of spaces or empty string 
     //then print error message there is no category or there is no meaning for the category
     //else remove any leading and ending spaces from category and meaning.
     //     replace any spaces in category and meaning with '_'s
     if(emptyString(category)||emptyString(meaning))
     {
      System.out.println("variable: "+variable+" has an empty category or empty meaning in the knowledge base.");
     }
     else 
     { 
        category = category.trim();
        meaning = meaning.trim();
        try 
        {
         category = category.replaceAll("\\s+","_");
         meaning = meaning.replaceAll("\\s+","_");
        }
        catch (PatternSyntaxException e)
	    {
         System.out.println("The syntax of regular expressions for category and meaning are invalid"); 
        }
        categories_meanings_of_variable.put(category,meaning);
     }
    }
    categories_meanings.put(variable,categories_meanings_of_variable);
   }
   line = in.readLine();
  }
  in.close();
  
  categoriesMeanings = categories_meanings;
 }
 
static void create_meaningful_data_cube(String datacube_file_name,String meaningful_datacube_file_name)throws IOException, FileNotFoundException
{//create a meaningful data cube by mapping categories in the data cube into their meanings
 //output: a meaningful data cube
 //map the categories in the data cube to their meanings by looking at their meanings in the hashtable
  BufferedReader in;
  String line=null;
  FileWriter out;
  int count_variable_index=0;
  String variables[]=null;
  String categories []=null;
  String meaning=null;
  
  in = new BufferedReader(new FileReader(datacube_file_name));
  out = new FileWriter(meaningful_datacube_file_name);
  line = in.readLine();//1st line of data cube file contains the variables names and the count variable 
  out.write(line+"\n");//write the variables names and count variable into the meaningful data cube
  variables = line.split(",");
  count_variable_index = variables.length-1;
  line = in.readLine();
  while(line!=null)
  {
   categories = line.split(",");
   for(int i=0;i < count_variable_index;i++)
   {
    meaning = (categoriesMeanings.get(variables[i])).get(categories[i]);
    out.write(meaning+",");
   }
   out.write(categories[count_variable_index]+"\n");//write count value
   line = in.readLine();
  }
  out.close();
  in.close();
 }

 static boolean emptyString(String Str)
 {//return true if string is empty literal or a sequence of spaces
  if (Str.length()==0)
   return true;
  else
   { 
	  Pattern p = Pattern.compile("^\\s+$");
	  Matcher m;
	  m=p.matcher(Str);
      if(m.matches())
      {
    	return true;  
      }
      return false;
   }
 }
 
 static float get_total_number_of_instances(String [][] datacube)
 {
  //return: total number of instances in the data cube
  
   float total_number_of_instances=0;

   for(int i = 0; i < datacube.length; i++)//add up the counts in the last column
     total_number_of_instances = total_number_of_instances + Float.parseFloat(datacube[i][datacube[0].length-1]);
  
   return total_number_of_instances;
 }

 static String [][] load_data_cube_into_array(String datacube_file_name) throws IOException, FileNotFoundException
 {
  //load a data cube into a 2-D array
  //return: a 2-D array containing the data cube

  BufferedReader in; 
  String line;
  StringTokenizer st;
  int cols;
  int rows;
  String datacube [][];
  
  in= new BufferedReader(new FileReader(datacube_file_name));
  line = in.readLine(); //consume the 1st line (attributes names)
  st = new StringTokenizer(line,",");
  cols=0;
  rows=0;

  //get dimensions of datacube
  cols = st.countTokens();
  line = in.readLine(); 
  while(line!=null)
  {
    rows++;
    line = in.readLine();      
  }    
  in.close();
  datacube = new String [rows][cols];
  //read data cube into 2-D array    
  in = new BufferedReader(new FileReader(datacube_file_name));
  line = in.readLine();//consume the 1st line: attributes names     
  for(int i=0;i<rows;i++)
  { 
   line = in.readLine();
   st = new StringTokenizer(line,",");
   for(int j=0;j<cols;j++)
   {
    datacube[i][j]= st.nextToken();
   } 
  }
  in.close();

  return datacube;
 }

 static int transform_data_cube_to_data_matrix(String datacube_file_name, String data_matrix_arff_file_name) throws IOException, FileNotFoundException
 { //transform a data cube to data matrix format 
   //output: a data matrix in weka arff format
   //return: number of attributes in the data cube

    BufferedReader in; 
    FileWriter out;
    String line;
    StringTokenizer st;
    StringTokenizer st_attr_names;
    int cols;
    int rows;
    String datacube [][];
    Set<String> attrs_vals_set;
    int count_attr_index;
    int last_attr_index;
    int no_of_attributes;

    in= new BufferedReader(new FileReader(datacube_file_name));
    out = new FileWriter(data_matrix_arff_file_name);
    line = in.readLine(); //consume the 1st line (attributes names)
    st = new StringTokenizer(line,",");
    st_attr_names = st;
    cols=0;
    rows =0;
    attrs_vals_set=new HashSet<String>();
    //get dimensions of l2s datacube
    cols=st.countTokens();
    line = in.readLine(); 
    while(line!=null)
    {
      rows++;
      st = new StringTokenizer(line,",");
      line = in.readLine();      
    }    
    in.close();
    datacube = new String [rows][cols];
    //read datacube into 2-D array    
    in = new BufferedReader(new FileReader(datacube_file_name));
    line = in.readLine();//consume the 1st line: list of attributes     
    for(int i=0;i<rows;i++)
    { 
      line = in.readLine();
      st = new StringTokenizer(line,",");
      for(int j=0;j<cols;j++)
      {
       datacube[i][j]= st.nextToken();
      } 
    }
    in.close();
    //output a data matrix in arff format
    count_attr_index= cols-1;
    last_attr_index = cols-2;
    out.write("@relation data_matrix\n");
    for(int j=0;j<count_attr_index;j++)
    {
     for(int i=0;i<rows;i++) 
     {
       attrs_vals_set.add(datacube[i][j]);
     }
     out.write("@attribute "+st_attr_names.nextToken()+" {");
     for(String val: attrs_vals_set)
     {
      out.write(val+",");
     }
     out.write("}\n");
     attrs_vals_set.clear();
   }
   out.write("@data\n");
   for(int i=0; i<rows;i++)
   {    
   	int count = Integer.parseInt(datacube[i][count_attr_index]);
   	if(count > 0)
    	{
      	  for(int c=0; c<count;c++)
      	  {
     	    for(int j=0;j<last_attr_index;j++)
            {
             out.write(datacube[i][j]+","); 
            }
            out.write(datacube[i][last_attr_index]+"\n");
          }
        }
  }
  out.close();
  no_of_attributes = cols-1;
  return no_of_attributes;
 }

static void create_data_cube(String data_arff_file_name, String data_cube_file_name) throws IOException
 {
 /*
  input arguments: a discrete data in weka arff format
                   data cube file name
  output: the data cube file
  
  1. create a data cube by counting the number of rows with the same combination of values for every combination of values in the data file: 

       for each combination of values in the data file
       {
        counting the number of rows (subjects) which have that combination of values
       }

  2. Output the data cube
 */
   BufferedReader in; 
   FileWriter out;
   String line="";
   Pattern p;
   String [][]data_matrix;
   Matcher m;

   p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}\\s]+)\\s+\\{([a-zA-Z_0-9\\p{Punct}]+)\\}$");

   data_matrix = DataMatrix.load_data_into_array(data_arff_file_name);
   
   in= new BufferedReader(new FileReader(data_arff_file_name));
   out = new FileWriter(data_cube_file_name);
   //write the variable names to the data cube file
   while(!line.equals("@data"))
   {       
     m=p.matcher(line);
     if(m.matches())
     {
      out.write(m.group(1)+",");             
     }
     line=in.readLine();     		
   }
   out.write("count\n");
   //create data cube and annoymize data cube
   try
   {
    out = count_subjects(data_matrix,out);   
   }
   catch(IOException e)
   {
    System.err.println("IOException thrown by create_combinations_and_count_combinations method");
    e.printStackTrace();
   } 
   out.close();
 }

 static FileWriter count_subjects(String [][] data_matrix, FileWriter out) throws IOException
 {
  HashSet<Integer> counted_rows = new HashSet<Integer>();//to remember the rows that have been counted
  int count;//count of the subjects which have the same combination of attributes values
  boolean match=true;
 
  for(int i=0;i<data_matrix.length;i++)
  { 
    if(counted_rows.size() == data_matrix.length)
      break;
    else
    { 
     if(counted_rows.contains(new Integer(i))==false)
     { 
      counted_rows.add(new Integer(i));
      count=1;
      for(int i2=0; i2<data_matrix.length; i2++)
      {
       if(i2!=i)
       {
        if(counted_rows.contains(new Integer(i2))==false)
        {
          for(int j=0;j<data_matrix[0].length;j++)
          {
            if(data_matrix[i][j].equals(data_matrix[i2][j]))
            {
            }
            else
            {     
             match=false;
             break;
            }
          }//for
          if(match==true)
          {
           count++;
           counted_rows.add(new Integer(i2)); 
          }      
          else
           match=true;
        }//if
       }//if
      }//for    
      for(int j=0;j<data_matrix[0].length;j++) 
         out.write(data_matrix[i][j]+",");//write a combination of values   
      out.write(count+"\n");
     }//if 
    }//if          
   }//for
   
   return out;
 }

 static void create_data_cube(String data_arff_file_name, int count_threshold, String data_cube_file_name) throws IOException
 {
 /*
  input arguments: a discrete data in weka arff format
                   count threshold
		   data cube file
  output: the annoymized data cube using the count threshold
  
  1. create a data cube by counting the number of rows with the same combination of values for every combination of values in the data file: 
       for each combination of values in the data file
       {
        counting the number of rows (subjects) which have that combination of values
       }
  2. perturbation of counts: perturb each count by adding 1 or -1 to the count
  3. thresholding: 
  4.   for each subject do
  5.   {
  6.    if(its count < count threshold)
  7.      remove the subject from the data set by setting its count to 0
  8.   }
  9. Output the annoymized data cube
 */
   BufferedReader in; 
   FileWriter out;
   String line="";
   Pattern p;
   String [][]data_matrix;
   Matcher m;

   p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}\\s]+)\\s+\\{([a-zA-Z_0-9\\p{Punct}]+)\\}$");

   data_matrix = DataMatrix.load_data_into_array(data_arff_file_name);
   
   in= new BufferedReader(new FileReader(data_arff_file_name));
   out = new FileWriter(data_cube_file_name);
   //write the variable names to the data cube file
   while(!line.equals("@data"))
   {       
     m=p.matcher(line);
     if(m.matches())
     {
      out.write(m.group(1)+",");             
     }
     line=in.readLine();     		
   }
   out.write("count\n");
   //create data cube and annoymize data cube
   try
   {
    out = count_subjects_and_perturb_counts_and_thresholding(count_threshold,data_matrix,out);   
   }
   catch(IOException e)
   {
    System.err.println("IOException thrown by create_combinations_and_count_combinations method");
   } 
   out.close();
 }
  
 static FileWriter count_subjects_and_perturb_counts_and_thresholding(int count_threshold,String [][] data_matrix, FileWriter out) throws IOException
 {
  HashSet<Integer> counted_rows = new HashSet<Integer>();//to remember the rows that have been counted
  int count;//count of the subjects which have the same combination of attributes values
  int perturbed_count;
  int no_of_subjects_removed=0;
  boolean match=true;
  Random randomGenerator;

  randomGenerator = new Random();
  for(int i=0;i<data_matrix.length;i++)
  { 
    if(counted_rows.size() == data_matrix.length)
      break;
    else
    { 
     if(counted_rows.contains(new Integer(i))==false)
     { 
      counted_rows.add(new Integer(i));
      count=1;
      for(int i2=0; i2<data_matrix.length; i2++)
      {
       if(i2!=i)
       {
        if(counted_rows.contains(new Integer(i2))==false)
        {
          for(int j=0;j<data_matrix[0].length;j++)
          {
            if(data_matrix[i][j].equals(data_matrix[i2][j]))
            {
            }
            else
            {     
             match=false;
             break;
            }
          }//for
          if(match==true)
          {
           count++;
           counted_rows.add(new Integer(i2)); 
          }      
          else
           match=true;
        }//if
       }//if
      }//for    
      if(randomGenerator.nextInt(2)==0)
        perturbed_count = count+1;  	     //perturb count by adding 1 to it
      else
        perturbed_count = count-1;             //perturb count by subtracting 1 from it      
      if(perturbed_count>=count_threshold)//>= threshold: write the combination of variables values and perturbed count of subjects to data cube file      
      { 
       for(int j=0;j<data_matrix[0].length;j++) 
         out.write(data_matrix[i][j]+",");   
       out.write(perturbed_count+"\n");
      }
      else //below threshold, the combination of variables values is not writen to data cube file
      {
       no_of_subjects_removed = count + no_of_subjects_removed;
      }
      System.out.println("count: "+count+" perturbed count: "+perturbed_count);    
     }//if          
    }//else
   }//for
   System.out.println("number of subjects removed by annoymization: "+no_of_subjects_removed+"("+(float)no_of_subjects_removed/(float)data_matrix.length*100f+")%");
   return out;
 }
 
  public static LinkedList<String> variables_names(String datacube_file_name)throws FileNotFoundException,IOException
  {
   //output names of all variables

   LinkedList<String> all_variables_list;
   BufferedReader in;
   String all_variables;
   StringTokenizer st;
   int n;

   all_variables_list=new LinkedList<String>();
   in = new BufferedReader(new FileReader(datacube_file_name));
   all_variables=in.readLine();
   st=new StringTokenizer(all_variables,",");
   n=st.countTokens();//position of the count variable in st

   for(int a=1;a<n;a++) //exclude the count variable
   {
    all_variables_list.add(st.nextToken());
   }
   return all_variables_list;
  }
}
  
                  
 	      
