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

package org.ejbca.core.protocol.cmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author lars
 * @version $Id: CmpProxyConfig.java 13686 2012-01-04 12:16:56Z mikekushner $
 *
 */
public class CmpProxyConfig {
	private static final Logger log = Logger.getLogger(CmpProxyConfig.class.getName());
	/**
	 * Protocol definition.
	 *
	 */
	public enum Protokoll {
		TCP,
		HTTP
	}
	public final int proxyPort;
	public final int serverPort;
	public final String serverAddress;
	public final String proxyBindAddress;
	public final Protokoll serverProtokoll;
	public final String quickConfigFile;
	final static private String defaultName = "/cmpProxy.properties";
	private InputStream getStreamFromFile( String fileName ) throws LocalException {
		final File file = new File(fileName);
		if ( !file.exists() ) {
			return null;
		}
		if ( !file.canRead() ) {
			throw new LocalException("File '"+fileName+"' can not be read and therfore it can not be used as a config file.");
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new Error("This should never happen.", e);
		}
	}
	private Properties getConfigProperties(String fileName) throws LocalException  {
		if ( fileName!=null ) {
			final Properties p = getPropertiesFromFile(fileName);
			if ( p==null ) {
				final String s = "File + '"+fileName+"' is not existing. The file '"+fileName+"' was given as the first parameter to the start command of the proxy. If you remove this parameter the '"+defaultName+"' in the distribution directory will be used instead.";
				throw new LocalException(s);
			}
			return p;
		}
		final Properties rootConfP = getPropertiesFromFile("./conf"+defaultName);
		if ( rootConfP!=null ) {
			return rootConfP;
		}
		final Properties rootP = getPropertiesFromFile("."+defaultName);
		if ( rootP!=null ) {
			return rootP;
		}
		final InputStream resourceIS = getClass().getResourceAsStream(defaultName);
		if ( resourceIS==null ) {
			throw new LocalException("The resource '"+defaultName+"' is not existing in the proxy distribution");
		}
		try {
			final Properties resourceP = new Properties();
			resourceP.load(resourceIS);
			log.info("The resource '"+defaultName+"' is used to read the properties.");
			return resourceP;
		} catch (IOException e) {
			throw new LocalException("The resource '"+defaultName+"' is not a valid java properties file.");
		}
	}
	private Properties getPropertiesFromFile( String fileName ) throws LocalException {
		InputStream is = getStreamFromFile(fileName);
		if ( is==null ) {
			return null;
		}
		final Properties properties = new Properties();
		try {
			properties.load(is);
		} catch (IOException e) {
			throw new LocalException("File + '"+fileName+"' is not a valid java properties file.");
		}
		log.info("Configuration loaded from the properties file: '"+fileName+"'.");
		return properties;
	}
	private CmpProxyConfig(String configFileName) throws LocalException {
		final Properties properties = getConfigProperties(configFileName);
		this.proxyPort = getInt("proxyPort", properties);
		this.serverPort = getInt("serverPort", properties);
		this.proxyBindAddress = getString("proxyBindAddress", properties, null);
		this.serverAddress = getString("serverAddress", properties);
		this.quickConfigFile = getString("quickConfigFile", properties, null);
		final String tmp = getString("serverProtokoll", properties, null);
		this.serverProtokoll = tmp!=null && tmp.toLowerCase().indexOf("tcp")>=0 ? Protokoll.TCP : Protokoll.HTTP;
	}
	private String getString(String key, Properties properties) throws LocalException {
		final String sValue = getString(key, properties, null);
		if ( sValue==null ) {
			throw new LocalException("The property '"+key+"' not defined.");
		}
		return getString(key, properties, null);
	}
	private String getString(String key, Properties properties, String sDefault) {
		final String sValue = properties.getProperty(key);
		if ( sValue==null ) {
			return sDefault;
		}
		return sValue;
	}
	private int getInt(String key, Properties properties) throws LocalException {
		final String sValue = getString(key, properties);
		try {
			return Integer.parseInt(sValue);
		} catch( NumberFormatException e ) {
			throw new LocalException("The property '"+key+"' with the value '"+sValue+"' is not an integer");
		}
	}
	private class LocalException extends Exception {
		private static final long serialVersionUID = 6791123716235445587L;

        LocalException( String e ) {
			super(e);
		}
	}
	/**
	 * @param configFileName file name of the configuration. If not set a default is used instead.
	 * @return
	 */
	public static CmpProxyConfig getIt(String configFileName) {
		try {
			return new CmpProxyConfig(configFileName);
		} catch (LocalException e) {
			log.fatal(e.getMessage());
			return null;
		}
	}
}


