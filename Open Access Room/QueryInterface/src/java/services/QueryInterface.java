
package services;

import beans.WorkflowInformation;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import database.Users;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONTokener;
import org.apache.commons.io.IOUtils;
import outputhandlers.OutputCSVHandler;
import outputhandlers.OutputGenericHandler;
import sparql.TriplestoreInterface;


@WebServlet(name = "QueryInterface", urlPatterns = {"/query"})
public class QueryInterface
extends HttpServlet
{
    private TriplestoreInterface triplestoreInterface = null;
    private String workflowFolderPath = null;
    private String tmpXMLBasePath = null;
    private JSONArray clusterDrug = null;
    private String[] drugClasses = {"receivedantidepressantdrugs","receivedantihypertensivedrug", "receivedantimanicdrugs"};
    private String SPARQLURL = "";
    private String drugClass = null;
    private String clusterID = null;

    
    public void init()
    {
        triplestoreInterface = new TriplestoreInterface();
        try {
            this.getPropertyValues();
        } catch (IOException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        WorkflowInformation workflowInformation = null;
        String sparqlQuery = request.getParameter("sparql");
        System.out.println("SPARQL1 = " + sparqlQuery);
        OutputGenericHandler outputStream = new OutputCSVHandler(response.getWriter());
        response.setContentType(outputStream.getMimeType());
        Iterator iterator;
        LinkedList<String> clusterQueries = null;
        LinkedList<String> subQueries = null;        
        LinkedList<String> dimensions = setSparql(sparqlQuery);
        boolean performCluster = false;
        ResultSet results = null;        
        String actorName;
        String logLine;
        String timeStamp = null;
        String tmpFilename = null;
        PrintWriter pw = null;
        File f = null;        

        try
        {

            performCluster = sparqlQuery.startsWith("cluster");
                    
            if(performCluster)
            {
                sparqlQuery = sparqlQuery.substring(7, sparqlQuery.length());
                LinkedList<String> d = this.setSparql(sparqlQuery);
                this.readTestClusters(this.findDrugInDimensions(d));
                clusterQueries = this.createClusterQueries(sparqlQuery, d);

                clusterQueries.add(sparqlQuery);
                iterator = clusterQueries.iterator();

                while(iterator.hasNext())
                {
                    String sp = (String)iterator.next();
                    System.out.println("--- " + sp);
                    URL url = new URL(SPARQLURL + URLEncoder.encode(sp).replaceAll("\\+","%20"));

                    URLConnection urlconnection = url.openConnection();
                    urlconnection.connect();
                    results = ResultSetFactory.fromXML(urlconnection.getInputStream());
                    outputStream.outputClusterData(results, d);
                }
                this.replaceDrugByCluster(d);
                outputStream.printClusterCube(d);                
            }
            else if(sparqlQuery.startsWith("count"))
            {
                System.out.println("inside count ");

                sparqlQuery = sparqlQuery.substring(5, sparqlQuery.length());
                URL url = new URL(SPARQLURL + URLEncoder.encode(sparqlQuery).replaceAll("\\+","%20"));            

                URLConnection urlconnection = url.openConnection();
                urlconnection.connect();
                results = ResultSetFactory.fromXML(urlconnection.getInputStream());
                outputStream.outputNumber(results, dimensions);
            }   
            else if((drugClass=this.checkIfAggregatedCategory(dimensions))!=null)
            {
                    String sparqlQueryBare = sparqlQuery.split("!")[1];
                    LinkedList<String> d = this.setSparql(sparqlQueryBare);
                    subQueries = this.createSubclassQueries(sparqlQueryBare, d, drugClass);

                    subQueries.add(sparqlQueryBare);
                    iterator = subQueries.iterator();

                    while(iterator.hasNext())
                    {
                        String sp = (String)iterator.next();
                        System.out.println("--- " + sp);
                        
                        URL url = new URL(SPARQLURL + URLEncoder.encode(sp).replaceAll("\\+","%20"));

                        URLConnection urlconnection = url.openConnection();
                        urlconnection.connect();
                        results = ResultSetFactory.fromXML(urlconnection.getInputStream());
                        outputStream.outputClusterData(results, d);
                    }
                outputStream.printClusterCube(d);                
         
            }
            else
            {
                dimensions = this.setSparql(sparqlQuery.split("!")[1]);
                System.out.println("DIMENSIOS:" + dimensions);
                URL url = new URL(SPARQLURL + URLEncoder.encode(sparqlQuery.split("!")[1]).replaceAll("\\+","%20"));            
                
                URLConnection urlconnection = url.openConnection();
                urlconnection.connect();
                results = ResultSetFactory.fromXML(urlconnection.getInputStream());
                outputStream.outputData(results, dimensions);
                Users users = new Users();
                users.insertLogfileline(sparqlQuery.split("!")[0], sparqlQuery.split("!")[1]);
            }
            
            actorName = new String("testuser");
            logLine = actorName + " | " + sparqlQuery;                        
            Logger.getLogger(TriplestoreInterface.class.getName()).log(Level.INFO, logLine);
        } 
        catch (Throwable ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String checkIfAggregatedCategory(LinkedList<String> dimensions)
    {
        Iterator<String> dims = dimensions.iterator();
        String dim = null;
        
        while(dims.hasNext())
        {
            dim = dims.next();
            for(int i=0;i<drugClasses.length;i++)
            {
                if(dim.startsWith(drugClasses[i]))
                {
                    return drugClasses[i];
                }
            }
        }
        
        return null;
    }
 
    public LinkedList<String> createSubcategoriesQueries(String sparql, LinkedList<String> dimensions, String drugClass)
    {
        String dim = null;
        String drugName = null;
        String singleDrugName = null;
        JSONObject jsonObject = null;
        LinkedList<String> subQueries = new LinkedList<String>();
        Iterator<String> dims = dimensions.iterator();
        Iterator<JSONObject> singleDrugIterator = null;
                       
        this.readClusters();
        singleDrugIterator = clusterDrug.iterator();
                
        while(singleDrugIterator.hasNext())
        {
            jsonObject = (JSONObject)singleDrugIterator.next();
            singleDrugName = jsonObject.getString("CommonName").toLowerCase();
                    
            subQueries.add(new String(sparql.replaceAll(drugClass, "received"+singleDrugName)));
        }
        
        return subQueries;
    }    

    public LinkedList<String> createSubclassQueries(String sparql, LinkedList<String> dimensions, String drugClassName)
    {
        try {
            String dim = null;
            JSONArray arraySubs = null;
            JSONObject subpropertyObject = null;
            LinkedList<String> subQueries = new LinkedList<String>();
            Iterator<String> dims = dimensions.iterator();
            String subClassName = null;
            
            arraySubs = this.getsubClasses(drugClassName);
            
            Iterator iter = arraySubs.iterator();

            while(iter.hasNext())
            {
                subpropertyObject = (JSONObject)iter.next();
                subClassName = subpropertyObject.getJSONObject("subproperty").getString("value").split("#")[1];
                subQueries.add(new String(sparql.replaceAll(drugClassName, subClassName.toLowerCase())));
            }        
                        
            return subQueries;
        } catch (MalformedURLException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public LinkedList<String> createClusterQueries(String sparql, LinkedList<String> dimensions)
    {
        String query = null;
        String dim = null;
        String drugName = null;
        String clusteredDrugName = null;
        JSONObject jsonObject = null;
        LinkedList<String> clusterQueries = new LinkedList<String>();
        Iterator<String> dims = dimensions.iterator();
        Iterator<JSONObject> clusterIterator = null;
        
        while(dims.hasNext())
        {
            dim = dims.next();
            
            if(dim.startsWith("received"))
            {
                drugName = dim.substring(8, dim.length());
                
                //get cluster
                
                clusterIterator = clusterDrug.iterator();
                
                while(clusterIterator.hasNext())
                {
                    jsonObject = (JSONObject)clusterIterator.next();
                    clusteredDrugName = jsonObject.getString("CommonName").toLowerCase();
                    
                    clusterQueries.add(new String(sparql.replaceAll(dim, ("received"+clusteredDrugName))));
                }
            }
        }
        
        return clusterQueries;
    }
   
    public void replaceDrugByCluster(LinkedList<String> dimensions)
    {
        Iterator<String> dims = dimensions.iterator();
        String clusterName = null;
        String dim = null;
        int indexNum = -1;
        
        while(dims.hasNext())
        {
            dim = dims.next();
            clusterName = readClusterName(dim);
            indexNum++;
            
            if(dim.startsWith("received"))
            {                    
                dimensions.set(indexNum, clusterName);
            }
        }        
    }

    public String findDrugInDimensions(LinkedList<String> dimensions)
    {
        Iterator<String> dims = dimensions.iterator();
        String clusterName = null;
        String dim = null;
        
        while(dims.hasNext())
        {
            dim = dims.next();
            
            if(dim.startsWith("received"))
            {                    
                return dim.substring(8,dim.length());
            }
        }
        
        return null;
    }
    
    
    public void readClusters()
    {
        clusterDrug = new JSONArray("[{\"Source\":\"Source5\",\"CommonName\":\"aspirin\",\"CanonicalSMILE\":\"weiruewiruoweirowem\",\"SourceID\":\"5\"}]");
    }

    public void readTestClusters(String drugName)
    throws IOException
    {
        JSONObject jo = (JSONObject) new JSONTokener(IOUtils.toString(new URL("http://lisis.cs.ucy.ac.cy:9090/LisisSafetyExternalInterface/rest/clusters/drug/" + drugName))).nextValue();
        clusterDrug = new JSONArray(jo.getJSONArray("cluster"));
        this.clusterID = jo.getString("clusterID");
    }
     
    public String readClusterName(String drugName)
    {
        return new String("drugCluster" + this.clusterID + "_v1");
    }
        
    public LinkedList<String> setSparql(String sparql)
    throws IOException 
    {
        String tmpString = null;
        String[] tmpTriples = null;
        
        System.out.println("SPARQL=" + sparql);
        tmpString = sparql.substring(sparql.indexOf("{") + 1, sparql.indexOf("}"));
        tmpTriples = tmpString.trim().split("l2s-dim");
        LinkedList<String> dimensions = new LinkedList<String>();        
        dimensions = new LinkedList<String>();
        
        for(int i=1;i<tmpTriples.length;i++)
        {
            tmpTriples[i] = tmpTriples[i].split(" ")[0];
            System.out.println("---- " + tmpTriples[i].substring(1,tmpTriples[i].length()));            
            dimensions.add(tmpTriples[i].substring(1,tmpTriples[i].length()));
        }   
        
        return dimensions;        
    } 
    
    private void storeDatabaseLogInfo(WorkflowInformation workflowInformation, String sparql)
    {
        try
        {                    
            Process p = Runtime.getRuntime().exec(new String[]{"python",
                                                               this.workflowFolderPath,
                                                               workflowInformation.getUserEmail(),
                                                               workflowInformation.getToolInputFile(),
                                                               workflowInformation.getToolOutputFile(),
                                                               workflowInformation.getToolXMLDescription(),
                                                               workflowInformation.getStoreWorkflow()});
            p.waitFor();
            
            BufferedReader reader = 
                     new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = new String();			
            while ((line = reader.readLine())!= null)
                    System.out.println(line);
            
        } catch (IOException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }        

    protected void saveXMLFile(String sparql)
    throws FileNotFoundException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String tmpFilename = tmpXMLBasePath + "/data" + (int)(10000*Math.random()) + ".xml";
        PrintWriter pw = new PrintWriter(tmpFilename);
        File f = null;
        
        pw.println("<Dataset SPARQL_query=\"" + sparql + "\" time=\"" + timeStamp + " />");
        pw.close();
        
        //f = new File(tmpFilename);
        //f.delete();
    }
    
    public void getTest(String sparqlQuery) throws IOException
    {
        System.out.println("SPARQL1 = " + sparqlQuery);
        OutputGenericHandler outputStream = new OutputCSVHandler(new PrintWriter(System.out));
        Iterator iterator;
        LinkedList<String> clusterQueries = null;
        LinkedList<String> subQueries = null;        
        LinkedList<String> dimensions = this.setSparql(sparqlQuery);
        boolean performCluster = false;
        ResultSet results = null;        
        String actorName;
        String logLine;
        String timeStamp = null;
        String tmpFilename = null;
        PrintWriter pw = null;
        File f = null;   
        
        if((drugClass=this.checkIfAggregatedCategory(dimensions))!=null)
        {
                    LinkedList<String> d = this.setSparql(sparqlQuery);
                    //this.readClusters();
                    subQueries = this.createSubclassQueries(sparqlQuery, d, drugClass);

                    subQueries.add(sparqlQuery);
                    iterator = subQueries.iterator();

                    while(iterator.hasNext())
                    {
                        String sp = (String)iterator.next();
                    }
             
        }
    }

    public void getTest2(String sparqlQuery) throws IOException
    {
        OutputGenericHandler outputStream = new OutputCSVHandler(new PrintWriter(System.out));
        Iterator iterator;
        LinkedList<String> clusterQueries = null;
        LinkedList<String> subQueries = null;        
        LinkedList<String> dimensions = this.setSparql(sparqlQuery);
        boolean performCluster = false;
        ResultSet results = null;        
        String actorName;
        String logLine;
        String timeStamp = null;
        String tmpFilename = null;
        PrintWriter pw = null;
        File f = null;   

                dimensions = this.setSparql(sparqlQuery);

                URL url = new URL(SPARQLURL + URLEncoder.encode(sparqlQuery).replaceAll("\\+","%20"));            

                URLConnection urlconnection = url.openConnection();
                urlconnection.connect();
                results = ResultSetFactory.fromXML(urlconnection.getInputStream());
                outputStream.outputData(results, dimensions);        
    }
    
    protected void getPropertyValues()
    throws IOException
    {
        Properties prop = new Properties();
        String propFileName = "/etc/triplestoreinterface.properties";
 
        InputStream inputStream = new FileInputStream(propFileName);        
        prop.load(inputStream);
        
        workflowFolderPath = prop.getProperty("workflowFolderPath");
        tmpXMLBasePath = prop.getProperty("tmpXMLBasePath");
    }    
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    public JSONArray getsubClasses(String property)
    throws MalformedURLException, IOException
    {
        JSONArray arraySubs = null;
        JSONObject subpropertyObject = null;
                
        String subpropertyQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                    "select ?subproperty\n" +
                                    "where\n" +
                                    "{\n" +
                                    " ?subproperty rdfs:subPropertyOf ?c.\n" +
                                    " BIND(STR(?c) AS ?str).\n" +
                                    " filter regex(?str,\"" + property + "\",\"i\").\n" +
                                    "}";
        
        JSONObject response = (JSONObject) new JSONTokener(IOUtils.toString(new URL("http://localhost:3030/model/query?query=" + URLEncoder.encode(subpropertyQuery).replaceAll("\\+","%20")))).nextValue();        
        
        return ((JSONObject)response.get("results")).getJSONArray("bindings");        
    }
    
   
}
