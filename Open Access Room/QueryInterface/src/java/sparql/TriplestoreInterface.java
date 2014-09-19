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
import com.hp.hpl.jena.query.ResultSetFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import outputhandlers.OutputCSVHandler;
import outputhandlers.OutputGenericHandler;

public class TriplestoreInterface
{
    private String url = "";
    private String sparql = null;
        
    public TriplestoreInterface()
    {
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }    
    
    public String getSparql() 
    {
        return sparql;        
    }

    public ResultSet retrieveSPARQLResults()
    throws ClassNotFoundException
    {
        ResultSet results = null;
        
        System.out.println(this.sparql);
        Query qr = QueryFactory.create(this.sparql);
        QueryExecution x = QueryExecutionFactory.sparqlService(this.url, qr);
        results = x.execSelect();
                
        return results;
    }
    
    
    public static LinkedList<String> sSparql(String sparql) 
    {
        String tmpString = null;
        String[] tmpTriples = null;
        
        tmpString = sparql.substring(sparql.indexOf("{") + 1, sparql.indexOf("}"));
        tmpTriples = tmpString.trim().split("l2s-dim");
        LinkedList<String> dimensions = new LinkedList<String>();        
        dimensions = new LinkedList<String>();
        
        for(int i=1;i<tmpTriples.length;i++)
        {
            tmpTriples[i] = tmpTriples[i].split(" ")[0];
            dimensions.add(tmpTriples[i].substring(1,tmpTriples[i].length()));
        }   
        
        return dimensions;
    } 
}
