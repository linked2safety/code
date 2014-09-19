/*
 *   Copyright (C) 2014 Panagiotis Hasapis (Panagiotis.hasapis@intrasoft-intl.com)

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

public class WorkflowInformation
{
    private String userEmail = null;
    private String toolInputFile = null;
    private String toolOutputFile = null;
    private String toolXMLDescription = null;
    private String storeWorkflow = null;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getToolInputFile() {
        return toolInputFile;
    }

    public void setToolInputFile(String toolInputFile) {
        this.toolInputFile = toolInputFile;
    }

    public String getToolOutputFile() {
        return toolOutputFile;
    }

    public void setToolOutputFile(String toolOutputFile) {
        this.toolOutputFile = toolOutputFile;
    }

    public String getToolXMLDescription() {
        return toolXMLDescription;
    }

    public void setToolXMLDescription(String toolXMLDescription) {
        this.toolXMLDescription = toolXMLDescription;
    }

    public String getStoreWorkflow() {
        return storeWorkflow;
    }

    public void setStoreWorkflow(String storeWorkflow) {
        this.storeWorkflow = storeWorkflow;
    }
    
}
