/*
    Lisis Connectivity for Galaxy

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

import filesystem.FileFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;

@Path("clusters")
public class ClusterResource
{
    @Context
    private UriInfo context;
    private String clusteringFolderPath;

    public ClusterResource()
    {
        try {
            getPropertyValues();
        } catch (IOException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        }          
    }
    
    @GET
    @Path("latest")
    @Produces(MediaType.APPLICATION_JSON)
    public String retrieveCluster()
    throws IOException
    {
        JSONObject jsonReply = new JSONObject();
        String filenameLatestFile = retrieveLatestFile("csv");
        JSONArray jsonArray = null;
        JSONArray mainArray = new JSONArray();        
        String[] lineElements = null;
        String line;      
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(clusteringFolderPath 
                                                    + "/" + filenameLatestFile));
            br.readLine();
            
            while((line = br.readLine()) != null)
            {
                lineElements = line.split(",");
                jsonArray = new JSONArray();
                
                JSONObject rowInfo = new JSONObject();
                rowInfo.put("CanonicalSMILE", lineElements[0]);
                rowInfo.put("SourceID", lineElements[1]);
                rowInfo.put("CommonName", lineElements[2]);
                rowInfo.put("Source", lineElements[3]);
                rowInfo.put("ClusterGroup", lineElements[4]);                
                
                jsonArray.put(rowInfo);
                mainArray.put(jsonArray);
            }
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        jsonReply.put("Clusters", mainArray);
        
        return jsonReply.toString();
    }

    @GET    
    @Path("/drug/{drugname}")
    @Produces(MediaType.APPLICATION_JSON)    
    public String retrieveClusterByDrug(@PathParam("drugname") String drugname)
    throws IOException
    {
        JSONArray jsonArray;
        String filenameLatestFile = retrieveLatestFile("csv");
        String[] lineElements = null;
        String clusterID = null;
        Hashtable<String, String> groupDrugs = new Hashtable<String, String>();
        Hashtable<String, JSONArray> cluster = new Hashtable<String, JSONArray>();
        String line;      
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(clusteringFolderPath 
                                                    + "/" + filenameLatestFile));
            br.readLine();
            drugname = drugname.toLowerCase();
            
            while((line = br.readLine()) != null)
            {
                lineElements = line.split(",");             

                groupDrugs.put(lineElements[2].trim().toLowerCase(), lineElements[4].trim());
                JSONObject rowInfo = new JSONObject();
                rowInfo.put("CanonicalSMILE", lineElements[0].trim());
                rowInfo.put("SourceID", lineElements[1].trim());
                rowInfo.put("CommonName", lineElements[2].trim().toLowerCase());
                rowInfo.put("Source", lineElements[3].trim());

                if((jsonArray=cluster.get(lineElements[4].trim()))==null)
                {
                    jsonArray = new JSONArray();
                }
                
                jsonArray.put(rowInfo);
                
                cluster.put(lineElements[4].trim(), jsonArray);
            }
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(groupDrugs.get(drugname));
        
        if((clusterID=groupDrugs.get(drugname))==null)
            return "[]";
        else
            return  "{" + "\"clusterID\":\"" + clusterID + "\",\"cluster\":" + cluster.get(groupDrugs.get(drugname)).toString() + "}";
    }    
    
    @GET
    @Path("latest/pdf")    
    @Produces("application/pdf")
    public Response retrievePDFCluster()
    {
        File file;
        ResponseBuilder response = null;
        
        try {
            file = new File(clusteringFolderPath + "/" + retrieveLatestFile("pdf"));
            response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename=clusterresults.pdf");            
        } catch (IOException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return response.build();        
    }
    
    protected String retrieveLatestFile(String fileExtension)
    throws IOException
    {
        String filenameLatestFile = null;
        FileTime latest = null;
        FileFilter fileFilter = new FileFilter(fileExtension);
        File currentFile = null;
        FileTime currentDate = null;
        File folder = new File(clusteringFolderPath);
        String[] listOfFiles = folder.list(fileFilter);
        BasicFileAttributes attr = null;
        java.nio.file.Path file = null;
        String files;
        
        file = FileSystems.getDefault().getPath(clusteringFolderPath
                                                + "/" + listOfFiles[0]);
        attr = Files.readAttributes(file, BasicFileAttributes.class);
        
        filenameLatestFile = listOfFiles[0];
        latest = attr.creationTime();

        for (int i = 1; i < listOfFiles.length; i++) 
        {
                files = listOfFiles[i];
                currentFile = new File(files);                
                file = FileSystems.getDefault().getPath(clusteringFolderPath 
                                                        + "/" + listOfFiles[i]);
                attr = Files.readAttributes(file, BasicFileAttributes.class);
                currentDate = attr.creationTime();
                
                if(currentDate.compareTo(latest)>0)
                {
                    filenameLatestFile = files;
                    latest = currentDate;
                }
        }
        
        return filenameLatestFile;
    }
    
    protected void getPropertyValues()
    throws IOException
    {
        Properties prop = new Properties();
        String propFileName = "/etc/config.properties";
 
        InputStream inputStream = new FileInputStream(propFileName);        
        prop.load(inputStream);
        
        clusteringFolderPath = prop.getProperty("clusterspath");
    }
    
    public static void main(String[] arg)
    {
        ClusterResource c = new ClusterResource();
        try {
            System.out.println(c.retrieveCluster());
            System.out.println(c.retrieveLatestFile("csv"));
        } catch (IOException ex) {
            Logger.getLogger(ClusterResource.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
