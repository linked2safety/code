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

import java.util.HashMap;

public class RDFNamespaces
{
    private HashMap<String,String> rdfDescriptor = null;
    
    public RDFNamespaces()
    {
        rdfDescriptor = new HashMap();
    }

    public RDFNamespaces(HashMap<String,String> rdfDescriptor)
    {
        this.rdfDescriptor = rdfDescriptor;
    }
        
    public HashMap<String, String> getRdfDescriptor() {
        return rdfDescriptor;
    }

    public void setRdfDescriptor(HashMap<String, String> rdfDescriptor) {
        this.rdfDescriptor = rdfDescriptor;
    }
        
    public String getURI(String name)
    {
        return rdfDescriptor.get(name);
    }
    
}
