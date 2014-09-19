package eu.ubitech.fistar.ejbca;

import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.security.cert.X509CRL;
import static junit.framework.Assert.assertNotNull;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Certificate Revocation List SOAP operations.
 */
public class CertificateRevocationListTest
        extends TestCase {

    EjbcaWSClientImpl EJBCAWSClient = new EjbcaWSClientImpl();

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CertificateRevocationListTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CertificateRevocationListTest.class);
    }

    /**
     * Check the status of a certificate (returns a revoked reason of the
     * certificate)
     */
    public void testCreateCRL() {
        assertTrue(EJBCAWSClient.createCRL("FISTARManagementCA"));
    }

        public void testGetLatestCRL() {
        X509CRL crl = EJBCAWSClient.getLatestCRL("FISTARManagementCA", false);
        assertNotNull(crl);
    }
}
