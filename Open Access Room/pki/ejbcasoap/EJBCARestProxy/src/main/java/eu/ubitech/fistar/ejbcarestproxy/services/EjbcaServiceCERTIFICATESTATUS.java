/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClient;
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/certificate_status")
public class EjbcaServiceCERTIFICATESTATUS {

    private final EjbcaWSClientImpl ejbcaWSclient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Check the status of a certificate (returns a revoked reason of the
     * certificate).
     *
     * @param issuerDN The Distinguished Name of the issuer CA
     * @param certSN Certificate serial number (hex value)
     *
     * @return Revocation Status
     */
    public Response getCertificateStatus(@QueryParam(value = "issuerDN") final String issuerDN, @QueryParam(value = "certSN") final String certSN) {

        if (issuerDN == null || issuerDN.isEmpty() || certSN == null || certSN.isEmpty()) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Parameter values are invalid...").build();
        }

        RevokeStatus rs;
        rs = ejbcaWSclient.checkCertificateRevokeStatus(issuerDN, certSN);
        if (rs == null) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Certificate not found...").build();
        }

        return Response.status(RESTfulServiceStatus.OK).entity("Certificate Status: " + EjbcaWSClient.INSTANCE.getRevokeReason(rs.getReason())).build();

    }

    @PUT
    @Produces(MediaType.TEXT_HTML)
    /**
     * Revoke user's certificate.
     *
     * @param issuerDN The Distinguished Name of the issuer CA
     * @param certSN Certificate serial number (hex value)
     * @param revokeReason The status code which represents the revocation
     * reason
     */
    public Response revokeCertificate(@QueryParam(value = "issuerDN") final String issuerDN, @QueryParam(value = "certSN") final String certSN, @QueryParam(value = "revokeReason") final String revokeReason) {

        if (issuerDN == null || issuerDN.isEmpty() || certSN == null || certSN.isEmpty() || revokeReason == null || revokeReason.isEmpty()) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Parameter values are invalid...").build();
        }

        int revokeCode = EjbcaWSClient.INSTANCE.getRevokeReason(revokeReason);

        if (revokeCode == 0) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Revocation reason is not valid...").build();
        }

        boolean isRevokeSuccess = ejbcaWSclient.revokeUserCert(issuerDN, certSN, revokeCode);

        if (isRevokeSuccess) {
            return Response.status(RESTfulServiceStatus.OK).entity("Certificate with issuerDN: " + issuerDN + " and certSN: " + certSN + " was revoked!").build();
        }

        return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Could not revoke certificate or certificate already revoked...").build();

    }

}
