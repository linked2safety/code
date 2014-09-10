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

package beans;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import rdfizers.RDFNamespaces;

public class DatasetInformation 
{
    private String date;
    private String publisher;
    private String urlBase = null;
    private String label;
    private String composer;
    private HashMap<String,String> mappingNames;
    private RDFNamespaces rdfNamespaces;
    private String structureBase;
    private String componentBaseURL;
    private String fallbackNamespace;
    private long datacubeNum = 0;

    public String getFallbackNamespace() {
        return fallbackNamespace;
    }

    public void setFallbackNamespace(String fallbackNamespace) {
        this.fallbackNamespace = fallbackNamespace;
    }

    public long getDatacubeNum() {
        return datacubeNum;
    }

    public void setDatacubeNum(long datacubeNum) {
        this.datacubeNum = datacubeNum;
    }

    public DatasetInformation(String date, String publisher, String label, String composer, RDFNamespaces rdfNamespaces, String urlBase, String structureBase, HashMap<String,String> mappingNames, String componentBaseURL, String fallbackNamespace)
    {
        this.date = date;
        this.publisher = publisher;
        this.label = label;
        this.composer = composer;
        this.rdfNamespaces = rdfNamespaces;
        this.urlBase = urlBase;
        this.structureBase = structureBase;
        this.mappingNames = mappingNames;
        this.componentBaseURL = componentBaseURL;
        this.fallbackNamespace = fallbackNamespace;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getStructureBase() {
        return structureBase;
    }

    public void setStructureBase(String structureBase) {
        this.structureBase = structureBase;
    }  
    
    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public RDFNamespaces getRdfNamespaces() {
        return rdfNamespaces;
    }

    public void setRdfNamespaces(RDFNamespaces rdfNamespaces) {
        this.rdfNamespaces = rdfNamespaces;
    }
    
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getStructureDefinition(HashMap<String,String> csvHeader)
    {
        LinkedList<String> bnodeNames = new LinkedList<String>();
        Iterator<String> bnodeNamesIterator;
        int orderNum = 0;
        int i=0;
        StringBuffer strBuffer = new StringBuffer();
        StringBuffer secBuffer = new StringBuffer();
        HashMap<String,String> bnodesMap = new HashMap<String,String>();
        String mname = null;
        String bnode = null;
        
        strBuffer.append("<");
        //strBuffer.append(this.urlBase + (++datacubeNum));
        strBuffer.append(this.urlBase);
        strBuffer.append("> dcterms:date \"");
        strBuffer.append(this.date);
        strBuffer.append("\";\n");
        strBuffer.append("\tdcterms:publisher \"");
        strBuffer.append(this.publisher);
        strBuffer.append("\";\n\trdfs:label \"");
        strBuffer.append(this.label);
        strBuffer.append("\";\n\tqb:structure <");
        strBuffer.append(this.structureBase);                
        strBuffer.append(">;\n\ta qb:DataSet.\n\n");
        
        strBuffer.append("<" + structureBase + "> qb:component ");
        
        for(i=0;i<csvHeader.size();i++)
        {   
            //bnode = "_:bnode"+((int)(Math.random()*100000));
            bnode = new String(this.componentBaseURL + "/" +  i);
            System.out.println("-----" + bnode);
            mname = mappingNames.get(csvHeader.get(""+i));

           if(mname!=null)
           {
                if(!mname.equals("qb:Observation") && !(mname.startsWith("xsd")))
                {
                    bnodesMap.put(bnode, mname);
                }
           }
           else
           {
                bnodesMap.put(bnode, "l2s-dim:" + csvHeader.get(""+i).toLowerCase());                
           }
        }
        
        bnodeNamesIterator = bnodesMap.keySet().iterator();
        
        //do both in one loop
        while(bnodeNamesIterator.hasNext())
        {
            bnode = (String)bnodeNamesIterator.next();
            
            secBuffer.append("<" + bnode + ">");
            secBuffer.append(" qb:dimension ");
            secBuffer.append(bnodesMap.get(bnode));
            secBuffer.append(".\n");            
//            secBuffer.append("; qb:order " + (++orderNum) + ".\n");
            System.out.println(bnode);
            strBuffer.append("<" + bnode + ">" + ",");
        }
        
        secBuffer.append("<" + (this.componentBaseURL + "/" + i) + "> qb:measure sdmx-measure:Cases.\n\n");
        strBuffer.trimToSize();
        strBuffer.setCharAt(strBuffer.length()-1, ' ');
        strBuffer.append(",<" + this.componentBaseURL + "/" + i + ">; a qb:DataStructureDefinition.\n\n");
        strBuffer.append(secBuffer);
        
        return strBuffer.toString();
    }
    
    public String getNamespacesAsTriples()
    {
        HashMap<String,String> prefixes = rdfNamespaces.getRdfDescriptor();
        System.out.println("prefixes: " + prefixes);
        Set<String> prefixesKeysSet = prefixes.keySet();
        Iterator prefixesKeys = prefixesKeysSet.iterator();
        StringBuffer strBuffer = new StringBuffer();

        while(prefixesKeys.hasNext())
        {
            String prefix = (String)prefixesKeys.next();
            strBuffer.append("@prefix ");
            strBuffer.append(prefix);
            strBuffer.append(": <");
            strBuffer.append(prefixes.get(prefix));
            strBuffer.append("> .\n");
        }
        
        return strBuffer.toString();
    }
}
