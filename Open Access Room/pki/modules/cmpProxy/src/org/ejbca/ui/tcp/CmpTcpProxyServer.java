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

package org.ejbca.ui.tcp;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.ejbca.core.protocol.cmp.CmpProxyConfig;
import org.ejbca.util.Log4jHandler;
import org.quickserver.net.AppException;
import org.quickserver.net.server.QuickServer;

/**
 * Starts and initiate the TCP proxy.
 * 
 * @author lars
 * @version $Id: CmpTcpProxyServer.java 10276 2010-10-25 12:12:33Z anatom $
 *
 */
public class CmpTcpProxyServer {
	private static final Logger log = Logger.getLogger(CmpTcpProxyServer.class);

	/**
	 * @throws UnknownHostException 
	 * @throws AppException 
	 * 
	 */
	private CmpTcpProxyServer(CmpProxyConfig config) throws UnknownHostException, AppException {
		final QuickServer myServer = new QuickServer();
		myServer.setClientAuthenticationHandler(null);
		if ( config.proxyBindAddress!=null ) {
			myServer.setBindAddr(config.proxyBindAddress);
		}
		myServer.setPort(config.proxyPort);
		final String cmdHandle;
		switch ( config.serverProtokoll ) {
		case TCP:
			cmdHandle = CmpTcpProxyCommandHandlerTcp.class.getName();
			break;
		case HTTP:
			cmdHandle = CmpTcpProxyCommandHandlerHttp.class.getName();
			break;
		default:
			log.fatal("No valid server protokoll.");
			return;
		}
		CmpTcpProxyCommandHandler.setConfig(config);
		myServer.setClientBinaryHandler(cmdHandle);
		myServer.setClientEventHandler(cmdHandle);
		myServer.getClientBinaryHandler();

		//reduce info to Console
		myServer.setConsoleLoggingToMicro();

		if ( config.quickConfigFile!=null && config.quickConfigFile.trim().length()>0 ) {
			Object _config[] = new Object[] {config.quickConfigFile};
			if ( !myServer.initService(_config) ) {
				System.out.println("Configuration from config file "+config.quickConfigFile+" failed!");
			}
		}
		myServer.startServer();	
		//myServer.getQSAdminServer().setShellEnable(true);
		//myServer.startQSAdminServer();			
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log4jHandler.add();
		log.trace("started");
		try {
			final CmpProxyConfig config = CmpProxyConfig.getIt(args.length>0 ? args[0] : null);
			if ( config==null ) {
				System.out.println("Cold not continue. Exiting the jvm.");
				System.exit(-1); // NOPMD, this is not a JEE component
				return;
			}
			new CmpTcpProxyServer(config);
			log.info("Server started.");
		} catch (Throwable t) {
			log.error("Not able to start. See exception", t);
		}
	}
}
