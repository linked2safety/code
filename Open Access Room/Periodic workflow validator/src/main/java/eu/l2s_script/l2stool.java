/*
 * Copyright (C) 2014 CERTH - zeginis@iti.gr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This component bundles PostgreSQL JDBC driver, which is available under 
 * BSD license. You may download the source code and associated license at 
 * http://jdbc.postgresql.org/
 * 
 * This component bundles Apache Jena library, which is available under 
 * Apache v2 licence . You may download the source code and associated license at 
 * https://jena.apache.org/
 *
 * This component bundles Java SE, which is available under BCL license. 
 * You may download the source code and associated license at  http://www.oracle.com/
 * The java BCL license clarifies that:
 *   "Use of the Commercial Features for any commercial or production purpose 
 *    requires a separate license from Oracle. “Commercial Features” means those 
 *    features identified Table 1-1 (Commercial Features In Java SE Product Editions)
 *    of the Java SE documentation accessible at
 *    http://www.oracle.com/technetwork/java/javase/documentation/index.html"  
 *
 *
 *
 *  The source files are available at:  https://github.com/linked2safety/code
 */


package eu.l2s_script;

/**
 *
 * @author Dimitris Zeginis
 */
public class l2stool {
    
    private String SQLtable;
    private String executable;

    public l2stool(String SQLtable, String executable) {
        this.SQLtable = SQLtable;
        this.executable = executable;
    }

    public void setSQLtable(String SQLtable) {
        this.SQLtable = SQLtable;
    }

    public void setExecytable(String executable) {
        this.executable = executable;
    }

    public String getSQLtable() {
        return SQLtable;
    }

    public String getExecutable() {
        return executable;
    }
    
    
}
