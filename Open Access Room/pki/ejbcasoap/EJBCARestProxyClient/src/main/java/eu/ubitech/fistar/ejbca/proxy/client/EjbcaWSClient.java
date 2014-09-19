/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbca.proxy.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWSService;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
public enum EjbcaWSClient {

    INSTANCE;

    /**
     * Base class inherited by all EJBCA RA WS cli commands. Checks the property
     * file and creates a webservice connection.
     *
     * @author Chris Paraskeva
     */
    private EjbcaWS ejbcaraws = null;
    final private URL webServiceURL;
    final private Exception exception;

    private static final String[] REASON_TEXTS = {"NOT REVOKED",
        "REV_UNSPECIFIED", "REV_KEYCOMPROMISE", "REV_CACOMPROMISE",
        "REV_AFFILIATIONCHANGED", "REV_SUPERSEDED", "REV_CESSATIONOFOPERATION",
        "REV_CERTIFICATEHOLD", "REV_REMOVEFROMCRL", "REV_PRIVILEGEWITHDRAWN",
        "REV_AACOMPROMISE"};

    public static final int NOT_REVOKED = RevokeStatus.NOT_REVOKED;
    public static final int REVOKATION_REASON_UNSPECIFIED = RevokeStatus.REVOKATION_REASON_UNSPECIFIED;
    public static final int REVOKATION_REASON_KEYCOMPROMISE = RevokeStatus.REVOKATION_REASON_KEYCOMPROMISE;
    public static final int REVOKATION_REASON_CACOMPROMISE = RevokeStatus.REVOKATION_REASON_CACOMPROMISE;
    public static final int REVOKATION_REASON_AFFILIATIONCHANGED = RevokeStatus.REVOKATION_REASON_AFFILIATIONCHANGED;
    public static final int REVOKATION_REASON_SUPERSEDED = RevokeStatus.REVOKATION_REASON_SUPERSEDED;
    public static final int REVOKATION_REASON_CESSATIONOFOPERATION = RevokeStatus.REVOKATION_REASON_CESSATIONOFOPERATION;
    public static final int REVOKATION_REASON_CERTIFICATEHOLD = RevokeStatus.REVOKATION_REASON_CERTIFICATEHOLD;
    public static final int REVOKATION_REASON_REMOVEFROMCRL = RevokeStatus.REVOKATION_REASON_REMOVEFROMCRL;
    public static final int REVOKATION_REASON_PRIVILEGESWITHDRAWN = RevokeStatus.REVOKATION_REASON_PRIVILEGESWITHDRAWN;
    public static final int REVOKATION_REASON_AACOMPROMISE = RevokeStatus.REVOKATION_REASON_AACOMPROMISE;

    private static final int[] REASON_VALUES = {NOT_REVOKED, REVOKATION_REASON_UNSPECIFIED,
        REVOKATION_REASON_KEYCOMPROMISE, REVOKATION_REASON_CACOMPROMISE,
        REVOKATION_REASON_AFFILIATIONCHANGED, REVOKATION_REASON_SUPERSEDED,
        REVOKATION_REASON_CESSATIONOFOPERATION, REVOKATION_REASON_CERTIFICATEHOLD,
        REVOKATION_REASON_REMOVEFROMCRL, REVOKATION_REASON_PRIVILEGESWITHDRAWN,
        REVOKATION_REASON_AACOMPROMISE};

    EjbcaWSClient() { //Thread.currentThread().getContextClassLoader().getResourceAsStream("./KS/keystore.jks");

        TrustManagerFactory trustManagerFactory = null;
        KeyManagerFactory keyManagerFactory = null;
        KeyStore trustStore = null;
        KeyStore identitystore = null;
        final Properties props = new Properties();
        URL tmpURL = null;
        Exception tmpException = null;
        try {
            try {
                //props.load(new FileInputStream("ejbcawsra.properties"));
                props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ejbcawsra.properties"));

            } catch (FileNotFoundException e) {
                Logger.getLogger(EjbcaWSClient.class.getName()).severe(e.getMessage());
            }
            CryptoProviderTools.installBCProvider();

            //Set Trustore file path and certificate password
            final String trustStorePath = props.getProperty("truststore.path");
            final String truststorePassword = props.getProperty("truststore.password");
            if (trustStorePath != null) {
                checkIfFileExists(trustStorePath);

                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                InputStream truststoreStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(trustStorePath);
                trustStore.load(truststoreStream, truststorePassword.toCharArray());
                trustManagerFactory.init(trustStore);

                /*Used only to pass SSL parameter from filesytem*/
                //System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                //System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
            }

            //Set Keystore file path and certificate password
            final String keyStorePath = props.getProperty("keystore.path", "keystore.jks");
            final String keystorePassword = props.getProperty("keystore.password");
            if (keyStorePath != null) {
                checkIfFileExists(keyStorePath);
                identitystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                InputStream keystoreStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStorePath);
                identitystore.load(keystoreStream, keystorePassword.toCharArray());
                keyManagerFactory.init(identitystore, keystorePassword.toCharArray());

                /*Used only to pass SSL parameter from filesytem*/
                //System.setProperty("javax.net.ssl.keyStore", keyStorePath);
                //System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
            }

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLContext.setDefault(sc); //Alternative Use: HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         
            tmpURL = new URL(props.getProperty("ejbcaws.url", "https://localhost:8443/ejbca/ejbcaws/ejbcaws") + "?wsdl");
            //Security.setProperty("ssl.KeyManagerFactory.algorithm", "NewSunX509");
        } catch (Exception e) {
            tmpException = e;
        }
        this.exception = tmpException;
        this.webServiceURL = tmpURL;
    }

    private void checkIfFileExists(String fileName) throws Exception {
        if (fileName.equals("NONE")) {
            return;
        }
        // final File f = new File(fileName);
        // if (!f.exists()) {

        if (Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName) == null) {
            throw new Exception("File '" + fileName + "' does not exist");
        }
    }

    /**
     * Method creating a connection to the webservice using the information
     * stored in the property files. If a connection already is established this
     * connection will be used
     *
     * @throws ServiceException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public EjbcaWS getEjbcaRAWS()  {
        try {
            return getEjbcaRAWS(false);
        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Method creating a connection to the webservice using the information
     * stored in the property files. A new connection will be created for each
     * call.
     *
     * @throws ServiceException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private EjbcaWS getEjbcaRAWSFNewReference() throws Exception {
        return getEjbcaRAWS(true);
    }

    private EjbcaWS getEjbcaRAWS(boolean bForceNewReference) throws Exception {
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.ejbcaraws == null || bForceNewReference) {
            final QName qname = new QName("http://ws.protocol.core.ejbca.org/", "EjbcaWSService");
            final EjbcaWSService service = new EjbcaWSService(this.webServiceURL, qname);
            if (bForceNewReference) {
                return service.getEjbcaWSPort();
            }
            this.ejbcaraws = service.getEjbcaWSPort();
        }
        return this.ejbcaraws;
    }

    public int getRevokeReason(String reason)  {
        for (int i = 0; i < REASON_TEXTS.length; i++) {
            if (REASON_TEXTS[i].equalsIgnoreCase(reason)) {
                return REASON_VALUES[i];
            }
        }
        return 0;
    }

    public String getRevokeReason(int reason) {
        for (int i = 0; i < REASON_VALUES.length; i++) {
            if (REASON_VALUES[i] == reason) {
                return REASON_TEXTS[i];
            }
        }
        return null;
    }

}
