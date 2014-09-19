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

package services;

import com.hp.hpl.jena.query.QuerySolution;
import database.KnowledgeBaseConnector;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.apache.commons.io.IOUtils;

import triplestore.TriplestoreConnector;

@Path("knowbase")
public class KnowbaseResource {

    @Context
    private UriInfo context;

    public KnowbaseResource() {
    }

    @GET
    @Path("requestCluster/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String requestCluster(@PathParam("id")String id)
    throws MalformedURLException, IOException
    {
        JSONArray response = new JSONArray();
        JSONObject jo = (JSONObject) new JSONTokener(IOUtils.toString(new URL("http://lisis.cs.ucy.ac.cy:9090/LisisSafetyExternalInterface/rest/clusters/latest"))).nextValue();
        JSONArray arrayInternal = null;
        int index = 0;
        
        JSONArray jsonArray = jo.getJSONArray("Clusters");
        Iterator iter = jsonArray.iterator();
        
        while(iter.hasNext())
        {
            arrayInternal = (JSONArray)iter.next();
            Iterator iterInternal = arrayInternal.iterator();
            
            while(iterInternal.hasNext())
            {
                JSONObject o = (JSONObject)iterInternal.next();
                
                if(o.getString("ClusterGroup").equals(id))
                {
                    response.put(index++, o.getString("CommonName"));
                    System.out.println(o.toString());
                }
            }
        }
        
        return "{" + response + "}";
    }    
    
    
    @GET
    @Path("requestAssocRules/{rule}")
    @Produces(MediaType.APPLICATION_JSON)
    public String requestAssocRules(@PathParam("rule")String rule)
    {
        KnowledgeBaseConnector knowledgeBaseConnector;
        JSONObject jsonReply = new JSONObject();
        JSONArray  assocArray = new JSONArray();
        
        try 
        {
            ResultSet rs;
            
            knowledgeBaseConnector = new KnowledgeBaseConnector();
            rs = knowledgeBaseConnector.searchAssociations(rule);
            
            while(rs.next())
            {
                assocArray.put(rs.getString("association"));
                System.out.println(rs.getString("association"));
            }
            
            jsonReply.put("associations", assocArray);
            System.out.println(jsonReply.toString());
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex) 
        {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (InstantiationException ex) 
        {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) 
        {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        return jsonReply.toString();
    }    
    
    @GET
    @Path("requestLinkedData/{pred}")
    @Produces(MediaType.APPLICATION_JSON)
    public String requestLinkedData(@PathParam("pred")String pred)
    {
        JSONObject jsonReply = new JSONObject();
        JSONArray  assocArray = new JSONArray();
        
        try
        {
            TriplestoreConnector triplestoreConnector = new TriplestoreConnector();
            JSONObject result;
            com.hp.hpl.jena.query.ResultSet rs = triplestoreConnector.retrieveLinkedData(pred);

            while(rs.hasNext())
            {
                QuerySolution binding = rs.nextSolution();
                for(Iterator varNameIterator = binding.varNames(); varNameIterator.hasNext(); varNameIterator.next())
                {
                    result = new JSONObject();
                    result.put("label", binding.getLiteral("label").toString());
                    result.put("url",binding.getResource("url").toString());
                    assocArray.put(result);
                }

                jsonReply.put("linkedrules", assocArray);
            }            
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(KnowbaseResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return jsonReply.toString();        
    }

    @GET
    @Path("combinedlinkedrules/{rule}")
    @Produces(MediaType.APPLICATION_JSON)
    public String combinedLinkedRules(@PathParam("rule")String rule)
    {
        String jsonRules = requestAssocRules(rule);
        String linkedRules = requestLinkedData(rule);
        
        return ("[" + jsonRules + ", " + linkedRules + "]");
    }    
    
}
