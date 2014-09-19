/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/crl")
public class EjbcaServiceCRL {

    private final EjbcaWSClientImpl ejbcaWSclient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all the available certificate revocation lists names.
     *
     */
    public Response getAvailableCAs() {
        String output = "";
        List<NameAndId> nidList = ejbcaWSclient.getAvailableCAs();
        for (NameAndId nid : nidList) {
            output += "CA Name: " + nid.getName() + "\n";
        }
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch the latest Certificate Revocation List of the given Certificate
     * Authority.
     *
     * @param name The name of the CA to fetch the CRL.
     *
     * @return CRL The CRL of the specific CA.(in DER format)
     */
    public Response getLatestCRL(@PathParam("name") String name) {
        byte[] crlraw = null;

        try {
            crlraw = ejbcaWSclient.getEjbcaRAWS().getLatestCRL(name, false);
        } catch (CADoesntExistsException_Exception ex) {
            Logger.getLogger(EjbcaServiceCRL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaServiceCRL.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (crlraw == null) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("No such CRL List as : " + name).build();
        }
        return Response.status(RESTfulServiceStatus.OK).entity(crlraw).header("content-disposition", "attachment; filename = " + name + ".crl").build();

    }

    @POST
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Creates a CRL for the given Certificate Authority.
     *
     * @param name The name of the CA to create the CRL.
     */
    public Response createCRL(@PathParam("name") String name) {

        boolean isCreateSuccess = ejbcaWSclient.createCRL(name);
        if (!isCreateSuccess) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Could not create CRL with name : " + name).build();
        }
        return Response.status(RESTfulServiceStatus.OK).entity("CRL of CA: " + name + " was created!").build();

    }

    @PUT
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Updates a CRL for the given Certificate Authority.
     *
     * @param name The name of the CA to update the CRL.
     */
    public Response updateCRL(@PathParam("name") String name) {

        boolean isCreateSuccess = ejbcaWSclient.createCRL(name);
        if (!isCreateSuccess) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Could not update CRL with name : " + name).build();
        }
        return Response.status(RESTfulServiceStatus.OK).entity("CRL of CA: " + name + " was updated!").build();

    }

}