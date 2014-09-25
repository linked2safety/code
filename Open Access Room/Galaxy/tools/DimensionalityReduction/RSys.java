/* 
   RSys.java - defines the RSys class 

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
  
   The source files are available at: https://github.com/linked2safety/code
*/
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public final class RSys
{
  ArrayList<BitSet>[] partitions; //partitions for each individual attribute
  ArrayList<BitSet> current;
  int attributes;
  int objects;
  BitSet remaining;
  BitSet empty;
  ArrayList<BitSet> R_ind;
  int class_index0;
  int current_missing;
  int best_missing;
  boolean [] discObjects;
  boolean failed;
  boolean bailed;

  public RSys(String [][] data, int rows, int cols, int class_index) 
  {
    discObjects = new boolean[rows];
    objects = rows;
    attributes = cols;
    class_index0 = class_index;
    partitions = new ArrayList [attributes];
    constructPartitions(data);
  }

  public void constructPartitions(String [][] data)
  {
    BitSet eq;
    for(int a=0;a<attributes;a++) {
      partitions[a] = new ArrayList<BitSet>();
      remaining = new BitSet(objects);
      for (int i=0;i<objects;i++) remaining.set(i);

      for( int o1 = 0; o1<objects; o1++ ) {
        if( remaining.get(o1) )
        {
          eq =  new BitSet(objects);
          eq.set(o1);
          for(int o2 = o1+1;o2<objects;o2++)
            if(remaining.get(o2))
              if((data[o1][a]).equals(data[o2][a]))
   	      {
                eq.set(o2);
		remaining.clear(o2);
	      }
            remaining.clear(o1);
	    partitions[a].add(eq);
        }
      }
    }
  }

  public ArrayList<BitSet> partition(ArrayList a1) 
  { //xintersect partitions[a] with R_ind
    Iterator IND_a = a1.iterator();//partitions[a]
    BitSet temp = null;
    ArrayList<BitSet> newPartition = new ArrayList<BitSet>();
    Iterator IND_reduct;

    while (IND_a.hasNext()) {
      BitSet t = (BitSet)IND_a.next();
      IND_reduct = R_ind.iterator();

      while (IND_reduct.hasNext()) {
	temp = (BitSet)t.clone();
	temp.and((BitSet)IND_reduct.next());
	if (!temp.equals(empty)) {
	  newPartition.add(temp);
	}
      }
    }
    return newPartition;
  }

  //if b1 subset= b2 then return no. of object in b1
  public BitSet intersects(BitSet b1, BitSet b2) 
  {
    BitSet ret=null;
    boolean flag=false;

    for(int i=0;i<objects;i++)
    {
      if(b1.get(i)&&b2.get(i))
      {
	ret = b1;
	break;
      }
    }
    return ret;
  }

  //if b1 subset= b2 then return no. of object in b1
  public float subset(BitSet b1,BitSet b2) 
 {
    float ret=0;
    boolean[] discTemp = discObjects;

    for(int i=0;i<objects;i++) 
    {
      if(b1.get(i))
      {
	if(!b2.get(i))
        {
	  ret=0;
	  break;
	}
	else
       {
	ret++;
       }
      }
    }
    return ret;
  }

  public int size(BitSet b)
  {
    int counter=0;
    for(int i=0;i<objects;i++)
    {
      if(b.get(i))
      {
       counter++;
      }
    }
    return counter;
  }

 public float calculateGamma(int a) 
 {
    current = partition(partitions[a]);
    Iterator e = current.iterator();
    BitSet temp = null;
    float gamma = 0;
    float tempGamma = 0;
    Iterator f;
    
    while(e.hasNext())
    {
      temp = (BitSet)e.next();
      f = partitions[class_index0].iterator(); //partition using decision variable
      failed=false;
      tempGamma=0;
      while(f.hasNext())
      {
	tempGamma += subset(temp,(BitSet)f.next());
      }
      if(tempGamma == 0)
      {
	current_missing += size(temp);
	if(current_missing >= best_missing)
        { 
 	 bailed=true;
         break;
        }	
      }
      else gamma+=tempGamma;
    }
    return gamma/objects;
  }
 
 public String quickReduct()
 {
    BitSet reduct = new BitSet(attributes);
    empty = new BitSet(objects);
    int best=-1;
    float gamma = 0;
    float bestGamma=-1;
    best_missing=objects;
    BitSet temp=null;
    float max_so_far=-1;
    ArrayList T_ind;

    R_ind = new ArrayList<BitSet>();
    BitSet t = new BitSet(objects);
    for(int i=0;i<objects;i++) 
      t.set(i);
    R_ind.add(t);
     
    while(true) 
    {
      for(int a=0;a<attributes;a++) 
      {
	if(!reduct.get(a)&&a!=class_index0)
 	{
	  temp=(BitSet)reduct.clone();
	  temp.set(a);
	  T_ind=(ArrayList)R_ind.clone();
	  current_missing=0;
	  gamma=calculateGamma(a);
          //System.out.println(" gamma of selecting "+a+" = "+gamma);
	  if(gamma>bestGamma)
	  {
	    bestGamma=gamma;
	    best=a;
	    best_missing=current_missing;
	    if(gamma==1)
              break;
	  }
	}
      }
      if(max_so_far>=bestGamma)//previous gamma better than new gamma   
         break;
      else
      {
	max_so_far = bestGamma;
	reduct.set(best);
	//System.out.println("gamma of "+reduct.toString()+" = "+bestGamma);
        if(bestGamma==1)
        {
          break;
        }
	R_ind = partition(partitions[best]);
      }
    }
    reduct.set(class_index0);
    //System.out.println("gamma of "+reduct.toString()+" = "+bestGamma);    
    return reduct.toString();   
  }
}
