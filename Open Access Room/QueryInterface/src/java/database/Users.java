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

package database;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Users
{
    private dbConnector dbconnector = null;

    public Users()
    throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
    {
        dbconnector = new dbConnector();
        dbconnector.dbOpen();
    }
    
    public ResultSet retrieveValues(String email)
    throws SQLException
    {
        ResultSet rs = null;
        
        rs = dbconnector.dbQuery("select userid from users where email_address= '" + email + "';");
        
        return rs;
    }
        
    public void insertLogfileline(String email, String query)
    throws SQLException
    {
        ResultSet rs = retrieveValues(email);
        rs.next();
        query = URLEncoder.encode(query).replaceAll("\\+","%20");
        
        String resultTime;
        SimpleDateFormat formatter;

        formatter = new SimpleDateFormat("yyyy-MM-dd H:mm:ss.SSS");
        Date timeNow = new Date();
        resultTime = formatter.format(timeNow);
        int randomName = ((int)Math.floor(Math.random()*10000));


        PrintWriter pw;
        try {
            pw = new PrintWriter("/home/galaxy/galaxy-dist/tools/data_source/fakeXML.xml");
            pw.print("<Workflow description_of_workflow=\"input_data\" date_of_workflow=\"" + resultTime + "\"><Dataset SPARQL_query=\"" + email + "!"  + query + "\" time=\"" + resultTime + "\"></Dataset></Workflow>");
            pw.flush();
            pw.close();            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Users.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void close()
    throws SQLException
    {
        dbconnector.dbClose();
    }
    
   }
}
