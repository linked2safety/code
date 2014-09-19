/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.controller;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import eu.ubitech.fistar.ejbcarestproxy.conf.Configuration;
import eu.ubitech.fistar.ejbcarestproxy.server.ServerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
public class EJBCAProxyController {

    private final static Logger LOGGER = Logger.getLogger(EJBCAProxyController.class.getName());
    private final Properties prop = new Properties();
    private InputStream input = null;
    //Jetty Server
    private Server server;

    public static void main(String[] args) {
        EJBCAProxyController controller = new EJBCAProxyController();
        LOGGER.info("EJBCAProxyController initiated");
        controller.readProperties();
        controller.startJettyServer();

    }//main

    private void readProperties() {
        try {
            String path = new java.io.File(".").getCanonicalPath();
            LOGGER.log(Level.INFO, "PATH:{0}", path);
           // input = new FileInputStream("jetty.properties");
           input = Thread.currentThread().getContextClassLoader().getResourceAsStream("jetty.properties");
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            Configuration.port = Integer.parseInt(prop.getProperty("port").trim());
            Configuration.token = prop.getProperty("token").trim();
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }//EoM readproperties

    private void startJettyServer() {
        try {
            ServletHolder sh = new ServletHolder(ServletContainer.class);
            sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            sh.setInitParameter("com.sun.jersey.config.property.packages", "eu.ubitech.fistar.ejbcarestproxy.services");//Set the package where the services reside
            sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

            //get Singleton
            if (Configuration.port > 0 && Configuration.port < 65535) {
                server = ServerFactory.INSTANCE.getJettyServer(Configuration.port);
            } else {
                server = ServerFactory.INSTANCE.getJettyServer();
            }
            /* ServletHandler handler = new ServletHandler();
             handler.addServletWithMapping(HelloServlet.class, "/hello");//Set the servlet to run.*/
            ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            context.addServlet(sh, "/rest/ejbca/*");
            //server.setHandler(handler);
            //manage thread
            server.start();
            server.join();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//EoM

    private void stopJettyServer() {
        try {
            server.stop();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//EoM    

    @SuppressWarnings("serial")
    public static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello SimpleServlet</h1>");
        }
    }

}
