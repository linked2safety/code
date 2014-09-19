/*
    Knowleddge Base for Galaxy

    Copyright (C) 2014  Panagiotis Hasapis (Panagiotis.hasapis@intrasoft-intl.com)

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

*/

package triplestore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

public class TriplestoreConnector
{
    private static int limitResultsThreshold = 10;
    private String url = "http://127.0.0.1:3030/linkeddata/sparql";
    private String sparql = null;
        
    public TriplestoreConnector()
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

    public ResultSet retrieveLinkedData(String rule)
    throws ClassNotFoundException
    {
        this.sparql = new String("select ?label ?url\n" +
                                 "where \n" +
                                 "{ \n" +
                                 "?url <http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/class> ?dis.\n" +
                                 "?url <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
                                 "FILTER regex(?label,\"" + rule + "\",\"i\").\n" +
                                  "} limit " + limitResultsThreshold);
        
        return this.retrieveSPARQLResults();
    }
    
    public static void main(String[] arg)
    {
        String q = new String("select ?v_0 ?v_1 ?v_2 \n" +
                                "where\n" +
                                "{\n" +
                                "  ?a a <http://purl.org/linked-data/cube#Observation> .\n" +
                                "  ?a <http://www.linked2safety-project.eu/properties/cancer> ?v_0.\n" +
                                "  ?a <http://www.linked2safety-project.eu/properties/patienthasdiabetes> ?v_1.\n" +
                                "  ?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?c. \n" +
                                "  BIND(STR(?c) AS ?v_2). \n" +
                                "}");
                
    } 

}
