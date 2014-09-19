/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.core.protocol.scep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.operator.OperatorCreationException;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.certificate.request.ResponseStatus;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.Base64;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.config.ScepConfiguration;
import org.ejbca.config.WebConfiguration;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.config.ConfigurationSessionRemote;
import org.ejbca.core.ejb.ra.EndEntityManagementSessionRemote;
import org.ejbca.core.model.InternalEjbcaResources;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Tests http pages of scep
 */
public class ProtocolScepHttpRATest extends CaTestCase {
    private static final Logger log = Logger.getLogger(ProtocolScepHttpTest.class);
    private static final AuthenticationToken admin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("ProtocolScepHttpTest"));

    private static final String resourceScepRa =     "publicweb/apply/scep/ra/pkiclient.exe";
    private static final String resourceScepRaNoCA = "publicweb/apply/scep/ra/noca/pkiclient.exe";

    private static X509Certificate cacert;
    private String caname = getTestCAName();
    private static String senderNonce = null;
    private static String transId = null;

    private Random rand = new Random();
    private String httpReqPath;

    private ConfigurationSessionRemote configurationSessionRemote = EjbRemoteHelper.INSTANCE.getRemoteSession(ConfigurationSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private EndEntityManagementSessionRemote endEntityManagementSession = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityManagementSessionRemote.class);

    @BeforeClass
    public static void beforeClass() {
        // Install BouncyCastle provider
        CryptoProviderTools.installBCProvider();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final String httpPort = configurationSessionRemote.getProperty(WebConfiguration.CONFIG_HTTPSERVERPUBHTTP);
        httpReqPath = "http://127.0.0.1:" + httpPort + "/ejbca";
        cacert = (X509Certificate) getTestCACert();
        configurationSessionRemote.backupConfiguration();

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        configurationSessionRemote.restoreConfiguration();
    }

    public String getRoleName() {
        return this.getClass().getSimpleName(); 
    }
    
    /**
     * Tests the RA specific SCEP URLs
     * @throws Exception
     */
    @Test
    public void test01Access() throws Exception {
        // Hit scep, gives a 400: Bad Request        
        WebClient webClient = new WebClient();
        WebConnection con = webClient.getWebConnection();
        WebRequestSettings settings = new WebRequestSettings(new URL(httpReqPath + '/' + resourceScepRa));
        WebResponse resp = con.getResponse(settings);
        assertEquals("Response code", 400, resp.getStatusCode());
        
        webClient = new WebClient();
        con = webClient.getWebConnection();
        settings = new WebRequestSettings(new URL(httpReqPath + '/' + resourceScepRaNoCA));
        resp = con.getResponse(settings);
        assertEquals("Response code", 400, resp.getStatusCode());
    }

    /**
     * Happy path test. Sends a good SCEP request, with CA certificate, for a new end entity that will be created while handling the request 
     * 
     * @throws Exception
     */
    @Test
    public void test02ScepRequestRAMode() throws Exception {
        log.trace(">test02ScepRequestRAMode()");
  
        updatePropertyOnServer(ScepConfiguration.SCEP_EDITUSER, "true");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_DEFAULTCA, caname);
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_SCHEME, "DN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_PARAMETERS, "CN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_AUTHPWD, "none");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_ENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_CERTPROFILE, "ENDUSER");
        updatePropertyOnServer("scep.defaultca", caname);
        
        String userdn = "C=SE,O=PrimeKey,CN=scepratest";
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        byte[] msgBytes = genScepRequest(false, CMSSignedGenerator.DIGEST_SHA1, userdn, keys);
        // Send message with GET
        byte[] retMsg = sendScep(false, msgBytes, resourceScepRa, false, HttpServletResponse.SC_OK);
        
        assertNotNull("Response can not be null.", retMsg);
        assertTrue(retMsg.length > 0);
        
        checkScepResponse(retMsg, userdn, senderNonce, transId, false, CMSSignedGenerator.DIGEST_SHA1, false, keys);
        
        try{
            endEntityManagementSession.revokeAndDeleteUser(admin, "scepratest", ReasonFlags.unused);
        } catch(Exception e) {}
        
        log.trace("<test02ScepRequestRAMode()");
    }
    
    /**
     * Happy path test. Sends a good SCEP request, without CA certificate, for a new end entity that will be created while handling the request
     *  
     * @throws Exception
     */
    @Test
    public void test03ScepRequestRAModeNoCA() throws Exception {
        log.trace(">test03ScepRequestRAModeNoCA()");
        
        updatePropertyOnServer(ScepConfiguration.SCEP_EDITUSER, "true");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_DEFAULTCA, caname);
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_SCHEME, "DN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_PARAMETERS, "CN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_AUTHPWD, "none");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_ENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_CERTPROFILE, "ENDUSER");
        updatePropertyOnServer("scep.defaultca", caname);

        String userdn = "C=SE,O=PrimeKey,CN=scepranocerttest";
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        byte[] msgBytes = genScepRequest(false, CMSSignedGenerator.DIGEST_SHA1, userdn, keys);

        byte[] retMsg = sendScep(false, msgBytes, resourceScepRaNoCA, true, HttpServletResponse.SC_OK);
        assertNotNull("Response can not be null.", retMsg);
        assertTrue(retMsg.length > 0);
        
        checkScepResponse(retMsg, userdn, senderNonce, transId, false, CMSSignedGenerator.DIGEST_SHA1, true, keys);
        
        try{
            endEntityManagementSession.revokeAndDeleteUser(admin, "scepranocerttest", ReasonFlags.unused);
        } catch(Exception e) {}
        
        log.trace("<test03ScepRequestRAModeNoCA()");
    }
    
    /**
     * Sending a SCEP request to the RA specific SCEP URL while RA mode is disabled (aka. 'scep.ra.createOrEditUser' is set to 'false').
     * Error 403 is expected in return.
     * 
     * @throws Exception
     */
    @Test
    public void test04ScepRequestRADisabled() throws Exception {
        log.trace(">test03ScepRequestRAModeNoCA()");
        
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_DEFAULTCA, caname);
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_SCHEME, "DN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_PARAMETERS, "CN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_AUTHPWD, "none");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_ENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_CERTPROFILE, "ENDUSER");
        updatePropertyOnServer("scep.defaultca", caname);
        
        assertFalse("The SCEP RA mode should be disabled by default, but it is not", ScepConfiguration.getAddOrEditUser());

        String userdn = "C=SE,O=PrimeKey,CN=scepradisableduser";
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        byte[] msgBytes = genScepRequest(false, CMSSignedGenerator.DIGEST_SHA1, userdn, keys);
        // Send message with GET
        byte[] retMsg = sendScep(false, msgBytes, resourceScepRaNoCA, true, HttpServletResponse.SC_FORBIDDEN);
        assertNotNull("Response can not be null.", retMsg);
        assertTrue(retMsg.length > 0);
        
        try{
            endEntityManagementSession.revokeAndDeleteUser(admin, "scepradisableduser", ReasonFlags.unused);
        } catch(Exception e) {}
        
        log.trace("<test03ScepRequestRAModeNoCA()");
    }
    
    /**
     * Sending a SCEP request to the default SCEP URL (not RA) while RA mode is enabled (aka. 'scep.ra.createOrEditUser' is set to 'true').
     * Expected an error message saying that the user in the request does not already exist.
     * 
     * @throws Exception
     */
    @Test
    public void test04ScepRequestRAEnabledWrongURL() throws Exception {
        log.trace(">test04ScepRequestRAEnabledWrongURL()");
        
        updatePropertyOnServer(ScepConfiguration.SCEP_EDITUSER, "true");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_DEFAULTCA, caname);
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_SCHEME, "DN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_PARAMETERS, "CN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_AUTHPWD, "none");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_ENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_CERTPROFILE, "ENDUSER");
        updatePropertyOnServer("scep.defaultca", caname);
        
        String userdn = "C=SE,O=PrimeKey,CN=scepradisableduser";
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        byte[] msgBytes = genScepRequest(false, CMSSignedGenerator.DIGEST_SHA1, userdn, keys);
        // Send message with GET
        byte[] retMsg = sendScep(false, msgBytes, "publicweb/apply/scep/pkiclient.exe", true, HttpServletResponse.SC_BAD_REQUEST);
        assertNotNull("Response can not be null.", retMsg);
        assertTrue(retMsg.length > 0);
        String expectedMessage = "User scepradisableduser not found.";
        String returnMessageString = new String(retMsg);
        assertTrue("Wrong return message: "+returnMessageString, returnMessageString.indexOf(expectedMessage) >= 0);
        try{
            endEntityManagementSession.revokeAndDeleteUser(admin, "scepradisableduser", ReasonFlags.unused);
        } catch(Exception e) {}
        
        log.trace("<test04ScepRequestRAEnabledWrongURL()");
    }

    /**
     * Happy path test. Sends a good SCEP request, with CA certificate and a authentication password, for a new end entity 
     * that will be created while handling the request 
     * 
     * @throws Exception
     */
    @Test
    public void test05ScepRequestRAModeWithAuthPWD() throws Exception {
        log.trace(">test02ScepRequestRAMode()");
  
        updatePropertyOnServer(ScepConfiguration.SCEP_EDITUSER, "true");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_DEFAULTCA, caname);
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_SCHEME, "DN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_NAME_GENERATION_PARAMETERS, "CN");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_AUTHPWD, "foo123");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_ENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(ScepConfiguration.SCEP_RA_CERTPROFILE, "ENDUSER");
        updatePropertyOnServer("scep.defaultca", caname);
        
        String userdn = "C=SE,O=PrimeKey,CN=scepratest";
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        byte[] msgBytes = genScepRequest(false, CMSSignedGenerator.DIGEST_SHA1, userdn, keys);
        // Send message with GET
        byte[] retMsg = sendScep(false, msgBytes, resourceScepRa, false, HttpServletResponse.SC_OK);
        
        assertNotNull("Response can not be null.", retMsg);
        assertTrue(retMsg.length > 0);
        
        checkScepResponse(retMsg, userdn, senderNonce, transId, false, CMSSignedGenerator.DIGEST_SHA1, false, keys);
        
        try{
            endEntityManagementSession.revokeAndDeleteUser(admin, "scepratest", ReasonFlags.unused);
        } catch(Exception e) {}
        
        log.trace("<test02ScepRequestRAMode()");
    }
    

    private byte[] genScepRequest(boolean makeCrlReq, String digestoid, String userDN, KeyPair key) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidAlgorithmParameterException, CertStoreException,
            IOException, CMSException, IllegalStateException, OperatorCreationException, CertificateException {
        ScepRequestGenerator gen = new ScepRequestGenerator();
        gen.setKeys(key);
        gen.setDigestOid(digestoid);
        byte[] msgBytes = null;
        // Create a transactionId
        byte[] randBytes = new byte[16];
        this.rand.nextBytes(randBytes);
        byte[] digest = CertTools.generateMD5Fingerprint(randBytes);
        transId = new String(Base64.encode(digest));

        if (makeCrlReq) {
            msgBytes = gen.generateCrlReq(userDN, transId, cacert);
        } else {
            msgBytes = gen.generateCertReq(userDN, "foo123", transId, cacert);
        }
        assertNotNull(msgBytes);
        senderNonce = gen.getSenderNonce();
        byte[] nonceBytes = Base64.decode(senderNonce.getBytes());
        assertTrue(nonceBytes.length == 16);
        return msgBytes;
    }

    private void checkScepResponse(byte[] retMsg, String userDN, String _senderNonce, String _transId, boolean crlRep, String digestOid, boolean noca, KeyPair keys)
            throws CMSException, NoSuchProviderException, NoSuchAlgorithmException, CertStoreException, InvalidKeyException, CertificateException,
            SignatureException, CRLException {
        //
        // Parse response message
        //
        CMSSignedData s = new CMSSignedData(retMsg);
        // The signer, i.e. the CA, check it's the right CA
        SignerInformationStore signers = s.getSignerInfos();
        @SuppressWarnings("unchecked")
        Collection<SignerInformation> col = signers.getSigners();
        assertTrue(col.size() > 0);
        Iterator<SignerInformation> iter = col.iterator();
        SignerInformation signerInfo = iter.next();
        // Check that the message is signed with the correct digest alg
        assertEquals(signerInfo.getDigestAlgOID(), digestOid);
        SignerId sinfo = signerInfo.getSID();
        // Check that the signer is the expected CA
        assertEquals(CertTools.stringToBCDNString(cacert.getIssuerDN().getName()), CertTools.stringToBCDNString(sinfo.getIssuer().toString()));
        // Verify the signature
        boolean ret = signerInfo.verify(cacert.getPublicKey(), "BC");
        assertTrue(ret);
        // Get authenticated attributes
        AttributeTable tab = signerInfo.getSignedAttributes();
        // --Fail info
        Attribute attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_failInfo));
        // No failInfo on this success message
        assertNull(attr);
        // --Message type
        attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_messageType));
        assertNotNull(attr);
        ASN1Set values = attr.getAttrValues();
        assertEquals(values.size(), 1);
        ASN1String str = DERPrintableString.getInstance((values.getObjectAt(0)));
        String messageType = str.getString();
        assertEquals("3", messageType);
        // --Success status
        attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_pkiStatus));
        assertNotNull(attr);
        values = attr.getAttrValues();
        assertEquals(values.size(), 1);
        str = DERPrintableString.getInstance((values.getObjectAt(0)));
        assertEquals(ResponseStatus.SUCCESS.getStringValue(), str.getString());
        // --SenderNonce
        attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_senderNonce));
        assertNotNull(attr);
        values = attr.getAttrValues();
        assertEquals(values.size(), 1);
        ASN1OctetString octstr = ASN1OctetString.getInstance(values.getObjectAt(0));
        // SenderNonce is something the server came up with, but it should be 16
        // chars
        assertTrue(octstr.getOctets().length == 16);
        // --Recipient Nonce
        attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_recipientNonce));
        assertNotNull(attr);
        values = attr.getAttrValues();
        assertEquals(values.size(), 1);
        octstr = ASN1OctetString.getInstance(values.getObjectAt(0));
        // recipient nonce should be the same as we sent away as sender nonce
        assertEquals(_senderNonce, new String(Base64.encode(octstr.getOctets())));
        // --Transaction ID
        attr = tab.get(new ASN1ObjectIdentifier(ScepRequestMessage.id_transId));
        assertNotNull(attr);
        values = attr.getAttrValues();
        assertEquals(values.size(), 1);
        str = DERPrintableString.getInstance((values.getObjectAt(0)));
        // transid should be the same as the one we sent
        assertEquals(_transId, str.getString());

        // First we extract the encrypted data from the CMS enveloped data
        // contained
        // within the CMS signed data
        final CMSProcessable sp = s.getSignedContent();
        final byte[] content = (byte[]) sp.getContent();
        final CMSEnvelopedData ed = new CMSEnvelopedData(content);
        final RecipientInformationStore recipients = ed.getRecipientInfos();
        CertStore certstore;
        {
            @SuppressWarnings("unchecked")
            Collection<RecipientInformation> c = recipients.getRecipients();
            assertEquals(c.size(), 1);
            Iterator<RecipientInformation> it = c.iterator();
            byte[] decBytes = null;
            RecipientInformation recipient = it.next();
            decBytes = recipient.getContent(keys.getPrivate(), "BC");
            // This is yet another CMS signed data
            CMSSignedData sd = new CMSSignedData(decBytes);
            // Get certificates from the signed data
            certstore = sd.getCertificatesAndCRLs("Collection", "BC");
            assertNotNull(certstore);
        }
        if (crlRep) {
            // We got a reply with a requested CRL
            @SuppressWarnings("unchecked")
            final Collection<X509CRL> crls = (Collection<X509CRL>) certstore.getCRLs(null);
            assertEquals(crls.size(), 1);
            final Iterator<X509CRL> it = crls.iterator();
            // CRL is first (and only)
            final X509CRL retCrl = it.next();
            log.info("Got CRL with DN: " + retCrl.getIssuerDN().getName());
            
            // check the returned CRL
            assertEquals(cacert.getSubjectDN().getName(), retCrl.getIssuerDN().getName());
            retCrl.verify(cacert.getPublicKey());
        } else {
            // We got a reply with a requested certificate
            @SuppressWarnings("unchecked")
            final Collection<X509Certificate> certs = (Collection<X509Certificate>) certstore.getCertificates(null);
            // EJBCA returns the issued cert and the CA cert (cisco vpn
            // client requires that the ca cert is included)
            if (noca) {
                assertEquals(certs.size(), 1);
            } else {
                assertEquals(certs.size(), 2);
            }
            final Iterator<X509Certificate> it = certs.iterator();
            // Issued certificate must be first
            boolean verified = false;
            boolean gotcacert = false;
            while (it.hasNext()) {
                X509Certificate retcert = it.next();
                log.info("Got cert with DN: " + retcert.getSubjectDN().getName());
                
                // check the returned certificate
                String subjectdn = CertTools.stringToBCDNString(retcert.getSubjectDN().getName());
                if (CertTools.stringToBCDNString(userDN).equals(subjectdn)) {
                    // issued certificate
                    assertEquals(CertTools.stringToBCDNString(userDN), subjectdn);
                    assertEquals(cacert.getSubjectDN().getName(), retcert.getIssuerDN().getName());
                    retcert.verify(cacert.getPublicKey());
                    assertTrue(checkKeys(keys.getPrivate(), retcert.getPublicKey()));
                    verified = true;
                } else {
                    // ca certificate
                    assertEquals(cacert.getSubjectDN().getName(), retcert.getSubjectDN().getName());
                    gotcacert = true;
                }
            }
            assertTrue(verified);
            if (noca) {
                assertFalse(gotcacert);
            } else {
                assertTrue(gotcacert);
            }
        }

    }

    /**
     * checks that a public and private key matches by signing and verifying a message
     */
    private boolean checkKeys(PrivateKey priv, PublicKey pub) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signer = Signature.getInstance("SHA1WithRSA");
        signer.initSign(priv);
        signer.update("PrimeKey".getBytes());
        byte[] signature = signer.sign();

        Signature signer2 = Signature.getInstance("SHA1WithRSA");
        signer2.initVerify(pub);
        signer2.update("PrimeKey".getBytes());
        return signer2.verify(signature);
    }

    private byte[] sendScep(boolean post, byte[] scepPackage,String resource, boolean noca, int responseCode) throws IOException {
        String urlString = httpReqPath + '/' + resource + "?operation=PKIOperation";
        log.debug("UrlString =" + urlString);
        final HttpURLConnection con;
        if (post) {
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.connect();
            // POST it
            OutputStream os = con.getOutputStream();
            os.write(scepPackage);
            os.close();
        } else {
            String reqUrl = urlString + "&message=" + URLEncoder.encode(new String(Base64.encode(scepPackage)), "UTF-8");
            URL url = new URL(reqUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.getDoOutput();
            con.connect();
        }

        assertEquals("Response code", responseCode, con.getResponseCode());
        // Some appserver (Weblogic) responds with
        // "application/x-pki-message; charset=UTF-8"
        if (responseCode == HttpServletResponse.SC_OK) {
            assertTrue(con.getContentType().startsWith("application/x-pki-message"));
        } else {
            assertTrue(con.getContentType().startsWith("text/html"));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // This works for small requests, and SCEP requests are small enough
        final InputStream in;
        if (responseCode == HttpServletResponse.SC_OK) {
            in = con.getInputStream();
        } else {
            in = con.getErrorStream();
        }
        int b = in.read();
        while (b != -1) {
            baos.write(b);
            b = in.read();
        }
        baos.flush();
        in.close();
        byte[] respBytes = baos.toByteArray();
        assertNotNull("Response can not be null.", respBytes);
        assertTrue(respBytes.length > 0);
        return respBytes;
    }

    private void updatePropertyOnServer(String property, String value) {
        final String msg = "Setting property on server: " + property + "=" + value;
        log.debug(msg);
        boolean ret = EjbRemoteHelper.INSTANCE.getRemoteSession(ConfigurationSessionRemote.class, EjbRemoteHelper.MODULE_TEST).updateProperty(property, value);
        if (!ret) {
            throw new IllegalStateException("Failed operation: "+msg);
        }
    }

    static class InternalResourcesStub extends InternalEjbcaResources {

        private static final long serialVersionUID = 1L;
        private static final Logger log = Logger.getLogger(InternalResourcesStub.class);

        private InternalResourcesStub() {

            setupResources();

        }

        private void setupResources() {
            String primaryLanguage = PREFEREDINTERNALRESOURCES.toLowerCase();
            String secondaryLanguage = SECONDARYINTERNALRESOURCES.toLowerCase();

            InputStream primaryStream = null;
            InputStream secondaryStream = null;

            primaryLanguage = "en";
            secondaryLanguage = "se";
            try {
                primaryStream = new FileInputStream("src/intresources/intresources." + primaryLanguage + ".properties");
                secondaryStream = new FileInputStream("src/intresources/intresources." + secondaryLanguage + ".properties");

                try {
                    primaryEjbcaResource.load(primaryStream);
                    secondaryEjbcaResource.load(secondaryStream);
                } catch (IOException e) {
                    log.error("Error reading internal resourcefile", e);
                }

            } catch (FileNotFoundException e) {
                log.error("Localization files not found", e);

            } finally {
                try {
                    if (primaryStream != null) {
                        primaryStream.close();
                    }
                    if (secondaryStream != null) {
                        secondaryStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error closing internal resources language streams: ", e);
                }
            }

        }

        public static synchronized InternalEjbcaResources getInstance() {
            if (instance == null) {
                instance = new InternalResourcesStub();
            }
            return instance;
        }

    }
}
