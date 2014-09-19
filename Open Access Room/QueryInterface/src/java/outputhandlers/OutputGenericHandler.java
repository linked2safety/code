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
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class OutputGenericHandler
{
    protected PrintWriter pw = null;
    protected String mimeType = null; 
    protected Hashtable<String,Integer> cube = null;

    public Hashtable<String, Integer> getCube() {
        return cube;
    }

    public void setCube(Hashtable<String, Integer> cube) {
        this.cube = cube;
    }
       
    public PrintWriter getPw() {
        return pw;
    }

    public void setPw(PrintWriter pw) {
        this.pw = pw;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public OutputGenericHandler(PrintWriter writer)
    {
        pw = writer;
    }

    public void outputClusterData(ResultSet results, LinkedList<String> dimensions)
    {
        String varName = new String("");
        String str = new String("");
        Iterator cubeKeys;


        while(results.hasNext())
        {
            QuerySolution binding = results.nextSolution();
            handleRecord(binding);
        }
        
        cubeKeys = cube.keySet().iterator();
        
        while(cubeKeys.hasNext())
        {
            String key = (String)cubeKeys.next();
        }
    }    
    
    public void outputNumber(ResultSet results, LinkedList<String> dimensions)
    {
        results.hasNext();
        QuerySolution binding = results.nextSolution();
        pw.println("subjectcount");
        pw.println(binding.get("subjectcount").toString());
    }
    
    public void outputData(ResultSet results, LinkedList<String> dimensions)
    {
        String varName = new String("");
        String str = new String("");
        Iterator cubeKeys;
        
        for (int i=0;i<dimensions.size();i++)
        {
            varName = new String(dimensions.get(i));
            str += varName + ",";
        }
        
        pw.println(str + "Counts");        
        while(results.hasNext())
        {
            QuerySolution binding = results.nextSolution();
            handleRecord2(binding, results.getResultVars().size());
        }
        
        cubeKeys = cube.keySet().iterator();
        
        while(cubeKeys.hasNext())
        {
            String key = (String)cubeKeys.next();
            System.out.println(key + cube.get(key));
            pw.println(key + cube.get(key));
        }
    }
    
    public void printClusterCube(LinkedList<String> dimensions)
    {
        String varName = new String("");
        String str = new String("");
        Iterator cubeKeys;
        
        for (int i=0;i<dimensions.size();i++)
        {
            varName = new String(dimensions.get(i));
            
            str += varName + ",";
        }
        
        pw.println(str + "Counts");
        System.out.println(str + "Counts");

        cubeKeys = cube.keySet().iterator();
        
        while(cubeKeys.hasNext())
        {
            String key = (String)cubeKeys.next();
            System.out.println(key + cube.get(key));
            pw.println(key + cube.get(key));
        }
    }
    
    public abstract void handleRecord(QuerySolution binding);
    public abstract void handleRecord2(QuerySolution binding, int varNameLength);
    
}
