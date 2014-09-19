/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.services;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaUser;
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
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
@Path("/endentity")
public class EjbcaServiceENDENTITY {

    private final EjbcaWSClientImpl ejbcaWSclient = new EjbcaWSClientImpl();

    @GET
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all the end-entities stored to EJBCA database.
     *
     */
    public Response getRoot() {
        String output = "";
        List<NameAndId> nidList = ejbcaWSclient.getAvailableCAs();
        for (NameAndId nid : nidList) {
            UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_CA, UserMatch.MATCH_TYPE_EQUALS, nid.getName());
            List<UserDataVOWS> users = ejbcaWSclient.findUser(usermatch);
            for (UserDataVOWS user : users) {
                output += "Username: " + user.getUsername() + " CA: " + user.getCaName() + "<br>";
            }
        }
        return Response.status(RESTfulServiceStatus.OK).entity(output).build();
    }

    @GET
    @Path("{CA_NAME}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Fetch all the end-entities stored to EJBCA database for the given
     * Certificate Authority.
     *
     * @param name The name of the CA to fetch end-entities
     *
     * @return The end-entities that were found.
     */
    public Response getCAEndEntities(@PathParam("CA_NAME") String CA_NAME) {
        String output = "";
        UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_CA, UserMatch.MATCH_TYPE_EQUALS, CA_NAME);
        List<UserDataVOWS> users = ejbcaWSclient.findUser(usermatch);

        if (users == null) {
            return Response.status(RESTfulServiceStatus.INTERNAL_SERVER_ERROR).entity("Unexpected error...").build();
        }
        for (UserDataVOWS user : users) {
            output += "Username: " + user.getUsername() + " CA: " + user.getCaName() + "<br>";
        }

        return Response.status(RESTfulServiceStatus.OK).entity((output.isEmpty() ? "No entities found for CA: " + CA_NAME : output)).build();

    }

    @POST
    @Path("{username}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Creates an end-entity.
     *
     * @param name The name of the end-entity to create
     */
    public Response createEndEntity(@PathParam("username") String username, @QueryParam(value = "password") final String password, @QueryParam(value = "ca") final String CA, @QueryParam(value = "dn") final String DN, @QueryParam(value = "entityprofile") final String ENTITY_PROFILE, @QueryParam(value = "certificateprofile") final String CERTIFICATE_PROFILE, @QueryParam(value = "email") final String EMAIL, @QueryParam(value = "token") final String TOKEN, @QueryParam(value = "hardtoken") final String HARDTOKEN, @QueryParam(value = "subjectaltname") final String SUBJECTALTNAME) {
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, username);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, password);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, CA);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, DN);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, ENTITY_PROFILE);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, CERTIFICATE_PROFILE);
        if (EMAIL != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, EMAIL);
        }
        if (TOKEN != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, TOKEN);
        }
        if (HARDTOKEN != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.HARDTOKENSN, HARDTOKEN);
        }

        if (SUBJECTALTNAME != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTALTNAME, SUBJECTALTNAME);
        }

        boolean isCreateUserSuccess = ejbcaWSclient.editUser(ejbcaUser);

        if (isCreateUserSuccess) {
            return Response.status(RESTfulServiceStatus.OK).entity("End-Entity with name: " + username + " was created!").build();
        }

        return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Please ensure you have pass all required parameters").build();
    }

    @PUT
    @Path("{username}")
    @Produces(MediaType.TEXT_HTML)
    /**
     * Edit an existing end-entity.
     *
     * @param name The name of the end-entity to edit
     */
    public Response editEndEntity(@PathParam("username") String username, @QueryParam(value = "password") final String password, @QueryParam(value = "ca") final String CA, @QueryParam(value = "dn") final String DN, @QueryParam(value = "entityprofile") final String ENTITY_PROFILE, @QueryParam(value = "certificateprofile") final String CERTIFICATE_PROFILE, @QueryParam(value = "email") final String EMAIL, @QueryParam(value = "token") final String TOKEN, @QueryParam(value = "hardtoken") final String HARDTOKEN, @QueryParam(value = "subjectaltname") final String SUBJECTALTNAME) {
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, username);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, password);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, CA);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, DN);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, ENTITY_PROFILE);
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, CERTIFICATE_PROFILE);
        if (EMAIL != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, EMAIL);
        }
        if (TOKEN != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, TOKEN);
        }
        if (HARDTOKEN != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.HARDTOKENSN, HARDTOKEN);
        }

        if (SUBJECTALTNAME != null) {
            ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTALTNAME, SUBJECTALTNAME);
        }

        boolean isCreateUserSuccess = ejbcaWSclient.editUser(ejbcaUser);

        if (isCreateUserSuccess) {
            return Response.status(RESTfulServiceStatus.OK).entity("End-Entity with name: " + username + " was edited!").build();
        }

        return Response.status(RESTfulServiceStatus.BAD_REQUEST).entity("Please ensure you have pass all required parameters").build();
    }

}
