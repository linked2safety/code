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


import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class dbConnector
{
	private Statement stmt;
	private String stmtStr;
	private Connection dbConnection;

	public void dbOpen()
	throws ClassNotFoundException, SQLException,
        InstantiationException, IllegalAccessException
	{       
                String userName = "post";
                String password = "post;
                String url = "jdbc:postgresql://1.1.1.1:5432/postgres";
                           
                Class.forName ("org.postgresql.Driver").newInstance ();
                dbConnection = DriverManager.getConnection (url, userName, password);
		stmt = dbConnection.createStatement();
	}

	public ResultSet dbQuery(String sqlQuery)
	throws SQLException
	{
		ResultSet rs;
		clearRequestFields();
		this.stmtStr = new String(sqlQuery);
		rs = stmt.executeQuery(stmtStr);
		return(rs);
	}

	public int dbUpdate(String updateString)
	throws SQLException
	{
		int updatesMade;
		clearRequestFields();
		this.stmtStr = new String(updateString);
		updatesMade = stmt.executeUpdate(stmtStr);
		return(updatesMade);
	}

	public void dbClose()
	throws SQLException
	{
		clearRequestFields();
		dbConnection.close();
		return;
	}

	private void clearRequestFields()
	{
		stmtStr = null;
		return;
	}

        public static void main(String[] args)
        {
            dbConnector db = new dbConnector();
            try {
                db.dbOpen();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(dbConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(dbConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(dbConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(dbConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            ResultSet rs;
            
            try {
                rs = db.dbQuery("select * from users;");
                
                while(rs.next())
                {
                    System.out.println(rs.getString("name"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(dbConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
}
