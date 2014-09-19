/*
    Knowleddge Base for Galaxy

    Copyright (C) 2014  Panagiotis Hasapis (Panagiotis.hasapis@intrasoft-intl.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

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

                String userName = "postgres";
                String password = "postgres";

                String url = "jdbc:postgresql://1.1.1.1:5432/galaxy_prod";            

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
