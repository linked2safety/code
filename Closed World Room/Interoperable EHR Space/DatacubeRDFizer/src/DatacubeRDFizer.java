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

import rdfizers.CSVRDFizer;
import rdfizers.RDFizerFactory;
import rdfizers.RDFizerInterface;


public class DatacubeRDFizer
{
    public static void main(String[] args)
    {
        try
        {
            RDFizerInterface rsd = RDFizerFactory.getRDFizer("csv", "/home/panos/Desktop/fakeData_rs16.cub/bmi-patientdiabetes-smoking.csv","/home/panos/Desktop/test", "/home/panos/Desktop/t.ttl");
            //RDFizerInterface rsd = RDFizerFactory.getRDFizer("csv", args[0], args[1], args[2]);
            rsd.init();
            rsd.process();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        
    }
}
