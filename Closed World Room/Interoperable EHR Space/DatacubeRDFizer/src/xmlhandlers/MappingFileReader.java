package xmlhandlers;

import java.net.URLEncoder;
import java.util.HashMap;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MappingFileReader
extends DefaultHandler
{
            HashMap<String, HashMap<String,String>> entitiesDescription = new HashMap();
            HashMap<String,String> mappings;
            HashMap<String,String> types;            
            boolean namefl = false;
            boolean rdffl = false;
            boolean sourceitemfl = false;
            String name, rdf, sourceitem, ns, url, urlBase;
            private boolean vocabulariesfl;
            private HashMap<String,String> vocabularies;
            private boolean typefl, nsfl, urlfl, urlbasefl, structurebasefl;
            private String type, label, publisher, structureBase;
            private boolean nodefl, labelfl, publisherfl;
            private String nodeName;
            private boolean componentbaseurl;
            private String componentbaseurlString;
            private boolean fallbackfl;
            private String fallbackString;

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getPublisher() {
                return publisher;
            }

            public void setPublisher(String publisher) {
                this.publisher = publisher;
            }

            public boolean isLabelfl() {
                return labelfl;
            }

            public void setLabelfl(boolean labelfl) {
                this.labelfl = labelfl;
            }

            public boolean isPublisherfl() {
                return publisherfl;
            }

            public void setPublisherfl(boolean publisherfl) {
                this.publisherfl = publisherfl;
            }

            public String getUrlBase() {
                return urlBase;
            }

            public String getFallbackNamespace()
            {
                return fallbackString;
            }
            
            public String getComponentBaseURL()
            {
                return this.componentbaseurlString;
            }
            
            public String getStructureBase()
            {
                return structureBase;
            }
            
            public void setMappings(HashMap<String, String> mappings) {
                this.mappings = mappings;
            }
            
            public HashMap<String, HashMap<String,String>> getEntitiesDescription()
            {
                return this.entitiesDescription;
            }
            
            public HashMap<String, String> getTypes()
            {
                return this.types;
            }

            public HashMap<String, String> getVocabularies()
            {
                return this.vocabularies;
            }            
            
            public void startElement(String uri, String localName,String qName, Attributes attributes)
            throws SAXException
            {

                    if(qName.equalsIgnoreCase("MappingFile"))
                    {
                            vocabularies = new HashMap<String, String>(30);
                    }
                
                    if (qName.equalsIgnoreCase("resourcename")) {
                            namefl = true;
                            mappings = new HashMap<String, String>();
                            types = new HashMap<String, String>();
                    }

                    if (qName.equalsIgnoreCase("vocabulary")) {
                            vocabulariesfl = true;
                    }                    

                    if (qName.equalsIgnoreCase("ns")) {
                            nsfl = true;
                    }                               

                    if (qName.equalsIgnoreCase("url")) {
                            urlfl = true;
                    }                    
                    
                    if (qName.equalsIgnoreCase("resourcemapping")) {
                            nodefl = true;
                    }                                   
                    
                    if (qName.equalsIgnoreCase("targetconcept")) {
                            rdffl = true;
                    }

                    if (qName.equalsIgnoreCase("sourceitem")) {
                            sourceitemfl = true;
                    }
                    
                    if (qName.equalsIgnoreCase("type")) {
                            typefl = true;
                    }

                    if (qName.equalsIgnoreCase("urlbase")) {
                            urlbasefl = true;
                    }    

                    if (qName.equalsIgnoreCase("fallbacknamespace")) {
                            fallbackfl = true;
                    }                     
                    
                    if (qName.equalsIgnoreCase("structurebase")) {
                            structurebasefl = true;
                    }                        

                    if (qName.equalsIgnoreCase("componentbaseurl")) {
                            componentbaseurl = true;
                    }                        
                                        
                    if (qName.equalsIgnoreCase("publisher")) {
                            publisherfl = true;
                    }
                    
                    if (qName.equalsIgnoreCase("label")) {
                            labelfl = true;
                    }                       
            }

            
            // we assume that the XML is fairly simple and canonical,
            // always in the form: mappings/mapping/sourceitem first
            // followed by mappings/mapping/targetconcept
            
            public void endElement(String uri, String localName, String qName)
            throws SAXException
            {

                    if(qName.equals("description"))
                    {
                        //System.out.println("KOITA:" + name);
                        entitiesDescription.put(name, mappings);
                        mappings = null;
                    }

                    if(qName.equals("resourcename"))
                    {
                        mappings.put("resourcename", name);
                    }
                                        
                    if(qName.equals("vocabulary"))
                    {
                        vocabularies.put(ns, url);
                    }                    

                    if(qName.equals("resourcemapping"))
                    {
                        mappings.put("resourcemapping", nodeName);
                    }                               
                    
                    if(qName.equals("targetconcept"))
                    {
                        mappings.put(sourceitem, rdf);
                    }

                    if(qName.equals("type"))
                    {
                        types.put(sourceitem, type);
                    }
                    
                    if(qName.equals("urlbase"))
                    {
                        urlbasefl = false;
                    }

                    if(qName.equals("fallbacknamespace"))
                    {
                        fallbackfl = false;
                    }                    
                    
                    if(qName.equals("structurebase"))
                    {
                        structurebasefl = false;
                    }                    

                    if(qName.equals("componentbaseurl"))
                    {
                        componentbaseurl = false;
                    }                    

                    if(qName.equals("label"))
                    {
                        labelfl = false;
                    }
                    
                    if(qName.equals("publisher"))
                    {
                        publisherfl = false;
                    }                     
            }
                   
            public void characters(char ch[], int start, int length) throws SAXException {

                    if (typefl) {
                            type = new String(ch, start, length);
                            typefl = false;
                    }
                
                    if (namefl) {
                            name = new String(ch, start, length);
                            namefl = false;
                    }

                    if (rdffl) {
                            rdf = new String(ch, start, length);
                            rdffl = false;
                    }

                    if (nsfl) {                            
                            ns = new String(ch, start, length);
                            System.out.println("NS: " + ns);
                            nsfl = false;
                    }                    

                    if (urlfl) {                            
                            url = new String(ch, start, length);
                            String url2 = new String(ch);
                            //System.out.println("URL: " + url + " " + " " + start + " " + length);
                            //System.out.println("URL2: " + url2);
                            urlfl = false;
                    }                             
                    
                    if (sourceitemfl) {
                            sourceitem = new String(ch, start, length);
                            //System.out.println("---------------sourceitem: " + sourceitem);
                            String url2 = new String(ch);
                            //System.out.println("URL2: " + url2);                            
                            sourceitemfl = false;
                    }

                    if (nodefl) {
                            nodeName = new String(ch, start, length);
                            nodefl = false;
                    }
                    
                    if (urlbasefl) {
                            urlBase = new String(ch, start, length);
                            urlbasefl = false;
                    }

                    if (structurebasefl) {
                            structureBase = new String(ch, start, length);
                            structurebasefl = false;
                    }                    

                    if (fallbackfl) {
                            fallbackString = new String(ch, start, length);
                            fallbackfl = false;
                    }                    
                                        
                    if (componentbaseurl) {
                            componentbaseurlString = new String(ch, start, length);
                            componentbaseurl = false;
                    }                    
                                        
                    if (labelfl) {
                            label = new String(ch, start, length);
                            labelfl = false;
                    }                     
                    
                    if (publisherfl) {
                            publisher = new String(ch, start, length);
                            publisherfl = false;
                    }                     
            }
}
