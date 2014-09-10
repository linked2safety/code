/*
 *   Copyright (C) 2014 Project dntalaperas@ubitech.eu

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


package rdfizers;

import beans.DatasetInformation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import mappingfacilities.MappingImportHandler;

public class CSVRDFizer
implements RDFizerInterface
{
    private String csvPath;
    private PrintWriter pw = null;
    private String outFilename;
    private DatasetInformation datasetInfo = null;
    private String mappingPath;
    private String dateOfCreation;
    HashMap<String,HashMap<String, String>> mappingsTable = null;
    HashMap<String,String> mappings = null;
    HashMap<String,String> typesTable = null;  

    public String getCsvPath() {
        return csvPath;
    }

    public void setCsvPath(String csvPath) {
        this.csvPath = csvPath;
    }

    public DatasetInformation getDatasetInfo() {
        return datasetInfo;
    }

    public void setDatasetInfo(DatasetInformation datasetInfo) {
        this.datasetInfo = datasetInfo;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public HashMap<String, String> getMappingsTable()
    {
        return mappingsTable.get("qb:Observation");
    }

    public void setMappingsTable(HashMap<String, HashMap<String, String>> mappingsTable) {
        this.mappingsTable = mappingsTable;
    }

    public HashMap<String, String> getTypesTable() {
        return typesTable;
    }

    public void setTypesTable(HashMap<String, String> typesTable) {
        this.typesTable = typesTable;
    }

    private static String md5convert(String initialStr)
    {
        try 
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(initialStr.getBytes());
          
            StringBuffer sb = new StringBuffer();
          
            for (int i = 0; i < array.length; ++i)
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));

            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {}
     
        return null;
    }    
     
    public void init()
    throws Throwable
    {   
        MappingImportHandler mappingImportHandler = new MappingImportHandler(this.mappingPath);
        mappingImportHandler.requestMapping();
        mappingsTable = mappingImportHandler.getEntitiesDescription();
        typesTable = mappingImportHandler.getTypes();
        String datacubeID = null; 

        //DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        dateOfCreation = dateFormat.format(date);        
        
        datacubeID = md5convert(dateOfCreation + csvPath + Math.random());
        
        datasetInfo = new DatasetInformation(dateOfCreation, 
                                            mappingImportHandler.getPublisher(), 
                                            mappingImportHandler.getLabel(), 
                                            csvPath, 
                                            new RDFNamespaces(mappingImportHandler.getVocabularies()), 
                                            mappingImportHandler.getURLBase() + datacubeID + "/",
                                            mappingImportHandler.getStructureBase() + datacubeID + "/",
                                            this.getMappingsTable(),
                                            mappingImportHandler.getComponentBaseURL() + datacubeID,
                                            mappingImportHandler.getFallbackNamespace());        
    }
    
    private HashMap<String,String> extractCSVheader(BufferedReader br)
    throws IOException
    {
        HashMap<String,String> csvHeader = new HashMap<String, String>();
        String[] headerElements = br.readLine().split(",");
        String[] headerName = null;
        
        for(int i=0;i<headerElements.length;i++)
        {
            System.out.println(headerElements[i]);
            
            headerName = headerElements[i].split(":");
            
            if(headerName.length==2)                
                csvHeader.put(""+i, headerName[1].toLowerCase());
            else
                csvHeader.put(""+i, headerElements[i].toLowerCase());
        }
        
        return csvHeader;
    }
    
    public void process()
    {
        System.out.println("dcube-rdfizer: Processing file:" + this.csvPath);
        processEachFile(this.csvPath);
    }

    public void processEachFile(String csv)
    {
        HashMap<String,String> mappings  = mappingsTable.get("qb:Observation");
        HashMap<String,String> csvHeader = new HashMap<String, String>();
        PrintWriter pw;
        BufferedReader br = null;
        String csvLine;
        StringBuffer triplesBundle = new StringBuffer();
        String entity;
        String[] csvLineList;
        String filename;
        int num = 0;
        String mapName = null;
        String type;
        
        //filename = outFilename + "cube_" + dateOfCreation + ".ttl";
        filename = outFilename;
        System.out.println("dcube-rdfizer: Creating file: " + filename);
        
        try
        {
            br = new BufferedReader(new FileReader(csv));
            csvHeader = extractCSVheader(br);
            pw = new PrintWriter(filename);
        
            pw.println(datasetInfo.getNamespacesAsTriples());
            pw.println(datasetInfo.getStructureDefinition(csvHeader));
            
            while ((csvLine = br.readLine()) != null)
            {
                String[] csvArray = csvLine.split(",");

                triplesBundle.append("<");
                triplesBundle.append(datasetInfo.getUrlBase());
                triplesBundle.append((num++) + "> qb:dataSet <");
                triplesBundle.append(datasetInfo.getUrlBase());
                triplesBundle.append(">;\n");

                for(int i=0;i<csvArray.length;i++)
                {
                    type = typesTable.get(csvHeader.get(""+i));
                    if(type!=null){
                        if(type.equals("data_property"))
                        {
                            triplesBundle.append("\tsdmx-measure:Cases \"");                        
                            triplesBundle.append(csvArray[i] + "\"^^" + mappings.get(csvHeader.get(""+i)) + ";\n");                        
                        }
                        else
                        {
                            triplesBundle.append("\t");
                            mapName = mappings.get(csvHeader.get(""+i));
                            triplesBundle.append(mapName);
    /*                        
                            if(mapName!=null)
                                triplesBundle.append(mapName);
                            else
                                triplesBundle.append("sdmx-dimension:" + mapName);
      */                      
    //                        triplesBundle.append(" <");
    //                        triplesBundle.append(datasetInfo.getUrlBase() + csvArray[i] + ">;\n");

                            triplesBundle.append(" \"");
                            triplesBundle.append(csvArray[i]+"\";\n");
                        }
                    }
                    else
                    {
                            triplesBundle.append("\t");

                            triplesBundle.append(datasetInfo.getFallbackNamespace() + ":" + csvHeader.get(""+i));
                            triplesBundle.append(" \"");
                            triplesBundle.append(csvArray[i]+"\";\n");
                            
                    }
                }
                
                triplesBundle.append("\t");
                triplesBundle.append("a qb:Observation.\n");
            }
            
            br.close();
            pw.println(triplesBundle.toString());
            pw.flush();
        }
         catch (Throwable t) {
            t.printStackTrace();
        }        
    }
    
    @Override
    public void finish()
    {
        pw.flush();
        pw.close();
    }

    @Override
    public String getOutputPath()
    {
        return outFilename;
    }

    @Override
    public void setOutputPath(String outputPath)
    {
        this.outFilename = new String(outputPath);
    }
    
}
