/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.cesecore.certificates.ca.catoken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cesecore.internal.InternalResources;
import org.cesecore.internal.UpgradeableDataHashMap;
import org.cesecore.keys.token.CryptoToken;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.util.StringTools;

/**
 * The CAToken is keeps references to the CA's key aliases and the CryptoToken where the keys are stored.
 * 
 * The signing key can have 3 stages:
 * - Next:     Can become the new current CA key when a valid signing certificate is present
 * - Current:  Is used to issue certificates and has a CA certificate
 * - Previous: The signing key before the latest CA renewal.
 * 
 * Each CA signing key "generation" has a corresponding key sequence number that is kept track of
 * via this class. The key sequence also have the states next, current and previous.
 * 
 * The CA token stores a reference (an integer) to the CryptoToken where the CA keys are stored.
 * 
 * @version $Id: CAToken.java 17786 2013-10-10 10:51:47Z jeklund $
 */
public class CAToken extends UpgradeableDataHashMap {

    private static final long serialVersionUID = -459748276141898509L;

    /** Log4j instance */
    private static final Logger log = Logger.getLogger(CAToken.class);
    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    /** Latest version of the UpgradeableHashMap, this determines if we need to auto-upgrade any data. */
    public static final float LATEST_VERSION = 8;

    @Deprecated // Used by upgrade code
    public static final String CLASSPATH = "classpath";
    public static final String PROPERTYDATA = "propertydata";
    @Deprecated // Used by upgrade code
    public static final String KEYSTORE = "KEYSTORE";

    // The Initial sequence number is 00000-99999 or starts at 00001 according to generated doc 2012-12-03.
    public static final String DEFAULT_KEYSEQUENCE = "00000";

    public static final String SOFTPRIVATESIGNKEYALIAS = "privatesignkeyalias";
    public static final String SOFTPREVIOUSPRIVATESIGNKEYALIAS = "previousprivatesignkeyalias";
    public static final String SOFTNEXTPRIVATESIGNKEYALIAS = "nextprivatesignkeyalias";
    public static final String SOFTPRIVATEDECKEYALIAS = "privatedeckeyalias";

    /** A sequence for the keys, updated when keys are re-generated */
    public static final String SEQUENCE = "sequence";
    /** Format of the key sequence, the value for this property is one of StringTools.KEY_SEQUENCE_FORMAT_XX */
    public static final String SEQUENCE_FORMAT = "sequenceformat";
    public static final String SIGNATUREALGORITHM = "signaturealgorithm";
    public static final String ENCRYPTIONALGORITHM = "encryptionalgorithm";
    public static final String CRYPTOTOKENID = "cryptotokenid";

    private int cryptoTokenId;
    private transient PurposeMapping keyStrings = null;

    public CAToken(final int cryptoTokenId, final Properties caTokenProperties) {
        super();
        this.cryptoTokenId = cryptoTokenId;
        data.put(CAToken.CRYPTOTOKENID, String.valueOf(cryptoTokenId));
        internalInit(caTokenProperties);
    }

	/** Common code to initialize object called from all constructors. */
	private void internalInit(Properties caTokenProperties) {
        this.keyStrings = new PurposeMapping(caTokenProperties);
        setCATokenPropertyData(storeProperties(caTokenProperties));
	}

    /** Constructor used to initialize a stored CA token, when the UpgradeableHashMap has been stored as is.
     * 
     * @param data LinkedHashMap
     */
    @SuppressWarnings("rawtypes")
    public CAToken(final HashMap tokendata) {
		loadData(tokendata);
		final Object cryptoTokenIdObject = data.get(CAToken.CRYPTOTOKENID);
		if (cryptoTokenIdObject==null) {
		    log.warn("No CryptoTokenId in CAToken map. This can safely be ignored if shown during an upgrade from EJBCA 5.0.x or lower.");
		} else {
            this.cryptoTokenId = Integer.parseInt((String) cryptoTokenIdObject);
		}
        final Properties caTokenProperties = getProperties();
        internalInit(caTokenProperties);
    }
    
    /** Verifies that the all the mapped keys are present in the CryptoToken and optionally that the test key is usable. */
    public int getTokenStatus(boolean caTokenSignTest, CryptoToken cryptoToken) {
        if (log.isTraceEnabled()) {
            log.trace(">getCATokenStatus");
        }
        int ret = CryptoToken.STATUS_OFFLINE;
        // If we have no key aliases, no point in continuing...
        try {
        	if (this.keyStrings != null) {
        		String aliases[] = this.keyStrings.getAliases();
        		int i = 0;
        		// Loop that checks  if there all key aliases have keys
        		while (aliases != null && i < aliases.length) {
        			if (cryptoToken != null && cryptoToken.getKey(aliases[i]) != null) {
        				i++;
        			} else {
        				if (log.isDebugEnabled()) {
        					log.debug("Missing key for alias: "+aliases[i]);
        				}
        			}
        		}
        		// If we don't have any keys for the strings, or we don't have enough keys for the strings, no point in continuing...
        		if (aliases != null && i >= aliases.length) {
        		    final String aliasTestKey = getAliasFromPurpose(CATokenConstants.CAKEYPURPOSE_KEYTEST);
        			PrivateKey privateKey;
        			PublicKey publicKey;
        			try {
        				privateKey = cryptoToken.getPrivateKey(aliasTestKey);
        				publicKey = cryptoToken.getPublicKey(aliasTestKey);
        			} catch (CryptoTokenOfflineException e) {
        				privateKey = null;
        				publicKey = null;
        				if (log.isDebugEnabled()) {
        					log.debug("no test key defined");
        				}
        			}
        			if (privateKey != null && publicKey != null) {
        				// Check that that the testkey is usable by doing a test signature.
        				try {
        				    if (caTokenSignTest) {
                                cryptoToken.testKeyPair(aliasTestKey);
        				    }
        					// If we can test the testkey, we are finally active!
        					ret = CryptoToken.STATUS_ACTIVE;
        				} catch (Throwable th) { // NOPMD: we need to catch _everything_ when dealing with HSMs
        					log.error(intres.getLocalizedMessage("token.activationtestfail", cryptoToken.getId()), th);
        				}
        			}
        		} else {
        			if (log.isDebugEnabled()) {
        				StringBuilder builder = new StringBuilder();
        				for (int j = 0; j < aliases.length; j++) {
        					builder.append(' ').append(aliases[j]);
        				}
        				log.debug("Not enough keys for the key aliases: "+builder.toString());
        			}
        		}
        	}
        } catch (CryptoTokenOfflineException e) {
        	if (log.isDebugEnabled()) {
        		log.debug("CryptoToken offline: "+e.getMessage());
        	}
        }

        if (log.isTraceEnabled()) {
        	log.trace("<getCATokenStatus: " + ret);
        }
        return ret;
    }

    /** @return the key pair alias in the CryptoToken from the CATokenConstants.CAKEYPURPOSE_.. */
    public String getAliasFromPurpose(final int purpose) throws CryptoTokenOfflineException {
        if (keyStrings==null) {
            // keyStrings is transient and can be null after serialization
            keyStrings = new PurposeMapping(getProperties());
        }
        final String alias = keyStrings.getAlias(purpose);
        if (alias == null) {
            throw new CryptoTokenOfflineException("No alias for key purpose " + purpose);
        }
        return alias;
    }

    /** @return the reference to the CA's CryptoToken */
    public int getCryptoTokenId() {
        return cryptoTokenId;
    }

    /** Set a property and update underlying Map */
    public void setProperty(String key, String value) {
        final Properties caTokenProperties = getProperties();
        caTokenProperties.setProperty(key, value);
        setCATokenPropertyData(storeProperties(caTokenProperties));
    }

    /**
     * Internal method just to get rid of the always present date that is part of the standard Properties.store().
     * 
     * @param prop
     * @return String that can be loaded by Properties.load
     */
    private String storeProperties(Properties caTokenProperties) {
        this.keyStrings = new PurposeMapping(caTokenProperties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        Enumeration<Object> e = caTokenProperties.keys();
        while (e.hasMoreElements()) {
            Object s = e.nextElement();
            if (caTokenProperties.get(s) != null) {
                writer.println(s + "=" + caTokenProperties.get(s));
            }
        }
        writer.close();
        return baos.toString();
    }

    /** Sets the propertydata used to configure this CA Token. */
    private void setCATokenPropertyData(String propertydata) {
        data.put(CAToken.PROPERTYDATA, propertydata);
    }

    public Properties getProperties() {
        String propertyStr = null;
        if (data != null) {
            propertyStr = (String) data.get(CAToken.PROPERTYDATA);
        }
        return getPropertiesFromString(propertyStr);
    }
    
    public static Properties getPropertiesFromString(String propertyStr) {
        final Properties prop = new Properties();
        if (StringUtils.isNotEmpty(propertyStr)) {
            try {
				// If the input string contains \ (backslash on windows) we must convert it to \\
				// Otherwise properties.load will parse it as an escaped character, and that is not good
				propertyStr = StringUtils.replace(propertyStr, "\\", "\\\\");
                prop.load(new ByteArrayInputStream(propertyStr.getBytes()));
                // Trim whitespace in values
                for (Object keyObj : prop.keySet()) {
                    String key = (String)keyObj;
                    String value = prop.getProperty(key);
                    prop.setProperty(key, value.trim());
                }
            } catch (IOException e) {
                log.error("Error getting PCKS#11 token properties: ", e);
            }
        }
        return prop;
    }

    /** Returns the Sequence, that is a sequence that is updated when keys are re-generated */
    public String getKeySequence() {
        Object seq = data.get(SEQUENCE);
        if (seq == null) {
            seq = new String(CAToken.DEFAULT_KEYSEQUENCE);
        }
        return (String) seq;
    }

    /** Sets the key sequence */
    public void setKeySequence(String sequence) {
        data.put(SEQUENCE, sequence);
    }

    /** Sets the SequenceFormat */
    public void setKeySequenceFormat(int sequence) {
        data.put(SEQUENCE_FORMAT, sequence);
    }

    /** Returns the Sequence format, that is the format of the key sequence */
    public int getKeySequenceFormat() {
        Object seqF = data.get(SEQUENCE_FORMAT);
        if (seqF == null) {
            seqF = Integer.valueOf(StringTools.KEY_SEQUENCE_FORMAT_NUMERIC);
        }
        return (Integer) seqF;
    }

    /** Returns the SignatureAlgoritm */
    public String getSignatureAlgorithm() {
        return (String) data.get(CAToken.SIGNATUREALGORITHM);
    }

    /** Sets the SignatureAlgoritm */
    public void setSignatureAlgorithm(String signaturealgoritm) {
        data.put(CAToken.SIGNATUREALGORITHM, signaturealgoritm);
    }

    /** Returns the EncryptionAlgoritm */
    public String getEncryptionAlgorithm() {
        return (String) data.get(CAToken.ENCRYPTIONALGORITHM);
    }

    /** Sets the SignatureAlgoritm */
    public void setEncryptionAlgorithm(String encryptionalgo) {
        data.put(CAToken.ENCRYPTIONALGORITHM, encryptionalgo);
    }

    /** @see org.cesecore.internal.UpgradeableDataHashMap#getLatestVersion() */
    @Override
    public float getLatestVersion() {
        return LATEST_VERSION;
    }

    /** @see org.cesecore.internal.UpgradeableDataHashMap#upgrade() */
    @Override
    public void upgrade() {
        if (Float.compare(LATEST_VERSION, getVersion()) != 0) {
            // New version of the class, upgrade
            String msg = intres.getLocalizedMessage("token.upgrade", new Float(getVersion()));
            log.info(msg);
            // Put upgrade stuff here
            if (data.get(CAToken.SEQUENCE_FORMAT) == null) { // v7
                log.info("Adding new sequence format to CA Token data: " + StringTools.KEY_SEQUENCE_FORMAT_NUMERIC);
                data.put(CAToken.SEQUENCE_FORMAT, StringTools.KEY_SEQUENCE_FORMAT_NUMERIC);
            }
            if (data.get(CAToken.SEQUENCE) == null) { // v7
                log.info("Adding new default key sequence to CA Token data: " + CAToken.DEFAULT_KEYSEQUENCE);
                data.put(CAToken.SEQUENCE, CAToken.DEFAULT_KEYSEQUENCE);
            }

            if (data.get(CAToken.CLASSPATH) != null) { // v8 upgrade of classpaths for CESeCore
                final String classpath = (String) data.get(CAToken.CLASSPATH);
                log.info("Upgrading CA token classpath: "+classpath);
                String newclasspath = classpath;
                if (StringUtils.equals(classpath, "org.ejbca.core.model.ca.catoken.SoftCAToken")) {
                	newclasspath = "org.cesecore.keys.token.SoftCryptoToken";
                	// Upgrade properties to set a default key, also for soft crypto tokens
                	Properties prop = getProperties();
                    // A small unfortunate special property that we have to make in order to 
                    // be able to use soft keystores that does not have a specific test or default key
                    if ((prop.getProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING) == null) &&
                    		(prop.getProperty(CATokenConstants.CAKEYPURPOSE_DEFAULT_STRING) == null)) {
                    	log.info("Setting CAKEYPURPOSE_CERTSIGN_STRING and CAKEYPURPOSE_CRLSIGN_STRING to privatesignkeyalias.");
                    	prop.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING, CAToken.SOFTPRIVATESIGNKEYALIAS);
                    	prop.setProperty(CATokenConstants.CAKEYPURPOSE_CRLSIGN_STRING, CAToken.SOFTPRIVATESIGNKEYALIAS);
                    }
                    if ((prop.getProperty(CATokenConstants.CAKEYPURPOSE_DEFAULT_STRING) == null) &&
                    		(prop.getProperty(CATokenConstants.CAKEYPURPOSE_TESTKEY_STRING) == null)) {
                    	log.info("Setting CAKEYPURPOSE_DEFAULT_STRING to privatedeckeyalias.");
                    	prop.setProperty(CATokenConstants.CAKEYPURPOSE_DEFAULT_STRING, CAToken.SOFTPRIVATEDECKEYALIAS);
                    }
                    setCATokenPropertyData(storeProperties(prop)); // Stores property string in "data"
                } else if (StringUtils.equals(classpath, "org.ejbca.core.model.ca.catoken.PKCS11CAToken")) {
                	newclasspath = "org.cesecore.keys.token.PKCS11CryptoToken";
                } else if (StringUtils.equals(classpath, "org.ejbca.core.model.ca.catoken.NullCAToken")) {
                	newclasspath = "org.cesecore.keys.token.NullCryptoToken";
                } else if (StringUtils.equals(classpath, "org.ejbca.core.model.ca.catoken.NFastCAToken")) {
                	log.error("Upgrading of NFastCAToken not supported, you need to convert to using PKCS11CAToken before upgrading.");
                }
                data.put(CAToken.CLASSPATH, newclasspath);
            }

            data.put(VERSION, new Float(LATEST_VERSION));
        }
    }

    /**
     * Use current key sequence to generate and store a "next" key sequence and "next" singing key alias.
     * @return the next sign key alias.
     */
    public String generateNextSignKeyAlias() {
        // Generate a new key sequence
        final String currentKeySequence = getKeySequence();
        final String newKeySequence = StringTools.incrementKeySequence(getKeySequenceFormat(), currentKeySequence);
        if (log.isDebugEnabled()) {
            log.debug("Current key sequence: " + currentKeySequence + "  New key sequence: " + newKeySequence);
        }
        // Generate a key alias based on the new key sequence
        final String currentCertSignKeyLabel = keyStrings.getAlias(CATokenConstants.CAKEYPURPOSE_CERTSIGN);
        final String newCertSignKeyLabel = StringUtils.removeEnd(currentCertSignKeyLabel, currentKeySequence) + newKeySequence;
        if (log.isDebugEnabled()) {
            log.debug("Current sign key alias: " + currentCertSignKeyLabel + "  New sign key alias: " + newCertSignKeyLabel);
        }
        // Store the new values in the properties of this token
        setNextCertSignKey(newCertSignKeyLabel);
        setNextKeySequence(newKeySequence);
        return newCertSignKeyLabel;
    }

    /** Next sign key becomes current. Current becomes previous. Same goes for KeySequence. CRL sign key is updated if it is the same as cert sign key */
    public void activateNextSignKey() {
        final Properties caTokenProperties = getProperties();
        // Replace certificate (and crl) signing key aliases (if present)
        boolean swichedSigningKey = false;
        final String nextCertSignKeyLabel = keyStrings.getAlias(CATokenConstants.CAKEYPURPOSE_CERTSIGN_NEXT);
        if (nextCertSignKeyLabel!=null) {
            final String currentCertSignKeyLabel = keyStrings.getAlias(CATokenConstants.CAKEYPURPOSE_CERTSIGN);
            final String currentCrlSignKeyLabel = keyStrings.getAlias(CATokenConstants.CAKEYPURPOSE_CRLSIGN);
            if (log.isDebugEnabled()) {
                log.debug("CERTSIGN_NEXT: " + nextCertSignKeyLabel);
                log.debug("CERTSIGN:      " + currentCertSignKeyLabel);
                log.debug("CRLSIGN:       " + currentCrlSignKeyLabel);
            }
            if (StringUtils.equals(currentCertSignKeyLabel, currentCrlSignKeyLabel)) {
                log.info("Setting CRL signing key alias to: " + nextCertSignKeyLabel);
                caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CRLSIGN_STRING, nextCertSignKeyLabel);
            }
            log.info("Setting certificate signing key alias to: " + nextCertSignKeyLabel);
            caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING_PREVIOUS, currentCertSignKeyLabel);
            caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING, nextCertSignKeyLabel);
            caTokenProperties.remove(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING_NEXT);
            swichedSigningKey = !StringUtils.equals(nextCertSignKeyLabel, currentCertSignKeyLabel);
        }
        // Replace key sequence (if present)
        final String nextKeySequence = caTokenProperties.getProperty(CATokenConstants.NEXT_SEQUENCE_PROPERTY);
        final String currentKeySequence = getKeySequence();
        if (nextKeySequence != null) {
            if (log.isDebugEnabled()) {
                log.debug("Current KeySequence: " + getKeySequence());
            }
            log.info("Set key sequence from nextSequence: " + nextKeySequence);
            caTokenProperties.setProperty(CATokenConstants.PREVIOUS_SEQUENCE_PROPERTY, currentKeySequence);
            setKeySequence(nextKeySequence);
            caTokenProperties.remove(CATokenConstants.NEXT_SEQUENCE_PROPERTY);
        } else if (swichedSigningKey) {
            // If we did not have a next key sequence before this activation we generate one and push back the current.
            final String newKeySequence = StringTools.incrementKeySequence(getKeySequenceFormat(), currentKeySequence);
            caTokenProperties.setProperty(CATokenConstants.PREVIOUS_SEQUENCE_PROPERTY, currentKeySequence);
            setKeySequence(newKeySequence);
        } else {
            // So there is no key sequence and we didn't switch singing key..
            // ..let us just set the previous sequence to the current to at least match the singing key alias
            caTokenProperties.setProperty(CATokenConstants.PREVIOUS_SEQUENCE_PROPERTY, currentKeySequence);
        }
        // Store changes in the CAToken's properties
        setCATokenPropertyData(storeProperties(caTokenProperties));
    }

    /** Set the next singing key alias */
    public void setNextCertSignKey(String nextSignKeyAlias) {
        final Properties caTokenProperties = getProperties();
        caTokenProperties.setProperty(CATokenConstants.CAKEYPURPOSE_CERTSIGN_STRING_NEXT, nextSignKeyAlias);
        setCATokenPropertyData(storeProperties(caTokenProperties));
    }

    /** Set the next key sequence */
    public void setNextKeySequence(String newSequence) {
        final Properties caTokenProperties = getProperties();
        caTokenProperties.setProperty(CATokenConstants.NEXT_SEQUENCE_PROPERTY, newSequence);
        setCATokenPropertyData(storeProperties(caTokenProperties));
    }
}
