
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaUser;
import eu.ubitech.fistar.ejbca.proxy.client.EjbcaWSClientImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.common.CertificateHelper;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
public class EJBCAWebServiceTester {

    private static EjbcaWSClientImpl EJBCAWSClient = new EjbcaWSClientImpl();

    public static void main(String[] args) {
        if (invoke()) {
            System.out.println("EJBCA is running and invocation was success....");
            testGetCACert();
            //System.out.println("Test1: CheckRevokationStatus");
            //testCheckRevokationStatus();
            // System.out.println("Test2: AddNewUser");
            //testAddNewUser();
            //testAddNewUser2();
            //System.out.println("Test3: RevokeUserCert");
            //testRevokeUserCert();
            //System.out.println("Test4: GetIssuedCert");
            //testGetIssuedCert();
            //System.out.println("Test5: CreateCRL");
            //testCreateCRL();
            //System.out.println("Test6: GetLatestCRL");
            //testGetLatestCRL();
            //System.out.println("Test7: FindUser");
            //testFindUser();
            //System.out.println("Test8: GetAvailableCAs");
            //testGetAvailableCAs();
            //System.out.println("Test9: GetAuthorizedEndEntityProfiles");
            //testGetAuthorizedEndEntityProfiles();
            //System.out.println("Test10: GetAvailableCertificateProfiles");
            //testGetAvailableCertificateProfiles();
            //testCrmfRequest();
        }
    }

    private static void testGetCACert() {
        List<Certificate> certs = EJBCAWSClient.getCACert("FISTARManagementCA");
        EJBCAWSClient.writeTOdisk(EJBCAWSClient.getfilepath("/home/promitheas/Downloads", "mycertca2"), certs.get(0).getCertificateData());
    }

    private static void testCrmfRequest() {
        try {
            StringBuilder sb = new StringBuilder();
            //List<String> list = Files.readAllLines(Paths.get("/home/promitheas/Downloads/tmpcert/csrfilename.csr"), Charset.forName("UTF-8"));
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("KS/smapleCSR.csr");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(newLine);
            }
            /* for (String s : list) {
             sb.append(s.concat("\n"));
             }*/
            System.out.println(sb.toString());
            CertificateResponse certificateResponse = EJBCAWSClient.crmfRequest("test", "test", sb.toString(), null, CertificateHelper.RESPONSETYPE_CERTIFICATE);
            X509Certificate cert = certificateResponse.getCertificate();
            EJBCAWSClient.writeTOdisk(EJBCAWSClient.getfilepath("/home/promitheas/Downloads", "mycert"), certificateResponse.getData());
            //EJBCAWSClient.storeX509Certificate(cert, "/home/promitheas/Downloads", "mycert");
        } catch (IOException ex) {
            Logger.getLogger(EJBCAWebServiceTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(EJBCAWebServiceTester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void testGetAvailableCertificateProfiles() {
        EJBCAWSClient.getAvailableCertificateProfiles(1806334636);
    }

    private static void testGetAuthorizedEndEntityProfiles() {
        EJBCAWSClient.getAuthorizedEndEntityProfiles();
    }

    private static void testGetAvailableCAs() {
        EJBCAWSClient.getAvailableCAs();

    }

    private static void testFindUser() {
        UserMatch usermatch = new UserMatch(UserMatch.MATCH_WITH_CA, UserMatch.MATCH_TYPE_EQUALS, "CIEC Sign Gold CA");
        EJBCAWSClient.findUser(usermatch);
    }

    private static void testGetLatestCRL() {
        X509CRL crl = EJBCAWSClient.getLatestCRL("CIEC Sign Gold CA", false);
        EJBCAWSClient.storeCRL(crl, "/home/promitheas/Downloads", "mycrl");
    }

    private static void testCreateCRL() {
        EJBCAWSClient.createCRL("CIEC Sign Gold CA");
    }

    private static void testGetIssuedCert() {
        Certificate cert = EJBCAWSClient.getCertificate("4CA55328EFB349B7", "CN=CIEC Sign Gold CA");
        EJBCAWSClient.storeCertificate(cert, "binary", "/home/promitheas/Downloads", "mycert2");

    }

    private static void testRevokeUserCert() {
        EJBCAWSClient.revokeUserCert("CN=CIEC Sign Gold CA", "5FD08B3E1E17EE23", RevokeStatus.REVOKATION_REASON_SUPERSEDED);

    }

    private static void testCheckRevokationStatus() {
        EJBCAWSClient.checkCertificateRevokeStatus("CN=CIEC Sign Gold CA", "5FD08B3E1E17EE23"); // 60EB2A11D8229814
    }

    private static void testAddNewUser() {
        EjbcaUser ejbcaUser = new EjbcaUser();
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, "test");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, "test");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, "CIEC Sign Gold CA");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, "CN=testa user3aa");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, "CIEC officer");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, "CIEC user");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, "test@testemail.com");
        ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, UserDataVOWS.TOKEN_TYPE_USERGENERATED);
        boolean status = EJBCAWSClient.editUser(ejbcaUser);
        //EJBCAWSClient.storeKeystore(EJBCAWSClient.createSoftTokenRequest(ejbcaUser), "P12", "binary", "/home/promitheas/Downloads", "mykeystore");

    }

    private static void testAddNewUser2() {
        String username = "FR_AmblÃ©on RO";
        String password = "Xola7Dat";
        String dn = "CN=AmblÃ©on RO1,OU=civil status,O=public sector,C=FR,UID=e9b7f567c3d34e6d822e";  //
        String caname = "CIEC Sign Gold CA";
        String eeprofilename = "CIEC officer";
        String cerprofilename = "CIEC user";
        //------------
        UserDataVOWS user1 = new UserDataVOWS();
        user1.setUsername(username);
        user1.setPassword(password);
        user1.setSubjectDN(dn);
        user1.setCaName(caname);
        user1.setEmail("p.polydoras@yahoo.gr");
        user1.setSubjectAltName(null);
        user1.setEndEntityProfileName(eeprofilename);
        user1.setCertificateProfileName(cerprofilename);
        user1.setTokenType(UserDataVOWS.TOKEN_TYPE_P12);
        user1.setKeyRecoverable(false);

        //EjbcaUser ejbcaUser = new EjbcaUser();
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.USERNAME, "testEjbcaUser");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.PASSWORD, "testPassword");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CA, "CIEC Sign Gold CA");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.SUBJECTDN, "CN=testa user2");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE, "CIEC officer");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE, "CIEC user");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.EMAIL, "test@testemail.com");
        //ejbcaUser.setEntityArgument(EjbcaUser.Arguments.TOKEN, UserDataVOWS.TOKEN_TYPE_P12);
        try {
            //KeyStore keystore = EJBCAWSClient.editUser(ejbcaUser);
            //EJBCAWSClient.storeKeystore(keystore, "P12", "binary", "/home/promitheas/Downloads", "mykeystore");
            //ServiceClientImpl.getEjbcaRAWS().editUser(user1);
            List<Certificate> list = EjbcaWSClientImpl.getEjbcaRAWS().findCerts("test", true);
            System.out.println("list.size: " + list.size());
        } catch (Exception ex) {
            Logger.getLogger(EJBCAWebServiceTester.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    static boolean invoke() {
        //1 - Retrieve the current version of EJBCA Instance
        System.out.println("Retrieving EJBCA current version: " + EJBCAWSClient.getEjbcaRAWS().getEjbcaVersion());
        return (EJBCAWSClient.getEjbcaRAWS().getEjbcaVersion() != null);
    }

}
