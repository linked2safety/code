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
@Path("/endentity_profile")
public class EjbcaServiceENDENTITYPROFILE {

    EjbcaWSClientImpl ejbcaWSClient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all authorized end-entities profiles for the current issuing CA.
     *
     *
     */
    public Response getAvailableCertificateProfiles() {

        String output = "";
        List<NameAndId> nidList = ejbcaWSClient.getAuthorizedEndEntityProfiles();
        for (NameAndId nid : nidList) {
            output += "End Entity Profile Name: " + nid.getName() + " Id: " + nid.getId() + "<br>";
        }
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();

    }

}
