package eu.ubitech.fistar.ejbca;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;

/**
 * Unit test for Certificate Status SOAP operations.
 */
public class CertificateStatusTest
        extends TestCase {

    EjbcaWSClientImpl EJBCAWSClient = new EjbcaWSClientImpl();

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CertificateStatusTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CertificateStatusTest.class);
    }

    /**
     * Check the status of a certificate (returns a revoked reason of the
     * certificate).
     */
    public void testCheckCertificateStatus() {
        assert (EJBCAWSClient.checkCertificateRevokeStatus("CN=ManagementCA,O=EJBCA Sample,C=SE", "4AB7F56FEF983DF1").getReason() == RevokeStatus.REVOKATION_REASON_SUPERSEDED);
    }

    /**
     * Revoke user's certificate.
     * 
     */
    public void testRevokeUserCert() {
        assertFalse(EJBCAWSClient.revokeUserCert("CN=ManagementCA,O=EJBCA Sample,C=SE", "4AB7F56FEF983DF1", RevokeStatus.REVOKATION_REASON_SUPERSEDED));
    }

}
