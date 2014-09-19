/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/certificate_profile")
public class EjbcaServiceCERTIFICATEPROFILE {

    EjbcaWSClientImpl ejbcaWSClient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRoot(){
        return Response.status(RESTfulServiceStatus.METHOD_NOT_ALLOWED).entity("Operation is not supported...").build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all available certificate profiles for a specific end-entity
     * profile.
     *
     */
    public Response getAvailableCertificateProfiles(@PathParam("name") String name) {

        int entityProfileId = 0;
        try {
            entityProfileId = Integer.parseInt(name);

        } catch (NumberFormatException nex) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity(String.valueOf(RESTfulServiceStatus.BAD_REQUEST)).build();
        }

        String output = "";
        List<NameAndId> nidList = ejbcaWSClient.getAvailableCertificateProfiles(entityProfileId);
        if (nidList == null || nidList.isEmpty()) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity(String.valueOf(RESTfulServiceStatus.INTERNAL_SERVER_ERROR)).build();
        }
        for (NameAndId nid : nidList) {
            output += "Certificate Profile Name: " + nid.getName() + " Id: " + nid.getId() + "<br>";
        }
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();

    }

}
