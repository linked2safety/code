/*
 *

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


package repository;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import java.util.LinkedList;

public class UpdateCategories
{
    private String url = "http://127.0.0.1:3030/l2s/query";
    private LinkedList<String> dimensions = null;
    private String sparql = null;

    public UpdateCategories()
    {
        this.sparql = new String("select ?a ?b\n" +
                "where\n" +
                "{\n" +
                " ?a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Datatype>.\n" +
                " ?a <http://www.w3.org/2000/01/rdf-schema#comment> ?b.\n" +
        "}");
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


    public ResultSet retrieveCategories()
    {
        ResultSet results = null;
        
        System.out.println(this.sparql);
        Query qr = QueryFactory.create(this.sparql);
        QueryExecution x = QueryExecutionFactory.sparqlService(this.url, qr);
        results = x.execSelect();
        
        while(results.hasNext())
        {
            QuerySolution binding = results.nextSolution();
            System.out.println(" instance :" + binding.get("b").toString() );
        }
        
        return null;
    }
    
    public static void main(String[] args)
    {
        UpdateCategories u = new UpdateCategories();
        u.retrieveCategories();
    }
}
