/*
 *   Copyright (C) 2014 Panagiotis Hasapis (Panagiotis.hasapis@intrasoft-intl.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   This component links (bundles) to Apache Jena, which is available under Apache Licence 2.
   You may download the source code and associated license at https://jena.apache.org/
    
   The source files are available at:  https://github.com/linked2safety/code 
   */

package outputhandlers;

import com.hp.hpl.jena.query.QuerySolution;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

public class OutputCSVHandler
extends OutputGenericHandler
{
    public OutputCSVHandler(PrintWriter pw)
    {
        super(pw);
        super.cube = new Hashtable<String, Integer>();
        super.setMimeType("text/csv");
    }   
    
    @Override
    public void handleRecord(QuerySolution binding)
    {
        String varName = null;
        String str = new String();
        int varNameLength = 0;
        Integer prevCounts = null;
        Integer counts = null;
        int i;

        System.out.println("TEST");
        for (Iterator varNameIterator = binding.varNames(); varNameIterator.hasNext(); varNameIterator.next(),varNameLength++) {;}

       
        for (i=0;i<varNameLength-1;i++)
        {
            varName = new String("v_" + i);
            
            if(binding.get(varName)==null)
            {
                System.out.println(varNameLength +" NULL---------- " + "v_" + i);
                str+=",";
            }
            else
                str += binding.get(varName).toString() + ",";
            System.out.println(" ---------- " + "v_" + i);
            
        }
        
        counts = new Integer(binding.get("v_" + i).toString());

        if((prevCounts=cube.get(str))==null)
        {            
            cube.put(str, counts);
        }
        else
        {
            prevCounts = prevCounts + counts;
            cube.put(str, prevCounts);
        }         
    }

    public void handleRecord2(QuerySolution binding, int varNameLength)
    {
        String varName = null;
        String str = new String();
        Integer prevCounts = null;
        Integer counts = null;
        int i;

       
        for (i=0;i<varNameLength-1;i++)
        {
            varName = new String("v_" + i);
            
            if(binding.get(varName)==null)
            {
                str+=",";
            }
            else
                str += binding.get(varName).toString() + ",";            
        }
        
        counts = new Integer(binding.get("v_" + i).toString());

        if((prevCounts=cube.get(str))==null)
        {            
            cube.put(str, counts);
        }
        else
        {
            prevCounts = prevCounts + counts;
            cube.put(str, prevCounts);
        }  
    }    
}
