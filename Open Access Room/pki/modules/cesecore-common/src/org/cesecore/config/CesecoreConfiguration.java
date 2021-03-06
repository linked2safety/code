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

package org.cesecore.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This file handles configuration from ejbca.properties
 * 
 * @version $Id: CesecoreConfiguration.java 18154 2013-11-14 15:44:27Z anatom $
 */
public final class CesecoreConfiguration {

    private static final Logger log = Logger.getLogger(CesecoreConfiguration.class);

    /** NOTE: diff between EJBCA and CESeCore */
    public static final String PERSISTENCE_UNIT = "ejbca";

    /** This is a singleton so it's not allowed to create an instance explicitly */
    private CesecoreConfiguration() {
    }

    private static final String TRUE = "true";

    /**
     * Cesecore Datasource name
     */
    public static String getDataSourceJndiName() {
        String prefix = ConfigurationHolder.getString("datasource.jndi-name-prefix");
        String name = ConfigurationHolder.getString("datasource.jndi-name");

        return prefix + name;
    }

    /**
     * Password used to protect CA keystores in the database.
     */
    public static String getCaKeyStorePass() {
        return ConfigurationHolder.getExpandedString("ca.keystorepass");
    }

    /**
     * The length in octets of certificate serial numbers generated. 8 octets is a 64 bit serial number.
     */
    public static int getCaSerialNumberOctetSize() {
        String value = ConfigurationHolder.getString("ca.serialnumberoctetsize");
        if (!"8".equals(value) && !"4".equals(value)) {
            value = "8";
        }
        return Integer.parseInt(value);
    }

    /**
     * The algorithm that should be used to generate random numbers (Random Number Generator Algorithm)
     */
    public static String getCaSerialNumberAlgorithm() {
        return ConfigurationHolder.getString("ca.rngalgorithm");
    }

    /**
     * The date and time from which an expire date of a certificate is to be considered to be too far in the future.
     */
    public static String getCaTooLateExpireDate() {
        return ConfigurationHolder.getExpandedString("ca.toolateexpiredate");
    }

    /**
     * @return true if it is permitted to use an extractable private key in a HSM.
     */
    public static boolean isPermitExtractablePrivateKeys() {
        final String value = ConfigurationHolder.getString("ca.doPermitExtractablePrivateKeys");
        return value != null && value.trim().equalsIgnoreCase(TRUE);
    }

    /**
     * The language that should be used internally for logging, exceptions and approval notifications.
     */
    public static String getInternalResourcesPreferredLanguage() {
        return ConfigurationHolder.getExpandedString("intresources.preferredlanguage");
    }

    /**
     * The language used internally if a resource not found in the preferred language
     */
    public static String getInternalResourcesSecondaryLanguage() {
        return ConfigurationHolder.getExpandedString("intresources.secondarylanguage");
    }

    /**
     * Sets pre-defined EC curve parameters for the implicitlyCA facility.
     */
    public static String getEcdsaImplicitlyCaQ() {
        return ConfigurationHolder.getExpandedString("ecdsa.implicitlyca.q");
    }

    /**
     * Sets pre-defined EC curve parameters for the implicitlyCA facility.
     */
    public static String getEcdsaImplicitlyCaA() {
        return ConfigurationHolder.getExpandedString("ecdsa.implicitlyca.a");
    }

    /**
     * Sets pre-defined EC curve parameters for the implicitlyCA facility.
     */
    public static String getEcdsaImplicitlyCaB() {
        return ConfigurationHolder.getExpandedString("ecdsa.implicitlyca.b");
    }

    /**
     * Sets pre-defined EC curve parameters for the implicitlyCA facility.
     */
    public static String getEcdsaImplicitlyCaG() {
        return ConfigurationHolder.getExpandedString("ecdsa.implicitlyca.g");
    }

    /**
     * Sets pre-defined EC curve parameters for the implicitlyCA facility.
     */
    public static String getEcdsaImplicitlyCaN() {
        return ConfigurationHolder.getExpandedString("ecdsa.implicitlyca.n");
    }

    /**
     * Flag indicating if the BC provider should be removed before installing it again. When developing and re-deploying alot this is needed so you
     * don't have to restart JBoss all the time. In production it may cause failures because the BC provider may get removed just when another thread
     * wants to use it. Therefore the default value is false.
     */
    public static boolean isDevelopmentProviderInstallation() {
        return TRUE.equalsIgnoreCase(ConfigurationHolder.getString("development.provider.installation"));
    }

    /** Parameter to specify if retrieving CAInfo and CA from CAAdminSession should be cached, and in that case for how long. */
    public static long getCacheCaTimeInCaSession() {
        // Cache for 10 seconds is the default (Changed 2013-02-14 under ECA-2801.)
        return getLongValue("cainfo.cachetime", 10000L, "milliseconds to cache CA info");
    }

    /** @return configuration for when cached CryptoTokens are considered stale and will be refreshed from the database. */
    public static long getCacheTimeCryptoToken() {
        return getLongValue("cryptotoken.cachetime", 10000L, "milliseconds");
    }

    /** @return configuration for when cached SignersMapping are considered stale and will be refreshed from the database. */
    public static long getCacheTimeInternalKeyBinding() {
        return getLongValue("internalkeybinding.cachetime", 10000L, "milliseconds");
    }

    /** Parameter to specify if retrieving Certificate profiles in StoreSession should be cached, and in that case for how long. */
    public static long getCacheCertificateProfileTime() {
        return getLongValue("certprofiles.cachetime", 1000L, "milliseconds to cache Certificate profiles");
    }

    /** Parameter to specify if retrieving Authorization Access Rules (in AuthorizationSession) should be cached, and in that case for how long. */
    public static long getCacheAuthorizationTime() {
        return getLongValue("authorization.cachetime", 30000L, "milliseconds to cache authorization");
    }

    private static long getLongValue(final String propertyName, final long defaultValue, final String unit) {
        final String value = ConfigurationHolder.getString(propertyName);
        long time = defaultValue;
        try {
            if (value!=null) {
                time = Long.valueOf(value);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid value for " + propertyName + ". Using default " + defaultValue + ". Value must be decimal number (" + unit + "): " + e.getMessage());
        }
        return time;
    }

    public static Class<?> getTrustedTimeProvider() throws ClassNotFoundException {
        String providerClass = ConfigurationHolder.getString("time.provider");
        if(log.isDebugEnabled()) {
            log.debug("TrustedTimeProvider class: "+providerClass);
        }
        return Class.forName(providerClass);
    }

    /**
     * Regular Expression to fetch the NTP offset from an NTP client output
     */
    public static Pattern getTrustedTimeNtpPattern() {
        String regex = ConfigurationHolder.getString("time.ntp.pattern");
        return Pattern.compile(regex);
    }

    /**
     * System command to execute an NTP client call and obtain information about the selected peers and their offsets
     */
    public static String getTrustedTimeNtpCommand() {
        return ConfigurationHolder.getString("time.ntp.command");
    }

    /**
     * Option if we should keep JBoss serialized objects as such, or convert them to JPA/hibernate serialization. Used for backwards compatibility
     * with older versions of EJBCA than 4.0.0.
     */
    public static boolean isKeepJbossSerializationIfUsed() {
        final String value = ConfigurationHolder.getString("db.keepjbossserialization");
        return value != null && value.trim().equalsIgnoreCase(TRUE);
    }

    /**
     * Option if we should keep internal CA keystores in the CAData table to be compatible with CeSecore 1.1/EJBCA 5.0.
     * Default to true. Set to false when all nodes in a cluster have been upgraded to CeSecore 1.2/EJBCA 5.1 or later,
     * then internal keystore in CAData will be replaced with a foreign key in to the migrated entry in CryptotokenData.
     */
    public static boolean isKeepInternalCAKeystores() {
        final String value = ConfigurationHolder.getString("db.keepinternalcakeystores");
        return value == null || !value.trim().equalsIgnoreCase("false");
    }

    /**
     * When we run in a cluster, each node should have it's own identifier. By default we use the DNS name.
     */
    public static String getNodeIdentifier() {
    	final String PROPERTY_NAME = "cluster.nodeid";
    	final String PROPERTY_VALUE = "undefined";
        String value = ConfigurationHolder.getString(PROPERTY_NAME);
        if (value == null) {
        	try {
				value = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				log.warn(PROPERTY_NAME + " is undefined on this host and was not able to resolve hostname. Using " + PROPERTY_VALUE + " which is fine if use a single node.");
				value = PROPERTY_VALUE;
			}
			// Update configuration, so we don't have to make a hostname lookup each time we call this method.
			ConfigurationHolder.updateConfiguration(PROPERTY_NAME, value);
        }
        return value;
    }

    /** Oid tree for GOST32410 */
    public static String getOidGost3410() {
        return ConfigurationHolder.getString("extraalgs.gost3410.oidtree");
    }

    /** Oid tree for DSTU4145 */
    public static String getOidDstu4145() {
        return ConfigurationHolder.getString("extraalgs.dstu4145.oidtree");
    }
    
    /** Returns extraalgs such as GOST, DSTU */
    public static List<String> getExtraAlgs() {
        return ConfigurationHolder.getPrefixedPropertyNames("extraalgs");
    }
    
    /** Returns "subalgorithms", e.g. different keylengths or curves */
    public static List<String> getExtraAlgSubAlgs(String algName) {
        return ConfigurationHolder.getPrefixedPropertyNames("extraalgs." + algName + ".subalgs");
    }
    
    public static String getExtraAlgSubAlgTitle(String algName, String subAlg) {
        String name = ConfigurationHolder.getString("extraalgs." + algName + ".subalgs." + subAlg + ".title");
        if (name == null) {
            // Show the algorithm name, if it has one
            String end = ConfigurationHolder.getString("extraalgs." + algName + ".subalgs." + subAlg + ".name");
            // Otherwise, show the key name in the configuration
            if (end == null) { end = subAlg; }
            name = ConfigurationHolder.getString("extraalgs." + algName + ".title") + " " + end;
        }
        return name;
    }
    
    public static String getExtraAlgSubAlgName(String algName, String subAlg) {
        String name = ConfigurationHolder.getString("extraalgs." + algName + ".subalgs." + subAlg + ".name");
        if (name == null) {
            // Not a named algorithm
            name = getExtraAlgSubAlgOid(algName, subAlg);
        }
        return name;
    }
    
    public static String getExtraAlgSubAlgOid(String algName, String subAlg) {
        final String oidTree = ConfigurationHolder.getString("extraalgs." + algName + ".oidtree");
        final String oidEnd = ConfigurationHolder.getString("extraalgs." + algName + ".subalgs." + subAlg + ".oid");
        
        if (oidEnd != null && oidTree != null) { return oidTree + "." + oidEnd; }
        if (oidEnd != null) { return oidEnd; }
        else { return null; }
    }

    /**
     * @return true if the Base64CertData table should be used for storing the certificates.
     */
    public static boolean useBase64CertTable() {
        final String value = ConfigurationHolder.getString("database.useSeparateCertificateTable");
        return value!=null && Boolean.parseBoolean(value.trim());
    }
    
    /** If database integrity protection should be used or not. */
    public static boolean useDatabaseIntegrityProtection(final String tableName) {
        // First check if we have explicit configuration for this entity
        final String enableProtect = ConfigurationHolder.getString("databaseprotection.enablesign." + tableName);
        if (enableProtect != null) {
            return Boolean.TRUE.toString().equalsIgnoreCase(enableProtect);
        }
        // Otherwise use the global or default
        return Boolean.TRUE.toString().equalsIgnoreCase(ConfigurationHolder.getString("databaseprotection.enablesign"));
    }

    /** If database integrity verification should be used or not. */
    public static boolean useDatabaseIntegrityVerification(final String tableName) {
        // First check if we have explicit configuration for this entity
        final String enableVerify = ConfigurationHolder.getString("databaseprotection.enableverify." + tableName);
        if (enableVerify != null) {
            return Boolean.TRUE.toString().equalsIgnoreCase(enableVerify);
        }
        // Otherwise use the global or default
        return Boolean.TRUE.toString().equalsIgnoreCase(ConfigurationHolder.getString("databaseprotection.enableverify"));
    }
    
    public static boolean getCaKeepOcspExtendedService() {
        return Boolean.valueOf(ConfigurationHolder.getString("ca.keepocspextendedservice").toLowerCase());
    }

    /**
     * Used just in {@link #getForbiddenCharacters()}. The method is called very
     * often so we declare this String in the class so it does not have to be
     * each time the method is called.
     */
    final static private String FORBIDDEN_CARACTERS_KEY = "forbidden.characters";
    /**
     * Characters forbidden in fields to be stored in the DB.
     * @return all forbidden characters.
     */
    public static char[] getForbiddenCharacters() {
        // Using 'instance().getString' instead of 'getString' since an empty
        // String (size 0) must be returned when the property is defined without
        // any value.
        final String s = ConfigurationHolder.instance().getString(FORBIDDEN_CARACTERS_KEY);
        if (s==null) {
            return ConfigurationHolder.getDefaultValue(FORBIDDEN_CARACTERS_KEY).toCharArray();
        }
        return s.toCharArray();
    }
}

