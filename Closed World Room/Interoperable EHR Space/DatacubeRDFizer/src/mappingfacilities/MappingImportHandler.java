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


package mappingfacilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import xmlhandlers.MappingFileReader;

public class MappingImportHandler
{
    private SAXParserFactory factory;
    private SAXParser saxParser;
    private MappingFileReader handler;
    private String path;
    private String publisher;
    private String label;
    private String componentBaseURL;
    
    public MappingImportHandler(String path)
    {
        this.path = new String(path);
    }

    public String getURLBase()
    {
        return handler.getUrlBase();
    }

    public String getComponentBaseURL() {
        return handler.getComponentBaseURL();
    }

        
    public String getStructureBase()
    {
        return handler.getStructureBase();
    }
    
    public void requestMapping()
    {
        try
        {
            factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
            handler = new MappingFileReader();
            saxParser.parse(path, handler);  
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
       
    public HashMap<String,String> getTypes()
    {
        return handler.getTypes();
    }
    
    public String getPublisher()
    {
        return handler.getPublisher();
    }
    
    public String getLabel()
    {
        return handler.getLabel();
    }

    public String getFallbackNamespace()
    {
        return handler.getFallbackNamespace();
    }    
    
    public HashMap<String,String> getVocabularies()
    {
        return handler.getVocabularies();
    }
    
    public HashMap<String, HashMap<String,String>> getEntitiesDescription()
    {
        return handler.getEntitiesDescription();
    }
    
    public static void main(String[] args)
    {
        MappingImportHandler mih = new MappingImportHandler("C:\\Users\\user\\Desktop\\MappingFileForCSV.xml");
        mih.requestMapping();
        System.out.println(mih.getVocabularies());
    }
}
