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
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.apache.commons.io.IOUtils;

@WebServlet(name = "drugcluster", urlPatterns = {"/drugcluster"})
public class drugcluster extends HttpServlet {

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
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        LinkedList<String> result = null;
        Iterator iter = null;
        String str;
        
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Drug Cluster Information</title>");            
            out.println("</head>");
            out.println("<body>");
            result = this.retrieveDrugCluster(request.getParameter("clusterid"));
            
            iter = result.iterator();
            
            out.println("<p>Drugs maintained in this drug cluster:");
            
            while(iter.hasNext())
            {
                str = (String)iter.next();
                out.println(str + ", ");
            }
            
            out.println("</body>");
            out.println("</html>");
        } finally {            
            out.close();
        }
    }

    private LinkedList<String> retrieveDrugCluster(String id) throws IOException
    {
        LinkedList<String> response = new LinkedList<String>();
        JSONObject jo = (JSONObject) new JSONTokener(IOUtils.toString(new URL("http://lisis.cs.ucy.ac.cy:9090/LisisSafetyExternalInterface/rest/clusters/latest"))).nextValue();
        JSONArray arrayInternal = null;
        int index = 0;
        
        JSONArray jsonArray = jo.getJSONArray("Clusters");
        Iterator iter = jsonArray.iterator();
        
        while(iter.hasNext())
        {
            arrayInternal = (JSONArray)iter.next();
            Iterator iterInternal = arrayInternal.iterator();
            
            while(iterInternal.hasNext())
            {
                JSONObject o = (JSONObject)iterInternal.next();
                
                if(o.getString("ClusterGroup").equals(id))
                {
                    response.add(o.getString("CommonName"));
                    System.out.println(o.toString());
                }
            }
        }
        
        return response;
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
