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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KnowledgeBaseConnector
{
    private dbConnector dbconnector = null;

    public KnowledgeBaseConnector()
    throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
    {
        dbconnector = new dbConnector();
        dbconnector.dbOpen();
    }
    
    public ResultSet searchAssociations(String predicates)
    throws SQLException
    {
        ResultSet rs = null;
        
        rs = dbconnector.dbQuery("select association,odds_ratio,p_value from association where visible=true and association like '%=>" + predicates + "%';");
        
        return rs;
    }

    public ResultSet searchSelfAssociations(String predicates)
    throws SQLException
    {
        ResultSet rs = null;
        
        rs = dbconnector.dbQuery("select association,odds_ratio,p_value from selfreportedassociation where visible=true and association like '%=>" + predicates + "%';");
        
        return rs;
    }

    public ResultSet searchSelfAssociationsRule(String predicates)
    throws SQLException
    {
        ResultSet rs = null;
        
        rs = dbconnector.dbQuery("select lefthandside, righthandside,support,confidence,lift from selfreportedassociationrule where righthandside like '%" + predicates + "%';");
        
        return rs;
    }
    
    
    public ResultSet searchAssociationsRule(String predicates)
    throws SQLException
    {
        ResultSet rs = null;
        
        rs = dbconnector.dbQuery(" select lefthandside,righthandside,support,confidence from associationrule where visible=true and righthandside like '%" + predicates + "%';");
        
        return rs;
    }    
    
    public void close()
    throws SQLException
    {
        dbconnector.dbClose();
    }
    
    public static void main(String[] args)
    {
        KnowledgeBaseConnector k;

        try {
            ResultSet rs;
            
            k = new KnowledgeBaseConnector();
            rs = k.searchAssociations("Diabetes");
            
            while(rs.next())
            {
                System.out.println(rs.getString("association"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(KnowledgeBaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}
