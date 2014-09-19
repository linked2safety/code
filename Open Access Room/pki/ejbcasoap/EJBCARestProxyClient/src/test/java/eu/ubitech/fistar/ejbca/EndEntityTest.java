package eu.ubitech.fistar.ejbca;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaUser;
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.common.CertificateHelper;

/**
 * Unit test for End Entity(User) SOAP operations.
 */
public class EndEntityTest
        extends TestCase {

    EjbcaWSClientImpl EJBCAWSClient = new EjbcaWSClientImpl();

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EndEntityTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(EndEntityTest.class);
    }

    /**
     * Create a new end-entity(user) to EJBCA Database.
     */
    public void testAddEndEntity() {
        boolean status = EJBCAWSClient.editUser(this.getEJBCADummyUser());
        assertTrue(status);
    }

    /**
     * Fetch all the end-entities stored to EJBCA database for the given
     * Certificate Authority.
     */
    public void testFindEndEntity() {
        UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_CA, UserMatch.MATCH_TYPE_EQUALS, "FISTARManagementCA");
        assert (EJBCAWSClient.findUser(usermatch).size() > 0);
    }

    public void testGetEndEntityCertificate() {
        Certificate cert = EJBCAWSClient.getCertificate("4AB7F56FEF983DF1", "CN=ManagementCA,O=EJBCA Sample,C=SE");
        assertNotNull(cert);
    }

    public void testCreateSoftTokenRequest() {

        assertNotNull(EJBCAWSClient.createSoftTokenRequest(this.getEJBCADummyUser()));
    }

    public void testCertificateRequestFromCSR() {
        this.testAddEndEntity();
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("KS/smapleCSR.csr");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(newLine);
            }
        } catch (IOException ex) {
            Logger.getLogger(EndEntityTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        CertificateResponse certificateResponse = EJBCAWSClient.crmfRequest("testEjbcaUser2", "testPassword", sb.toString(), null, CertificateHelper.RESPONSETYPE_CERTIFICATE);
        assertNotNull(certificateResponse);
    }

    //Help function - return a dummy ejbca user object
    private EjbcaUser getEJBCADummyUser() {
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, "testEjbcaUser2");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, "testPassword");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, "FISTARManagementCA");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, "CN=test user2");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, "FISTARUser");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, "ENDUSER");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, "test@testemail.com");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, UserDataVOWS.TOKEN_TYPE_USERGENERATED);
        return ejbcaUser;
    }
}
