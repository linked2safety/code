/* DataMatrx.java - defines the DataMatrix class

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

public class DataMatrix
{

 public static HashMap<String,Integer> find_attrs_col_nos(String data_matrix_arff_file_name)
 {//find the column numbers of attributes in a weka data file
	int attr_col_no=0;	
	BufferedReader in=null;	
	String line=null;
	Pattern p;
	Matcher m;
        HashMap<String,Integer> hash_attributes_col_nos = new HashMap<String,Integer>();

	p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}]+)\\s+.+$");        
	try
	{        
	 in= new BufferedReader(new FileReader(data_matrix_arff_file_name));
 	}
	catch(FileNotFoundException e)
	{
	 System.out.println("FileNotFoundException thrown by find_attrs_col_nos at BufferedReader");
	}
  	try
	{       
 	 line = in.readLine();
        }
	catch(IOException e)
	{
	 System.out.println("IOException thrown by find_attrs_col_nos");	
	}
	while(!line.equals("@data"))
	{       
		m=p.matcher(line);
	 	if(m.matches())
		{
                  hash_attributes_col_nos.put(m.group(1),attr_col_no);		 
                  attr_col_no++;
		}           
		try
		{
		 line=in.readLine();
		}
		catch(IOException e)
		{
		  System.out.println("IOException thrown by find_attrs_col_nos at in.readLine()");	
		}	
	}
	try
	{
	 in.close();
	}
	catch(IOException e)
	{
	 System.out.println("IOException thrown by find_attrs_col_nos at in.close()");	
	}	
	return hash_attributes_col_nos;
 }

 public static String find_class_attr(String data_matrix_arff_file_name, String class_index)
 {//find the target variable name in a weka data file using the its index
	int attr_index=1;
	int class_index_int;
	BufferedReader in=null;	
	String line=null;
	Pattern p;
	Matcher m;
	
	p=Pattern.compile("^\\@attribute\\s+([\\w\\p{Punct}]+)\\s+.+$");
        class_index_int=Integer.parseInt(class_index);
	try
	{        
	 in= new BufferedReader(new FileReader(data_matrix_arff_file_name));
 	}
	catch(FileNotFoundException e)
	{
	 System.out.println("FileNotFoundException thrown by find_attrs_col_nos at BufferedReader");
	}
  	try
	{       
 	 line = in.readLine();
        }
	catch(IOException e)
	{
	 System.out.println("IOException thrown by find_attrs_col_nos");	
	}
	while(!line.equals("@data"))
	{       
		m=p.matcher(line);
	 	if(m.matches())
		{
                  if(attr_index==class_index_int)
		  {
		    return m.group(1);
		  }
		  attr_index++;
		}           
		try
		{
		 line=in.readLine();
		}
		catch(IOException e)
		{
		  System.out.println("IOException thrown by find_attrs_col_nos at in.readLine()");	
		}	
	}
	try
	{
	 in.close();
	}
	catch(IOException e)
	{
	 System.out.println("IOException thrown by find_attrs_col_nos at in.close()");	
	}	
	return null;
 }
 
 public static String [][] load_data_into_array(String data_matrix_arff_file_name) throws IOException, FileNotFoundException
 {
  BufferedReader in=null; 
  String line="";
  int rows=0;
  int cols;
  Pattern p,p2;
  Matcher m;
  int i=0;
  String [] row;
  String [][] data_matrix = null;

  p=Pattern.compile("^\\s*$");//empty line or a line of spaces
  p2=Pattern.compile("^[a-zA-Z_0-9\\p{Punct}\\s]+$");//an instance
  try
  {
   in= new BufferedReader(new FileReader(data_matrix_arff_file_name));
  }
  catch (IOException e)
  {
   System.out.println("IOException thrown by BufferedReader(new FileReader(data_matrix_arff_file_name))");
  }
  line = in.readLine();
  //create a 2-d array
  while(!line.equals("@data"))//go to line containing @data
  {  
   line=in.readLine();
  }  
  line=in.readLine();
  m=p.matcher(line);
  while(m.matches())//consume any lines of spaces after @data
  {
   line=in.readLine();
   m=p.matcher(line);
  }
  cols = line.split(",").length;   
  while(line!=null)
  {
   m = p2.matcher(line);
   if(m.matches())
   {
    rows++;
   }
   line=in.readLine();
  }
  data_matrix = new String[rows][cols];
  //System.out.println("rows: "+rows+" cols:"+cols);
  in.close();
  //read data into array
  in = new BufferedReader(new FileReader(data_matrix_arff_file_name));
  line=in.readLine();  
  while(!line.equals("@data"))//go to line containing @data
  {  
   line=in.readLine();
  }  
  line=in.readLine();
  m=p.matcher(line);
  while(m.matches())//consume any lines of spaces after @data
  {
   line=in.readLine();
   m=p.matcher(line);
  }
  while(line!=null)
  {
   m = p2.matcher(line);
   if(m.matches())
   {
    row = line.split(",");
    data_matrix[i] = row;
    i++;
   }
   line = in.readLine();
  }
  in.close();
  return data_matrix;
 }

 public static void reduce_data(LinkedList<String> all_variables,String [][] data_matrix, int []reduct_indices_array, String reduced_data_arff_file_name) throws IOException
  {
   //all_variables is output by variables_names method of DataCube class

   Set<String> attrs_vals_set;
   int f_index=0;
   int last_index=0;
   FileWriter out;

   attrs_vals_set = new HashSet<String>();

   out = new FileWriter(reduced_data_arff_file_name);   
   out.write("@relation reduced_data\n");

   for(int k=0;k<reduct_indices_array.length;k++)
   {  
     for(int i=0;i<data_matrix.length;i++) 
     {
       attrs_vals_set.add(data_matrix[i][reduct_indices_array[k]]);
     }    
     out.write("@attribute "+all_variables.get(reduct_indices_array[k]) +" {");
     for(String val: attrs_vals_set)
     {
      out.write(val+",");
     }
     out.write("}\n");
     attrs_vals_set.clear();
   }
   out.write("@data\n");
   last_index = reduct_indices_array.length-1;
   for(int i=0;i<data_matrix.length;i++) 
   {  
    for(int k=0;k<last_index;k++)
    {
     out.write(data_matrix[i][reduct_indices_array[k]]+",");
    }  
    out.write(data_matrix[i][reduct_indices_array[last_index]]+"\n");
   }
   out.close();
  }

}
