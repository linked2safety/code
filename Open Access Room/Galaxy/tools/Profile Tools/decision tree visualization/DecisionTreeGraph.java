/* DecisionTreeGraph.java - visualize a decision tree using a graph 

   Copyright (C) 2014 The University of Manchester tiand@cs.man.ac.uk
 
   This program is free software; you can redistribute it and/or modify it under
   the terms of the GNU General Public License as published by the Free Software Foundation;
   either version 3 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with this program; 
   if not, see <http://www.gnu.org/licenses>.

   Additional permission under GNU GPL version 3 section 7

   If you modify this Program, or any covered work, by linking or combining it with Graphivz (or a modified version of Graphivz), 
   containing parts covered by the terms of Eclipse Public License, Version 1, 
   the licensors of this Program grant you additional permission to convey the resulting work. 
   You may download the Graphivz source code along with associated license at: Graphivz home site 

   This component links to DecisionTrees.java which is under GNU GPL version 3. You may download the source code and associated license at: https://github.com/linked2safety/code under the DataMining directory.

   You may download the source code and associated license at  https://github.com/linked2safety/code
*/ 
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.System;

public class DecisionTreeGraph {
        static boolean root_node_added; //indicate whether the root node has been added to the hash table of nodes
	static String root_node_name;
	static int node_id;
	static HashMap<String,String> nodes; 
	//a previous condition of a node is the edge going into the node e.g. if edge mass > 26.4 goes into a node BMI, mass > 26.4 is the previous condition of BMI
	//A node can be identified using its previous condition plus the previous conditions of all the nodes before this node. 
 	//key: concatenation of the previous condition of a node and the previous conditions of all the nodes before this node 
	//value: concatenation of label of the node e.g. [label="rs0"] and name of the node in graphviz e.g. node12 is a node name in graphviz. "[label="rs0"]#node12" is a value
	static HashMap<String,String> leaf_nodes;        
	static HashMap<String,String> edges; 
	//An edge is identified using the pair of nodes
	//key: the pair of nodes of the edge e.g. "node0 -> node1"
	//value: the label of an edge e.g. [label="rs0=1"] 

	public final static void main(String[]args) throws Exception
	 {	    
	    String results_file=null;
	    String graphviz_file=null; //graphviz file of decision tree or random forest		  
	    results_file = args[0]; //output txt file of a Decision Trees tool 
	    graphviz_file = args[1];//graphviz file to create  
	    //initialize variables of nodes
	    root_node_added = false;
	    node_id = 0;
	    create_decision_tree_graph(results_file,graphviz_file);	    
	}	    

	static void create_decision_tree_graph(String results_file,String graphviz_file) throws IOException
	{
		 BufferedReader in; 
		 FileWriter out;
		 LinkedList<String> rule;  //elements of rule: rule[0]='a21 <= 16.76' rule[1]='a28 > 0.1108' rule[2]='a20 <= 0.004492' rule[3]='a7 <= 0.09061: 1 (6.0/1.0)'
         String line;
         
		 in=new BufferedReader(new FileReader(results_file));
		 out=new FileWriter(graphviz_file);
		 line = in.readLine();
		 out.write("digraph G {\n");
		  
		 nodes = new HashMap<String,String>();
		 leaf_nodes = new HashMap<String,String>();
		 edges = new HashMap<String,String>();
		 
		 LinkedList<String> rule_elements_and_next_line_and_rule_case=null;//store rule elements, next line and rule case
		 String rule_case=null; /*rule case 1: this line contains the 1st condition of a rule e.g. a21 <= 16.76
					          rule case 2: this line contains a rule which has only one condition e.g. a28 <= 0.1108: 1 (100.0)
					          rule case 3: this line contains the last condition of a rule and the RHS of the rule e.g. a28 <= 0.1108: 1 (100.0)
					          rule case 4: this line contains a middle condition of a rule e.g. |   a28 > 0.1108 */

		 rule = new LinkedList<String>();
		 DecisionTrees DT = new DecisionTrees();
		 
		 while(line!=null)
		 {	
		      try
			  {
		       rule_elements_and_next_line_and_rule_case = DT.extract_elements_of_rule(in, line, rule);
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
   	        	  get_nodes_edges(rule);
		         
			      if(line != null)//next line is not end of file
			      {
			       DT.last_condition_and_consequent(line, rule);	             
			      }
		 	    }
			    else if(rule_case.equals("only_one_condition_and_RHS"))
		        {		  
			       get_nodes_edges(rule);
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
		 try
		 {
		   create_graphviz_file(out);
		 }
		 catch (IOException e)
		 {
		  e.printStackTrace();
		  System.exit(0);
		 }
		 out.close();
	  }

static void create_graphviz_file(FileWriter out) throws IOException
  {
   	 //write nodes to graphviz file
	 try
	 {
	  write_nodes_to_graphviz_file(nodes,out);
	 }
	 catch (IOException e)
	 {
	   e.printStackTrace();
	   System.exit(0);
	 }
	 //write edges to graphviz file
	 try
	 {
	  write_edges_to_graphviz_file(edges,out);
	 }
	 catch (IOException e)
	 {
	  e.printStackTrace();
	  System.exit(0);
	 }
	 out.write("}");
  }

static void write_nodes_to_graphviz_file(HashMap <String,String> nodes, FileWriter out) throws IOException
  {
		String key=null;
		Iterator iter=null;
		String [] value=null;
		String values = null;
		String node_name=null;
		String node_label=null;
		String [] values2=null;

		iter = nodes.keySet().iterator();
		while(iter.hasNext())
		{
			   try
			   {
			    key = (String)iter.next();
			   }
			   catch (NoSuchElementException e)
			   {
			    e.printStackTrace();
			    System.exit(0);
			   }
			   values = nodes.get(key);
		       value = values.split("#");
			   node_name = value[1];
			   node_label = value[0];
	   	       out.write(node_name+" "+node_label+"\n");
		}

		iter = leaf_nodes.keySet().iterator();
		while(iter.hasNext())
		{
			  try
			   {
			    key = (String)iter.next();
			   }
			   catch (NoSuchElementException e)
			   {
			    e.printStackTrace();
			    System.exit(0);
			   }
			   values = leaf_nodes.get(key);
		       values2 = values.split("£");
			   if(values2 != null)
		       {
			      for(int i=0;i<values2.length;i++)
		 	      {      
		           value = values2[i].split("#");
			       node_name = value[1];
			       node_label = value[0];
	   	           out.write(node_name+" "+node_label+"\n"); 		 
			      }
		       }
			   else
			   {
			     value = values.split("#");
			     node_name = value[1];
			     node_label = value[0];
	   	         out.write(node_name+" "+node_label+"\n");
			   }
		  }  		
      }
	
static void write_edges_to_graphviz_file(HashMap <String,String> edges,FileWriter out)throws IOException
 {
		String key=null;
		Iterator iter=null;
		
		iter = edges.keySet().iterator();
		while(iter.hasNext())
		{
			   try
			   {
			    key = (String)iter.next();
			   }
			   catch (NoSuchElementException e)
			   {
			    e.printStackTrace();
			    System.exit(0);
			   }
			   out.write(key+" "+edges.get(key)+"\n");//write node0 -> node1 [label="rs0 = 1"];
		  }		
	}

static void get_nodes_edges(LinkedList<String> rule)
	{
	 /*
	 Nodes can be extracted from following rules: 
	 rule 1: plas <=127, mass > 26.4, age > 28 => 1
	 rule 2: plas >127, mass <= 29.9, plas > 145 => 1
	 rule 3: plas <=127, mass > 26.4, age <=28 => 1
	 nodes: plas, mass, mass, plas, age (two nodes have the same name 'mass')
	 previous condition of plas: none
	 previous condition of mass: plas <= 127
	 previous condition of mass: plas > 127
	 previous condition of plas: mass <= 29.9
	 previous condition of age: mass > 26.4
	 properties of node:  name of node e.g. node0
	 		      label of node e.g. "rs0"
	 		      previous condition of node e.g. plas <= 127
	 Edges can be extracted from above rules:
	 rule 1: plas -> mass, mass -> age, age -> 1
	 rule 2: plas -> mass, mass -> plas, plas -> 1
	 rule 3: plas -> mass, mass -> age, age -> 1
         properties of edge: the pair of nodes of edge e.g. node0 -> node1
			     label of edge e.g. [label="rs0=1"]
	 */
	 
	 Pattern p1=null;
	 Matcher m1=null;
	 String first_condition=null;
	 String previous_condition_of_current_node=null;
	 String previous_conditions_of_current_node;
	 String previous_conditions_of_previous_node=null;
	 String outcome=null;
	 String root_node=null;
	 String key=null;
	 String value="";
	 
	 String previous_node_name=null;
     String current_node_name=null;
	 String item;
	 String variable;
	 String values="";
	 int rule_size=0;

	 p1 = Pattern.compile("^\\s*(([\\w\\p{Punct}]+)\\s*=\\s*[\\w\\p{Punct}]+)\\s*$");
	 
	 first_condition=rule.get(0);

	 //add root node to nodes hash table
	 if(root_node_added == false)
	 {	 
      m1 = p1.matcher(first_condition);
	  if(m1.matches())
	  {
  	   root_node=m1.group(2);
	  }
  	  key = "none";//previous condition of root node is set to "none"
	  value = "[label=\""+root_node+"\"]#node"+Integer.toString(node_id);
	  nodes.put(key,value);
 	  root_node_added = true;
	  root_node_name = value;
	 }
	 previous_condition_of_current_node = first_condition; 
	 previous_conditions_of_previous_node = "none";//previous condition of root node is "none"
	 
	 //loop:
	 //     get next node from the rule
     //     create the edge between previous node and this node 	
	 rule_size=rule.size();
	 previous_conditions_of_current_node="";
	 for(int i=1;i<rule_size;i++)
	 {
	     item = rule.get(i);
         m1 = p1.matcher(item);
	     if(m1.matches())
	     {
	       //extract the node from the condition
	       variable = m1.group(2);
	       previous_conditions_of_current_node = previous_conditions_of_current_node+","+previous_condition_of_current_node; 
	       key = previous_conditions_of_current_node;
	       
	       if(nodes.containsKey(key)==false)//the current node is new
	       {
 		    node_id++;
	    	value = "[label=\""+variable+"\"]#node"+Integer.toString(node_id);
	       	nodes.put(key,value);
		    current_node_name ="node"+Integer.toString(node_id);
		    value = nodes.get(previous_conditions_of_previous_node);
		    previous_node_name = value.split("#")[1];
		    create_edge(previous_node_name,current_node_name,previous_condition_of_current_node);
  		   }
	       previous_conditions_of_previous_node = previous_conditions_of_current_node;
	       previous_condition_of_current_node = item;					       
	     }
	     else //the item is the consequent of the rule
	     {
  	        node_id++;
	        key = previous_conditions_of_current_node;
	        if(leaf_nodes.containsKey(key))
	        {	      
	          values = leaf_nodes.get(key); 
	          values = values+"£"+"[label= \""+item+"\",shape=box];#node"+Integer.toString(node_id);
	          leaf_nodes.put(key,values);
	        }
	        else
	        {
	          value = "[label= \""+item+"\",shape=box];#node"+Integer.toString(node_id);
	          leaf_nodes.put(key,value);
	        }
	        current_node_name = "node"+Integer.toString(node_id);  
	        value = nodes.get(previous_conditions_of_previous_node);
		    previous_node_name = value.split("#")[1];	        
		    create_edge(previous_node_name,current_node_name,previous_condition_of_current_node);	     
	    }
	 }	 
    }

static void create_edge(String previous_node_name,String current_node_name,String previous_condition_of_current_node) 
 {
  String key = previous_node_name+" -> "+current_node_name;
  String value = "[label=\""+previous_condition_of_current_node+"\"];";
  edges.put(key,value);
 }
}
