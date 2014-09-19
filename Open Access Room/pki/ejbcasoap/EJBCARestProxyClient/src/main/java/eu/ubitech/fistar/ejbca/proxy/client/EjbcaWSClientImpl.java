/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbca.proxy.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.util.CertTools;
import org.ejbca.core.EjbcaException;
import org.ejbca.core.model.ra.raadmin.EndEntityProfileNotFoundException;
import org.ejbca.core.protocol.ws.client.gen.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.CesecoreException_Exception;
import org.ejbca.core.protocol.ws.client.gen.DateNotValidException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.EndEntityProfileNotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.ExtendedInformationWS;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.KeyStore;
import org.ejbca.core.protocol.ws.client.gen.NameAndId;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.RevokeBackDateNotAllowedForProfileException_Exception;
import org.ejbca.core.protocol.ws.client.gen.RevokeStatus;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.ejbca.ui.cli.ErrorAdminCommandException;
import org.ejbca.util.query.IllegalQueryException;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 *
 */
public class EjbcaWSClientImpl {

    private final Logger logger = Logger.getLogger(EjbcaWSClientImpl.class.getName());

    private enum Encode {

        BASE64, BINARY
    };

    private enum Extension {

        P12, PEM, DER, JKS
    };

    /**
     * Edits/adds a user to the EJBCA database.
     *
     * @param ejbcaUser An EjbcaUser Object which contains all info for the user
     * to be edited/created
     * @return The generated KeyStore or null if no KeyStore could be created
     *
     */
    public boolean editUser(EjbcaUser ejbcaUser) {
        EjbcaWSLogger wslogger = new EjbcaWSLogger();
        try {
            final UserDataVOWS userdata = convertEjbcaUserTOUserDataVOWS(ejbcaUser);
            wslogger.append("Trying to add user:");
            wslogger.append("Username: " + userdata.getUsername());
            wslogger.append("Subject DN: " + userdata.getSubjectDN());
            wslogger.append("Subject Altname: " + userdata.getSubjectAltName());
            wslogger.append("Email: " + userdata.getEmail());
            wslogger.append("CA Name: " + userdata.getCaName());
            wslogger.append("Token: " + userdata.getTokenType());
            wslogger.append("Status: " + getStatus(userdata.getStatus()));
            wslogger.append("End entity profile: " + userdata.getEndEntityProfileName());
            wslogger.append("Certificate profile: " + userdata.getCertificateProfileName());
            wslogger.append("Hard Token Issuer Alias: " + (userdata.getHardTokenIssuerName() != null ? userdata.getHardTokenIssuerName() : "null"));

            final List<ExtendedInformationWS> eil = userdata.getExtendedInformation();
            if (eil != null) {
                wslogger.append("Extended information:");
                for (ExtendedInformationWS ei : eil) {
                    wslogger.append("	'" + ei.getName() + "' = '" + ei.getValue() + "'");
                }
            }

            final BigInteger bi = userdata.getCertificateSerialNumber();
            if (bi != null) {
                wslogger.append("CERTIFICATESERIALNUMBER" + "=0x" + bi.toString(16));
            }
            //Add/Edit End-Entity to EJBCA
            getEjbcaRAWS().editUser(userdata);
            wslogger.append("User '" + userdata.getUsername() + "' has been added/edited.");
            wslogger.showLogs(this.logger);
            return true;

        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        wslogger.showLogs(this.logger);
        return false;
    }

    public KeyStore createSoftTokenRequest(EjbcaUser ejbcaUser) {
        KeyStore keystore = null;
        try {
            EjbcaWSLogger wslogger = new EjbcaWSLogger();
            UserDataVOWS userdata = convertEjbcaUserTOUserDataVOWS(ejbcaUser);

            keystore = getEjbcaRAWS().softTokenRequest(userdata, userdata.getHardTokenIssuerName(), ejbcaUser.getEntityArgument(EjbcaUser.Arguments.KEYLENGTH), ejbcaUser.getEntityArgument(EjbcaUser.Arguments.ENCRYPTION_ALGORYTHM));

            if (keystore == null) {
                wslogger.append("No certificate could be generated for user, check server logs for error.");
            }

        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return keystore;
    }

    /**
     * Fetches issued certificate.
     *
     * Authorization requirements:
     * <pre>
     * - A valid certificate
     * - /ca_functionality/view_certificate
     * - /ca/&lt;of the issing CA&gt;
     * </pre>
     *
     * @param certSNinHex the certificate serial number in hexadecimal
     * representation
     * @param issuerDN the issuer of the certificate
     * @return the certificate (in WS representation) or null if certificate
     * couldn't be found.
     */
    public Certificate getCertificate(String certSNinHex, String issuerDN) {
        EjbcaWSLogger wslogger = new EjbcaWSLogger();
        Certificate certificate = null;
        try {
            certificate = getEjbcaRAWS().getCertificate(certSNinHex, issuerDN);
            if (certificate == null) {
                wslogger.append("Certificate with SerialNumber: " + certSNinHex + " and IssuerDN: " + issuerDN + " could not be found!");
            } else {
                wslogger.append("Certificate with SerialNumber: " + certSNinHex + " and IssuerDN: " + issuerDN + " was fetched!");
            }
        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CADoesntExistsException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        wslogger.showLogs(this.logger);
        return certificate;
    }

    /**
     * Returns revocation status for given user.
     *
     * @param issuerDN The distinguish name of the issuer CA
     * @param certSN The serial number of the certificate to check the
     * revocation status
     * @return RevokeStatus Object
     */
    public RevokeStatus checkCertificateRevokeStatus(String issuerDN, String certSN) {
        RevokeStatus status = null;
        EjbcaWSLogger wslogger = new EjbcaWSLogger();

        try {
            issuerDN = CertTools.stringToBCDNString(issuerDN);
            certSN = getCertSN(certSN);

            status = getEjbcaRAWS().checkRevokationStatus(issuerDN, certSN);

            if (status == null) {
                wslogger.append("Error, No certificate found in database.");
            } else {
                wslogger.append("Revocation status :");
                wslogger.append("  IssuerDN      : " + status.getIssuerDN());
                wslogger.append("  CertificateSN : " + status.getCertificateSN());
                if (status.getReason() == RevokedCertInfo.NOT_REVOKED) {
                    wslogger.append("  Status        : NOT REVOKED");
                } else {
                    wslogger.append("  Status        : REVOKED");
                    wslogger.append("  Reason        : " + EjbcaWSClient.INSTANCE.getRevokeReason(status.getReason()));
                    wslogger.append("  Date          : " + status.getRevocationDate().toString());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        wslogger.showLogs(this.logger);
        return status;

    }

    /**
     * Revokes a user certificate.
     *
     * @param issuerDN of the certificate to revoke
     * @param certificateSN of the certificate to revoke
     * @param reason for revocation, one of
     * {@link org.ejbca.core.protocol.ws.client.gen.RevokeStatus}.REVOKATION_REASON_
     * constants, or use
     * {@link org.ejbca.core.protocol.ws.client.gen.RevokeStatus}.NOT_REVOKED to
     * un-revoke a certificate on hold.
     * @param sDate The revocation date. If null then the current date is used.
     * If specified then the profile of the certificate must allow "back dating"
     * and the date must be i the past. The parameter is specified as an
     * <a href="http://en.wikipedia.org/wiki/ISO8601">ISO 8601 string</a>. An
     * example: 2012-06-07T23:55:59+02:00
     */
    //TODO: Implement a return value solution
    public boolean revokeCertBackdated(String issuerDN, String certificateSN, int reason, String sDate) {
        boolean isRevoked = false;
        EjbcaWSLogger wslogger = new EjbcaWSLogger();
        try {
            final String issuerdn = CertTools.stringToBCDNString(issuerDN);
            final String certsn = getCertSN(certificateSN);
            final String justRevoke = "To revoke the certificate with the current time remove the last argument (revocation time).";
            try {
                final RevokeStatus status = getEjbcaRAWS().checkRevokationStatus(issuerdn, certsn);
                if (status != null) {
                    getEjbcaRAWS().revokeCertBackdated(issuerdn, certsn, reason, sDate);
                    wslogger.append("Certificate revoked (or unrevoked) successfully.");
                    isRevoked = true;
                } else {
                    wslogger.append("Certificate does not exist.");
                }
            } catch (AuthorizationDeniedException_Exception e) {
                wslogger.append("Error : " + e.getMessage());
            } catch (AlreadyRevokedException_Exception e) {
                wslogger.append("The certificate was already revoked, or you tried to unrevoke a permanently revoked certificate.");
            } catch (WaitingForApprovalException_Exception e) {
                wslogger.append("The revocation request has been sent for approval.");
            } catch (ApprovalException_Exception e) {
                wslogger.append("This revocation has already been requested.");
            } catch (DateNotValidException_Exception e) {
                wslogger.append(e.getMessage());
                wslogger.append(justRevoke);
            } catch (RevokeBackDateNotAllowedForProfileException_Exception e) {
                wslogger.append(e.getMessage());
                wslogger.append(justRevoke);
            }
        } catch (CADoesntExistsException_Exception e) {
            try {
                throw new ErrorAdminCommandException(e);
            } catch (ErrorAdminCommandException ex) {
                Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (EjbcaException_Exception e) {
            try {
                throw new ErrorAdminCommandException(e);
            } catch (ErrorAdminCommandException ex) {
                Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NotFoundException_Exception e) {
            try {
                throw new ErrorAdminCommandException(e);
            } catch (ErrorAdminCommandException ex) {
                Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        wslogger.showLogs(this.logger);
        return isRevoked;
    }

    /**
     * Same as {@link #revokeCertBackdated(String, String, int, String)} but
     * revocation date is current time.
     *
     * @param issuerDN
     * @param certificateSN
     * @param reason
     */
    //TODO: Implement a return value solution
    public boolean revokeUserCert(String issuerDN, String certificateSN, int reason) {
        return revokeCertBackdated(issuerDN, certificateSN, reason, null);

    }

    /**
     * Generates a CRL for the given CA.
     *
     * Authorization requirements:
     * <pre>
     * - /ca/&lt;caid&gt;
     * </pre>
     *
     * @param caname the name in EJBCA of the CA that should have a new CRL
     * generated
     * @exception CADoesntExistsException if a referenced CA does not exist
     * @exception ApprovalException
     * @exception EjbcaException if an error occured, for example authorization
     * denied
     * @exception ApprovalRequestExpiredException
     * @exception CAOfflineException
     * @exception CryptoTokenOfflineException
     */
    public boolean createCRL(String caname) {
        try {
            getEjbcaRAWS().createCRL(caname);
            logger.log(Level.INFO, "CRL for CA: {0} was created successfuly!", caname);
        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * Retrieves the latest CRL issued by the given CA.
     *
     * Authorization requirements:
     * <pre>
     * - /ca/&lt;caid&gt;
     * </pre>
     *
     * @param caname the name in EJBCA of the CA that issued the desired CRL
     * @param deltaCRL false to fetch a full CRL, true to fetch a deltaCRL (if
     * issued)
     * @return the latest CRL issued for the CA as a DER encoded byte array
     * @throws CADoesntExistsException if a referenced CA does not exist
     * @throws EjbcaException if an error occured, for example authorization
     * denied
     */
    public X509CRL getLatestCRL(String caname, boolean deltaCRL) {
        byte[] crlraw = null;
        X509CRL crl = null;

        try {
            crlraw = getEjbcaRAWS().getLatestCRL(caname, deltaCRL);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlraw));
            logger.log(Level.INFO, "Success retrieve CRL list issued by CA: {0}", caname);
        } catch (CADoesntExistsException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CRLException ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return crl;
    }

    /**
     * Retrieves information about users in the database.
     *
     * @param usermatch the unique user pattern to search for
     * @return a array of
     * {@link org.ejbca.core.protocol.ws.client.gen.UserDataVOWS} objects (Max
     * 100) containing the information about the user or null if there are no
     * matches.
     * @exception AuthorizationDeniedException if client isn't authorized to
     * request
     * @exception IllegalQueryException if query isn't valid
     * @exception EjbcaException
     * @exception EndEntityProfileNotFoundException
     * @exception CesecoreException
     */
    public List<UserDataVOWS> findUser(UserMatch usermatch) {
        List<UserDataVOWS> users = null;
        try {
            //UserMatch(int matchwith, int matchtype, java.lang.String matchvalue)
            users = getEjbcaRAWS().findUser(usermatch);
            logger.log(Level.INFO, "Total {0} Users found matching the given criteria!", users.size());

        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EndEntityProfileNotFoundException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalQueryException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return users;
    }

    /**
     * Fetch a list of the ids and names of available CAs.
     *
     * Note: available means not having status "external" or "waiting for
     * certificate response".
     *
     * @return array of NameAndId of available CAs, if no CAs are found will an
     * empty array be returned of size 0, never null.
     * @throws EjbcaException if an error occured
     * @throws AuthorizationDeniedException
     */
    public List<NameAndId> getAvailableCAs() {
        List<NameAndId> availableCAs = null;
        try {
            availableCAs = getEjbcaRAWS().getAvailableCAs();
            logger.log(Level.INFO, "Total {0} CAs found in EJBCA database!", availableCAs.size());
        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return availableCAs;
    }

    /**
     * Fetches the end-entity profiles that the administrator is authorized to
     * use.
     *
     * @return array of NameAndId of available end entity profiles, if no
     * profiles are found will an empty array be returned of size 0, never null.
     * @exception EjbcaException if an error occured
     * @exception AuthorizationDeniedException
     */
    public List<NameAndId> getAuthorizedEndEntityProfiles() {
        List<NameAndId> endentityProfiles = null;
        try {
            endentityProfiles = getEjbcaRAWS().getAuthorizedEndEntityProfiles();
            logger.log(Level.INFO, "Total {0} Authorized End-Entity Profiles found in EJBCA database!", endentityProfiles.size());
        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return endentityProfiles;

    }

    public List<Certificate> getCACert(String name) {
        List<Certificate> certsList = null;
        try {
            certsList = getEjbcaRAWS().getLastCAChain(name);

        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CADoesntExistsException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return certsList;
    }

    /**
     * Fetches available certificate profiles in an end entity profile.
     *
     * @param entityProfileId id of an end entity profile where we want to find
     * which certificate profiles are available
     * @return array of NameAndId of available certificate profiles, if no
     * profiles are found will an empty array be returned of size 0, never null.
     * @exception EjbcaException if an error occured
     * @exception AuthorizationDeniedException
     */
    public List<NameAndId> getAvailableCertificateProfiles(int entityProfileId) {
        List<NameAndId> certificateProfiles = null;
        try {
            certificateProfiles = getEjbcaRAWS().getAvailableCertificateProfiles(entityProfileId);
            logger.log(Level.INFO, "Total {0} available certificate profiles for end-entity profile with ID: {1}", new Object[]{certificateProfiles.size(), String.valueOf(entityProfileId)});
        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return certificateProfiles;
    }

    public CertificateResponse crmfRequest(String username, String password, String crmf, String hardToeknSN, String responseType) {
        CertificateResponse certificateResponse = null;
        try {
            //getEjbcaRAWS().crmfRequest(username, password, crmf, hardToeknSN, responseType);
            certificateResponse = getEjbcaRAWS().pkcs10Request(username, password, crmf, hardToeknSN, responseType);
        } catch (AuthorizationDeniedException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CADoesntExistsException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CesecoreException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EjbcaException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotFoundException_Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return certificateResponse;
    }

    // -- Help Functions --
    private String getCertSN(String certsn) {
        try {
            new BigInteger(certsn, 16);
        } catch (NumberFormatException e) {
            logger.severe("Error in Certificate SN\nCommand used check the status of certificate \n Usage : checkrevocationstatus <issuerdn> <certificatesn (HEX)>  \n\n");
        }
        return certsn;
    }

    private String getHardTokenSN(String hardtokensn) {
        if (hardtokensn.equalsIgnoreCase("NONE")) {
            return null;
        }

        return hardtokensn;
    }

    //Stroes a KeyStore to the filesystem
    public boolean storeKeystore(KeyStore keystore, String type, String encoding, String pathname, String filename) {
        boolean isStoreSuccess = false;
        if (keystore != null) {
            pathname = (pathname == null || pathname.isEmpty() ? System.getProperty("user.dir") : pathname);
            //Check if a valid path and a certificate encoding is given
            if (this.isValidPath(pathname) && isValidEncoding(encoding) && isValidKeystoreType(type)) {

                if (encoding.equalsIgnoreCase(Encode.BASE64.toString())) {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + "." + type.toUpperCase()), keystore.getKeystoreData());
                } else {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + "." + type.toUpperCase()), keystore.getRawKeystoreData());
                }
            }
        }

        if (isStoreSuccess) {
            logger.log(Level.INFO, "KeyStore successfully stored to disk at filepath: {0}", getfilepath(pathname, filename + "." + type.toUpperCase()) + " in " + encoding.toLowerCase() + " format");
        } else {
            logger.log(Level.SEVERE, "Error storing KeyStore to disk...{0}", (keystore == null ? " Input KeyStore is null!" : ""));
        }
        return isStoreSuccess;
    }

    //Stores a CRL list to the filesystem
    public boolean storeCRL(X509CRL crl, String pathname, String filename) {
        boolean isStoreSuccess = false;
        if (crl != null) {
            pathname = (pathname == null || pathname.isEmpty() ? System.getProperty("user.dir") : pathname);
            //Check if a valid path is given
            if (this.isValidPath(pathname)) {
                try {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + ".crl"), crl.getEncoded());
                } catch (CRLException ex) {
                    Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (isStoreSuccess) {
            logger.log(Level.SEVERE, "CRL successfully stored to disk at filepath: {0}", getfilepath(pathname, filename + ".crl"));
        } else {
            logger.severe("Error storing CRL to disk...");
        }
        return isStoreSuccess;
    }

    //Stores a CRL list to the filesystem
    public boolean storeX509Certificate(X509Certificate cert, String pathname, String filename) {
        boolean isStoreSuccess = false;
        if (cert != null) {
            pathname = (pathname == null || pathname.isEmpty() ? System.getProperty("user.dir") : pathname);
            //Check if a valid path is given
            if (this.isValidPath(pathname)) {
                try {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + ".crl"), cert.getEncoded());
                } catch (CertificateException ex) {
                    Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (isStoreSuccess) {
            logger.log(Level.SEVERE, "CRL successfully stored to disk at filepath: {0}", getfilepath(pathname, filename + ".crl"));
        } else {
            logger.severe("Error storing CRL to disk...");
        }
        return isStoreSuccess;
    }

    public UserDataVOWS convertEjbcaUserTOUserDataVOWS(EjbcaUser ejbcaUser) {
        UserDataVOWS userdata = new UserDataVOWS();
        final String encoding = ejbcaUser.getEntityArgument(EjbcaUser.Arguments.ENCODING);
        final String hardtokensn = ejbcaUser.getEntityArgument(EjbcaUser.Arguments.HARDTOKENSN);
        final String outputPath = ejbcaUser.getEntityArgument(EjbcaUser.Arguments.OUTPUTPATH).length() > 0 ? ejbcaUser.getEntityArgument(EjbcaUser.Arguments.OUTPUTPATH) : null;

        userdata.setUsername(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.USERNAME));
        userdata.setPassword(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.PASSWORD));
        userdata.setClearPwd(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.CLEARPWD).equalsIgnoreCase("true"));
        userdata.setSubjectDN(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.SUBJECTDN));

        if (!ejbcaUser.getEntityArgument(EjbcaUser.Arguments.HARDTOKENSN).equalsIgnoreCase("NULL")) {
            userdata.setHardTokenIssuerName(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.HARDTOKENSN));
        }

        if (!ejbcaUser.getEntityArgument(EjbcaUser.Arguments.SUBJECTALTNAME).equalsIgnoreCase("NULL")) {
            userdata.setSubjectAltName(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.SUBJECTALTNAME));
        }

        if (!ejbcaUser.getEntityArgument(EjbcaUser.Arguments.EMAIL).equalsIgnoreCase("NULL")) {
            userdata.setEmail(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.EMAIL));
        }

        userdata.setCaName(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.CA));
        userdata.setTokenType(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.TOKEN));
        userdata.setStatus(Integer.parseInt(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.STATUS)));
        userdata.setEndEntityProfileName(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.ENDENTITYPROFILE));
        userdata.setCertificateProfileName(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.CERTIFICATEPROFILE));

        if (!ejbcaUser.getEntityArgument(EjbcaUser.Arguments.ISSUERALIAS).equalsIgnoreCase("NONE")) {
            userdata.setEmail(ejbcaUser.getEntityArgument(EjbcaUser.Arguments.ISSUERALIAS));
        }
        return userdata;
    }

    //Stroes a Certificate to the filesystem
    public boolean storeCertificate(Certificate certificate, String encoding, String pathname, String filename) {
        boolean isStoreSuccess = false;
        if (certificate != null) {
            pathname = (pathname == null || pathname.isEmpty() ? System.getProperty("user.dir") : pathname);
            //Check if a valid path and a certificate encoding is given
            if (this.isValidPath(pathname) && isValidEncoding(encoding)) {

                if (encoding.equalsIgnoreCase(Encode.BASE64.toString())) {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + "." + Extension.PEM), certificate.getCertificateData());
                } else {
                    isStoreSuccess = this.writeTOdisk(getfilepath(pathname, filename + "." + Extension.DER), certificate.getRawCertificateData());
                }
            }
        }
        if (isStoreSuccess) {
            logger.log(Level.SEVERE, "Certificate successfully stored to disk at filepath: {0}", getfilepath(pathname, filename + "." + (encoding.equalsIgnoreCase(Encode.BASE64.toString()) ? Extension.PEM : Extension.DER)));
        } else {
            logger.severe("Error storing certificate to disk...");
        }
        return isStoreSuccess;
    }

    private boolean isValidKeystoreType(String type) {
        for (Enum extension : Extension.values()) {
            if (extension.toString().equalsIgnoreCase(type)) {
                return true;
            }
        }
        logger.severe("ERROR: You must specify a valid KeyStroe type (PEM/P12/JKS)...");
        return false;
    }

    private boolean isValidEncoding(String encoding) {
        for (Enum encode : Encode.values()) {
            if (encode.toString().equalsIgnoreCase(encoding)) {
                return true;
            }
        }
        logger.severe("ERROR: You must specify a valid Encoding format (BASE64/BINARY)...");
        return false;
    }

    private String getStatus(int status) {

        switch (status) {

            case (UserDataVOWS.STATUS_NEW): {
                return "NEW";
            }
            case (UserDataVOWS.STATUS_FAILED): {
                return "FAILED";
            }
            case (UserDataVOWS.STATUS_INITIALIZED): {
                return "INITIALIZED";
            }
            case (UserDataVOWS.STATUS_INPROCESS): {
                return "INPROCESS";
            }
            case (UserDataVOWS.STATUS_GENERATED): {
                return "GENERATED";
            }
            case (UserDataVOWS.STATUS_REVOKED): {
                return "REVOKED";
            }
            case (UserDataVOWS.STATUS_HISTORICAL): {
                return "HISTORICAL";
            }
            case (UserDataVOWS.STATUS_KEYRECOVERY): {
                return "KEYRECOVERY";
            }
        }

        logger.log(Level.SEVERE, "Error in status string : {0}", status);
        System.exit(-1);
        return "";
    }

    private String getPKCS10(String pkcs10Path) {
        String retval = null;
        try {
            FileInputStream fis = new FileInputStream(pkcs10Path);
            byte[] contents = new byte[fis.available()];
            fis.read(contents);
            fis.close();
            retval = new String(contents);
        } catch (FileNotFoundException e) {
            logger.severe("Error : PKCS10 file couln't be found.");
            System.exit(-1); // NOPMD, it's not a JEE app		
        } catch (IOException e) {
            logger.severe("Error reading content of PKCS10 file.");
            System.exit(-1); // NOPMD, it's not a JEE app	
        }

        return retval;
    }

    //Checks if the give path exists and is valid
    private boolean isValidPath(String outputpath) {
        File dir = new File(outputpath);
        if (!dir.exists()) {
            logger.severe("Error : Output directory doesn't seem to exist.");
            return false;
        }
        if (!dir.isDirectory()) {
            logger.severe("Error : Output directory doesn't seem to be a directory.");
            return false;
        }
        if (!dir.canWrite()) {
            logger.severe("Error : Output directory isn't writeable.");
            return false;

        }
        return true;
    }

    //Get an EjbcaWS Object Instance
    public static EjbcaWS getEjbcaRAWS() {

        try {
            return EjbcaWSClient.INSTANCE.getEjbcaRAWS();
        } catch (Exception ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getfilepath(String pathname, String filename) {
        return pathname + "/" + filename;
    }

    /**
     * Writes a stream of bytes to the filesystem
     *
     * @param filepath The filepath in filesystem which the bytes will be stored
     * @param bytestream An array of bytes
     * @return True is the write was success or false if an error occurred
     */
    public boolean writeTOdisk(String filepath, byte[] bytestream) {
        boolean isWriteSuccess = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            fos.write(bytestream);
            fos.close();
            isWriteSuccess = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(EjbcaWSClientImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return isWriteSuccess;
    }

    private class EjbcaWSLogger {

        private final String LINE_SEPERATOR = System.getProperty("line.separator");
        private final StringBuilder logs = new StringBuilder();

        public void append(String log) {
            logs.append(log);
            logs.append(LINE_SEPERATOR);
        }

        public void showLogs(Logger logger) {
            logger.info(logs.toString());
        }
    }
}
