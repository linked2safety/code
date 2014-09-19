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
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyRepository
{
    private String url = "http://127.0.0.1:3030/test/query";
    private LinkedList<String> dimensions = null;
    private LinkedList<String> userTriples = null;    
    private String sparql = null;
    private String policySparql = null;     


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }    
    
    public String getPolicySparql() {
        return policySparql;
    }

    public void setPolicySparql(String policySparql) {
        this.policySparql = policySparql;
    }
    
    public String getSparql() 
    {
        return sparql;        
    }


    
    public void setSparql(String sparql) 
    {
        String tmpString = null;
        String[] tmpTriples = null;
        this.sparql = new String(sparql);
        
        tmpString = sparql.substring(sparql.indexOf("{") + 1, sparql.indexOf("}"));
        tmpTriples = tmpString.trim().split("l2s-dim");
        dimensions = new LinkedList<String>();
        
        for(int i=1;i<tmpTriples.length;i++)
        {
            System.out.println("---- " + " l2s-dim" + tmpTriples[i].split(" ")[0].trim());
            dimensions.add("l2s-dim" + tmpTriples[i].split(" ")[0].trim());
        }
        
    }    
    public void getUserProfileFromQuery()
    throws MissingUserProfileException
    {
        String tmpString = null;
        String[] tmpTriples = null;
        String[] lineTriples = null;
        String property = null;
        String value = null;
        String userProfileString[] = null;

        userProfileString = sparql.split("user_profile");

        if(userProfileString.length<2)
            throw new MissingUserProfileException();
        else
        {
            tmpString = userProfileString[1];
            tmpString = tmpString.substring(tmpString.indexOf("{") + 1, tmpString.indexOf("}"));
            lineTriples = tmpString.trim().split(" ");
            userTriples = new LinkedList<String>();
            
            if(lineTriples.length<1)
                throw new MissingUserProfileException();
            else
            {
                for(int i=1;i<lineTriples.length;i++)
                {                    
                    if((lineTriples[i].charAt(0)!='<') && (lineTriples[i].charAt(0)!='.'))
                    {
                            System.out.println(" ------------- " + (i % 2) + " line: " + lineTriples[i]);
                            if(i % 2==1)
                                property = new String(lineTriples[i].split(":")[1].trim());
                            else
                            {    
                                if(lineTriples[i].split(":").length>1)
                                {                                    
                                    value = lineTriples[i].substring(0, lineTriples[i].indexOf("."));                                    
                                }
                                else
                                {
                                    value = lineTriples[i].substring(lineTriples[i].indexOf("\""), lineTriples[i].indexOf("."));
                                }
                                userTriples.add(":" + property + " " + value);                                
                            }
                            
                    }
                }
            }
        }
    }    
    
    private void incorporateDimensions()
    {
        for(String dim : dimensions)
        {
            this.policySparql += "?condition lmds:hasDimension " + dim  + ". \n";
        }        
    }
    
    private void incorporatePolicy()
    {
        int varIndex = 0;
        
        for(String dim : userTriples)
        {
            this.policySparql += "?profile lmds" + dim + ". \n";
        }        
    }    
    
    private void formPolicingQuery()
    {
        this.policySparql = "prefix lmds: <http://www.linked2safety.eu/lmds2#> \n";
        this.policySparql += "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
        this.policySparql += "prefix acl: <http://www.w3.org/ns/auth/acl#> \n";
        this.policySparql += "prefix l2s-dim:<http://www.linked2safety-project.eu/properties/> \n";

        this.policySparql += "select ?graphuri ?sparqlendpoint where { \n";
        this.policySparql += "?accpolicy a lmds:AccessPolicy. \n";
        this.policySparql += "?accpolicy lmds:appliesToNamedGraph ?namedgraph. \n";
        this.policySparql += "?namedgraph lmds:hasGraphURI ?graphuri. \n";
        this.policySparql += "?namedgraph lmds:hasSparqlEndpoint ?sparqlendpoint. \n";

        
        this.policySparql += "?accpolicy lmds:grantsAccess acl:Read_l2s. \n";

        this.policySparql += "?accpolicy lmds:hasCondition ?condition. \n";
        incorporateDimensions();
        this.policySparql += "?accpolicy lmds:hasUserProfile ?profile.\n";
        incorporatePolicy();
        
        this.policySparql += " }\n";
    }   

    public LinkedList<String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(LinkedList<String> dimensions) {
        this.dimensions = dimensions;
    }

    public ResultSet performCredentialsAuthorization()
    {
        ResultSet results = null;
        
        this.formPolicingQuery();
        System.out.println(this.policySparql);
        Query qr = QueryFactory.create(this.policySparql);
        QueryExecution x = QueryExecutionFactory.sparqlService(this.url, qr);
        results = x.execSelect();

        return results.hasNext()==false ? null : results;
    }

}
