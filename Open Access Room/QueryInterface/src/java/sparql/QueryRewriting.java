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

package sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class QueryRewriting
{
    private String originalSparql;
    private LinkedList<String> rewrittenSparql;
    private String clusterServiceURL = null;
    private Hashtable<String, JSONArray> clusters = null;
    private String url = "http://127.0.0.1:3030/test/sparql";
    
    public QueryRewriting()
    {
        clusters = new Hashtable<String, JSONArray>();
    }
    
    public String getClusterServiceURL() {
        return clusterServiceURL;
    }

    public void setClusterServiceURL(String clusterServiceURL) {
        this.clusterServiceURL = clusterServiceURL;
    }    
    
    public String getOriginalSparql() {
        return originalSparql;
    }

    public void setOriginalSparql(String originalSparql) {
        this.originalSparql = originalSparql;
    }

    public LinkedList<String> getRewrittenSparql() {
        return rewrittenSparql;
    }

    public void setRewrittenSparql(LinkedList<String> rewrittenSparql) {
        this.rewrittenSparql = rewrittenSparql;
    }

    public LinkedList<String> extractDimensions()
    throws IOException 
    {
        String tmpString = null;
        String[] tmpTriples = null;

        tmpString = originalSparql.substring(originalSparql.indexOf("{") + 1, originalSparql.indexOf("}"));        
        tmpTriples = tmpString.trim().split("properties");
        LinkedList<String> dimensions = new LinkedList<String>();
        
        for(int i=1;i<tmpTriples.length;i++)
        {
            System.out.println(tmpTriples[i]);
            System.out.println("---- "  + tmpTriples[i].substring(tmpTriples[i].indexOf("/")+1, tmpTriples[i].indexOf(">")));
            dimensions.add(tmpTriples[i].substring(tmpTriples[i].indexOf("/")+1, tmpTriples[i].indexOf(">")));
        }
        
        return dimensions;
    } 
       
    private String readJSONString(String path)
    throws MalformedURLException, IOException
    {
        String line = "";
        String jsonString = new String("");
        URL url= new URL(path);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        
        BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while ((line = rd.readLine()) != null)
	   jsonString+=line;
        
        return jsonString;
    }
    
    public void retrieveClusteredDrugs(LinkedList<String> dimensions)
    throws MalformedURLException, IOException
    {
        Iterator dimensionsIterator = dimensions.iterator();
        JSONArray jsonArray = null;
        JSONArray drugInformation = null;
        String dimension = null;
        
        System.out.println(drugInformation);
    
        while(dimensionsIterator.hasNext())
        {
            dimension = (String)dimensionsIterator.next();
            drugInformation = new JSONArray(readJSONString(this.clusterServiceURL + dimension));

            if(!drugInformation.isEmpty())
                clusters.put(dimension, drugInformation);
        }
        
        System.out.println(" --- " + clusters);
    }    
    
    public void produceQueriesDrugDimensions()
    throws IOException
    {
        String newSparql = null;
        LinkedList<String> dimensions = null;
        String dimension = null;
        JSONArray substitutionsArray = null;
        Iterator dimensionIterator = null;
        
        dimensions = extractDimensions();
        retrieveClusteredDrugs(dimensions);
        
        dimensionIterator = dimensions.iterator();
        
        while(dimensionIterator.hasNext())
        {
            dimension = (String)dimensionIterator.next();
            
            substitutionsArray = clusters.get(dimension);

            if(substitutionsArray!=null)
            {
                for(int i=0;i<substitutionsArray.length();i++)
                {
                    System.out.println(this.originalSparql.replaceAll(dimension, (String)((JSONObject)substitutionsArray.get(i)).get("CommonName")));
                }
            }
        }
    }
    
    public void produceQueriesSubclassDimensions()
    throws IOException, ClassNotFoundException
    {
        String newSparql = null;
        LinkedList<String> dimensions = null;
        String dimension = null;
        ResultSet results = null;
        String subclassedVariable = null;
        Iterator dimensionIterator = null;
        
        dimensions = extractDimensions();
        retrieveClusteredDrugs(dimensions);
        
        dimensionIterator = dimensions.iterator();
        
        while(dimensionIterator.hasNext())
        {
            dimension = (String)dimensionIterator.next();
            
            results = this.retrieveSubclasses(dimension);

            if(results!=null)
            {
                while(results.hasNext())
                {
                    QuerySolution binding = results.nextSolution();
                    subclassedVariable = binding.getResource("a").toString();
                    System.out.println(subclassedVariable);
                    subclassedVariable =  subclassedVariable.substring(subclassedVariable.indexOf("#")+1, subclassedVariable.length());
                    System.out.println(this.originalSparql.replaceAll(dimension, subclassedVariable));
                }                                                
            }
        }
    } 

    public ResultSet retrieveSubclasses(String dimension)
    throws ClassNotFoundException
    {
        ResultSet results = null;
        String subclassingQuery = "SELECT ?a\n" +
                                  "WHERE\n" +
                                  "{\n" +
                                  " ?a <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://hcls.deri.ie/l2s/sehr/1.0#" + dimension + ">.\n" +
                                  " ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>.\n" +
                                  "}";
        
        Query qr = QueryFactory.create(subclassingQuery);
        QueryExecution x = QueryExecutionFactory.sparqlService(this.url, qr);
        results = x.execSelect();
                
        return results;
    }    
    

}
