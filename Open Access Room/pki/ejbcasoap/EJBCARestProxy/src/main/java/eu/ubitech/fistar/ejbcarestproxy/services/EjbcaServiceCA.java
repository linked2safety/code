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
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/ca")
public class EjbcaServiceCA {

    private final EjbcaWSClientImpl ejbcaWSclient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all the available Certificate Authorities.
     */
    public Response getAvailableCAs() {
        String output = "";
        List<NameAndId> nidList = ejbcaWSclient.getAvailableCAs();
        for (NameAndId nid : nidList) {
            output += "Name: " + nid.getName() + " Id: " + nid.getId() + "\n";
        }
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch the Certificate of a Certificate Authority.
     *
     * @param name The name of the CA to fetch the certificate representation
     *
     * @return Certificate The certificate of the CA.(in DER format)
     */
    public Response getCACertificate(@PathParam("name") String name) {
        String output = name;
        List<Certificate> certsList = ejbcaWSclient.getCACert(name);
        if (certsList == null) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("No such CA as : " + name).build();
        }
        return Response.status(RESTfulServiceStatus.OK).entity(certsList.get(0).getRawCertificateData()).header("content-disposition", "attachment; filename = " + name + ".der").build();
    }

}
//@QueryParam(value = "certSN") final String certSN,
