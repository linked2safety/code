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
package services;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import database.KnowledgeBaseConnector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author panos
 */
@WebServlet(name = "associations", urlPatterns = {"/associations"})
public class associations extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Associations Results</title>");            
            out.println("</head>");
            try {
                KnowledgeBaseConnector kbc = new KnowledgeBaseConnector();
                ResultSet rs = kbc.searchAssociations(request.getParameter("righthand"));

                out.println("<p>Association Rules:");
                out.println("<table>");

                out.println("<tr>");
                out.println("<td><b>Association</b></td>");
                out.println("<td><b>p_value</b></td>");
                out.println("<td><b>odds_ratio</b></td>");                    
                out.println("</tr>");                
                
                while(rs.next())
                {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("association") + "</td>");
                    out.println("<td>" + rs.getString("p_value") + "</td>");
                    out.println("<td>" + rs.getString("odds_ratio") + "</td>");                    
                    out.println("</tr>");
                }                
                
                out.println("</table>");
                
                rs.close();
                
                rs = kbc.searchSelfAssociations(request.getParameter("righthand"));

                out.println("<p>Association Self Rules:");
                out.println("<table>");
                out.println("<tr>");
                out.println("<td><b>Association</b></td>");
                out.println("<td><b>p_value</b></td>");
                out.println("<td><b>odds_ratio</b></td>");                    
                out.println("</tr>");                
                
                while(rs.next())
                {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("association") + "</td>");
                    out.println("<td>" + rs.getString("p_value") + "</td>");
                    out.println("<td>" + rs.getString("odds_ratio") + "</td>");                    
                    out.println("</tr>");
                }                
                
                out.println("</table>");
                
                rs.close();
                
                rs = kbc.searchAssociationsRule(request.getParameter("righthand"));

                out.println("<p>Association Rules:");
                out.println("<table>");
                out.println("<tr>");
                out.println("<td><b>Left hand</b></td>");
                out.println("<td><b>Right hand</b></td>");
                out.println("<td><b>Support</b></td>");
                out.println("<td><b>Confidence</b></td>");  
                out.println("</tr>");                
                
                while(rs.next())
                {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("lefthandside") + "</td>");
                    out.println("<td>" + rs.getString("righthandside") + "</td>");
                    out.println("<td>" + rs.getString("support") + "</td>"); 
                    out.println("<td>" + rs.getString("confidence") + "</td>");                    
                    out.println("</tr>");
                }                
                
                rs = kbc.searchAssociationsRule(request.getParameter("righthand"));

                out.println("<p>Association Rules:");
                out.println("<table>");
                out.println("<tr>");
                out.println("<td><b>Left hand</b></td>");
                out.println("<td><b>Right hand</b></td>");
                out.println("<td><b>Support</b></td>");
                out.println("<td><b>Confidence</b></td>");
                out.println("</tr>");                
                
                while(rs.next())
                {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("lefthandside") + "</td>");
                    out.println("<td>" + rs.getString("righthandside") + "</td>");
                    out.println("<td>" + rs.getString("support") + "</td>"); 
                    out.println("<td>" + rs.getString("confidence") + "</td>");                    
                    out.println("</tr>");
                }   
                
                
                out.println("</table>");
                                                            
                rs.close();                
                
                rs = kbc.searchSelfAssociationsRule(request.getParameter("righthand"));

                out.println("<p>Association Rules:");
                out.println("<table>");
                out.println("<tr>");
                out.println("<td><b>Left hand</b></td>");
                out.println("<td><b>Right hand</b></td>");
                out.println("<td><b>Support</b></td>");
                out.println("<td><b>Confidence</b></td>");                                    
                out.println("<td><b>Lift</b></td>");                
                out.println("</tr>");                
                
                while(rs.next())
                {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("lefthandside") + "</td>");
                    out.println("<td>" + rs.getString("righthandside") + "</td>");
                    out.println("<td>" + rs.getString("support") + "</td>"); 
                    out.println("<td>" + rs.getString("confidence") + "</td>");                    
                    out.println("<td>" + rs.getString("lift") + "</td>");                     
                    out.println("</tr>");
                }   
                
                
                out.println("</table>");
                                                            
                rs.close();                  
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(associations.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(associations.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(associations.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(associations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            out.println("<body>");
            out.println("</body>");
            out.println("</html>");
        } finally {            
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
