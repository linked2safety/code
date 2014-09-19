/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/info")
public class EjbcaServiceINFO {

    EjbcaWSClientImpl ejbcaWSClient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Retrieves the current version of the running EJBCA.
     *
     */
    public Response getEJBCAVersion() {
        String output = ejbcaWSClient.getEjbcaRAWS().getEjbcaVersion();
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();

    }

}
