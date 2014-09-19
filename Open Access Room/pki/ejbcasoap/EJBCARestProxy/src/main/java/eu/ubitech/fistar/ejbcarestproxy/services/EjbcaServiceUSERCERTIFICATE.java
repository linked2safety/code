/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaUser;
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClient;
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.KeyStore;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/user_certificate")
public class EjbcaServiceUSERCERTIFICATE {

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
    public Response getUserCertificate(@QueryParam(value = "issuerDN") final String issuerDN, @QueryParam(value = "certificateSN") final String certSN) {

        if (issuerDN == null || issuerDN.isEmpty() || certSN == null || certSN.isEmpty()) {
            return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Parameter values are invalid...").build();
        }

        Certificate certificate = null;
        certificate = ejbcaWSclient.getCertificate(certSN, issuerDN);
        if (certificate == null) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Certificate not found...").build();
        }

        return Response.status(RESTfulServiceStatus.OK).entity(certificate.getRawCertificateData()).header("content-disposition", "attachment; filename=certificate.der").build();

    }

    @POST
    @Path("{username}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Creates an end-entity.
     *
     * @param name The name of the end-entity to create
     */
    public Response createCertificate(@PathParam("username") String username, @QueryParam(value = "password") final String password) {
        UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_USERNAME, UserMatch.MATCH_TYPE_EQUALS, username);
        List<UserDataVOWS> userList = ejbcaWSclient.findUser(usermatch);

        if (userList == null || userList.isEmpty()) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("End-Entity with name: " + username + " not found in EJBCA database...").build();
        }

        UserDataVOWS user = userList.get(0);
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, user.getUsername());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, password);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, user.getCaName());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, user.getSubjectDN());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, user.getEndEntityProfileName());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, user.getCertificateProfileName());

        if (!user.getEmail().isEmpty()) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, user.getEmail());
        }

        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, UserDataVOWS.TOKEN_TYPE_USERGENERATED);

        KeyStore keystore = ejbcaWSclient.createSoftTokenRequest(ejbcaUser);

        if (keystore != null) {
            return Response.status(RESTfulServiceStatus.OK).entity("Certificate is created!").build();
        }

        return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Could not create certificate ....").build();
    }

    @PUT
    @Path("{username}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Creates an end-entity.
     *
     * @param name The name of the end-entity to create
     */
    public Response editCertificate(@PathParam("username") String username, @QueryParam(value = "password") final String password) {
        UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_USERNAME, UserMatch.MATCH_TYPE_EQUALS, username);
        List<UserDataVOWS> userList = ejbcaWSclient.findUser(usermatch);

        if (userList == null || userList.isEmpty()) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("End-Entity with name: " + username + " not found in EJBCA database...").build();
        }

        UserDataVOWS user = userList.get(0);
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, user.getUsername());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, password);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, user.getCaName());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, user.getSubjectDN());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, user.getEndEntityProfileName());
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, user.getCertificateProfileName());

        if (!user.getEmail().isEmpty()) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, user.getEmail());
        }

        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, UserDataVOWS.TOKEN_TYPE_USERGENERATED);

        KeyStore keystore = ejbcaWSclient.createSoftTokenRequest(ejbcaUser);

        if (keystore != null) {
            return Response.status(RESTfulServiceStatus.OK).entity("Certificate is created!").build();
        }

        return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Could not create certificate ....").build();
    }

}
