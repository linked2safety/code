/* AlgorithmParameters.java - defines the AlgorithmParameters class

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

public class AlgorithmParameters {
 
 String datacube_file;

public final static void main(String[]args) throws Exception
 {
  String target_variable = args[0];
  String class_index=null;
  int largest_index=0;

  AlgorithmParameters ap = new AlgorithmParameters(args[1]);
  //valid formats: '3:_diabetes' or 'diabetes' or '__ob____sq__Diabetes__sq____cb__' (Galaxy internal format of target variable) 
  class_index = ap.get_index_of_target_variable(target_variable);
  largest_index= ap.get_index_of_variable_with_largest_index();
  System.out.println("data cube: "+args[1]+"\nindex of specified target variable: "+class_index+"\n index of variable with the largest index: "+largest_index);
 }

 AlgorithmParameters(String data_cube_file)
 {
  datacube_file = data_cube_file;
 }

int get_index_of_variable_with_largest_index()
 {
  String all_variables=null;
  BufferedReader in=null;
  int index_of_variable_with_largest_index=0;

  try
  {
   in = new BufferedReader(new FileReader(this.datacube_file));
  }
  catch (FileNotFoundException e)
  {
   System.err.println(datacube_file+" is not found.");
   System.exit(-1);
  }
  try
  {
   all_variables=in.readLine();
  }
  catch(IOException e)
  {
   System.err.println("IOException in readLine()");
   System.exit(-1);
  }
  index_of_variable_with_largest_index = all_variables.split(",").length-1;//ignore the 'count' variable
  try
  {
    in.close();
  }
  catch(IOException e)
  {
   System.err.println("IOException in in.close()");
   System.exit(-1); 
  }
  return index_of_variable_with_largest_index;
 }

String get_index_of_target_variable(String target_variable)
 {
   Matcher m1, m2, m3;
   Pattern p1, p2, p3;
   String class_index=null;

   p1=Pattern.compile("^[0-9]+:_[a-zA-Z0-9]+$");//This pattern matches <target variable index>:_<target variable name> e.g. 3:_diabetes
   p2=Pattern.compile("^[a-zA-Z0-9]+$"); //This pattern matches a target variable name e.g. diabetes
   p3=Pattern.compile("^[_obsq]+([a-zA-Z0-9]+)[_sqcb]+$");//This pattern matches the Galaxy internal format of target variable e.g. __ob____sq__Diabetes__sq____cb__
  //The target variable is represented using internal format in a Galaxy workflow
   m1=p1.matcher(target_variable);
   if(m1.matches())//target variable format is 3:_diabetes
   {
    class_index = target_variable.split(":")[0];
   }
   else 
   {
    m2=p2.matcher(target_variable);
    if(m2.matches())//target variable format is diabetes
    {
     class_index = get_index_of_Target_Variable(target_variable);
    }
    else
    {
     m3 = p3.matcher(target_variable);
     if(m3.matches())
     {
      target_variable = m3.group(1);
      class_index = get_index_of_Target_Variable(target_variable);    
     }
     else
     {
      System.err.println("target variable: \""+target_variable+"\" is in wrong format.\n Valid target variable formats: \"3:_diabetes\" or \"diabetes\" or \" __ob____sq__Diabetes__sq____cb__\"\n Usage: java AlgorithmParameters <target variable> <data cube csv file>");
      System.exit(-1);
     }
   }
  }
 return class_index;
 }

 String get_index_of_Target_Variable(String target_variable)
 {
  String all_variables=null;
  String variable;
  StringTokenizer st;
  BufferedReader in=null;
  int variable_index=1;
  boolean target_variable_found=false;
  String class_index=null;
  
  try
  {
   in = new BufferedReader(new FileReader(this.datacube_file));
  }
  catch (FileNotFoundException e)
  {
   System.err.println(datacube_file+" is not found.");
   System.exit(-1);
  }
  try
  {
   all_variables=in.readLine();
  }
  catch(IOException e)
  {
   System.err.println("IOException in readLine()");
   System.exit(-1);
  }
  st=new StringTokenizer(all_variables,",");
  while(st.hasMoreTokens())
  { 
    variable = st.nextToken();
    if(variable.equals(target_variable))
    { 
     target_variable_found=true;
     break;
    }
    else
      variable_index++;
  }
  try
  {
    in.close();
  }
  catch(IOException e)
  {
   System.err.println("IOException in in.close()");
   System.exit(-1); 
  }
  if(target_variable_found)
   class_index = Integer.toString(variable_index);
  else
  { 
    System.err.println("target variable: \""+target_variable+"\" is not found in "+this.datacube_file);
    System.exit(-1);
  }
  return class_index;
 }
}
