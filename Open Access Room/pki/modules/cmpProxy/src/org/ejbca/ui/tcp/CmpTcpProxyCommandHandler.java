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
/*
 * This file is part of the QuickServer library 
 * Copyright (C) 2003-2005 QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

package org.ejbca.ui.tcp;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalEjbcaResources;
import org.ejbca.core.protocol.cmp.CmpProxyConfig;
import org.quickserver.net.server.ClientBinaryHandler;
import org.quickserver.net.server.ClientEventHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.DataMode;
import org.quickserver.net.server.DataType;

/**
 * Base class receiving TCP messages from QuickServer and then sending the request to the server.
 * 
 * @author lars
 * @version $Id: CmpTcpProxyCommandHandler.java 12227 2011-08-10 13:31:23Z anatom $
 */
public abstract class CmpTcpProxyCommandHandler implements ClientBinaryHandler, ClientEventHandler {
	private static final Logger log = Logger.getLogger(CmpTcpProxyCommandHandler.class.getName());
    /** Internal localization of logs and errors */
    private static final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();
    static CmpProxyConfig config;
	/* (non-Javadoc)
	 * @see org.quickserver.net.server.ClientBinaryHandler#handleBinary(org.quickserver.net.server.ClientHandler, byte[])
	 */
	@Override
	public void handleBinary(ClientHandler handler, byte[] command) throws IOException {
		log.info(intres.getLocalizedMessage("cmp.receivedmsg", handler.getHostAddress()));
		long startTime = System.currentTimeMillis();
		final TcpReceivedMessage cmpTcpMessage = TcpReceivedMessage.getTcpMessage(command);
		if ( cmpTcpMessage.message==null )  {
			handler.closeConnection();
			return;
		}
		log.debug("Sending back CMP response to client.");
		final ServerResult result = getServerResult(cmpTcpMessage.message, cmpTcpMessage.doClose, handler);
		// Send back reply
		if ( result.message!=null ) {
			handler.sendClientBinary(result.message);			
			long endTime = System.currentTimeMillis();
			log.info(intres.getLocalizedMessage("cmp.sentresponsemsg", handler.getHostAddress(), Long.valueOf(endTime - startTime)));
		}else{
			log.fatal(intres.getLocalizedMessage("cmp.errorresponsenull"));
		}
		if ( cmpTcpMessage.doClose || result.doClose ) {
			handler.closeConnection(); // It's time to say good bye			
		}
	}
	protected class ServerResult {
		/**
		 * Result message to be sent to the client.
		 */
		final byte message[];
		/**
		 * true if the server wants the connection to the client to be closed
		 */
		final boolean doClose;
		ServerResult( byte m[], boolean c) {
			this.message = m;
			this.doClose = c;
		}
	}
	/**
	 * @param message to be sent to the server
	 * @param doClose true means that the result message will be patched to indicate to the client that the session will be closed.
	 * @param handler is unique for each connection in the client connection pool
	 * @return the result
	 * @throws IOException
	 */
	protected abstract ServerResult getServerResult( byte message[], boolean doClose, ClientHandler handler) throws IOException;
	/* (non-Javadoc)
	 * @see org.quickserver.net.server.ClientEventHandler#closingConnection(org.quickserver.net.server.ClientHandler)
	 */
	@Override
	public void closingConnection(ClientHandler handler) throws IOException {
		log.debug("Connection closed: "+handler.getHostAddress());
	}

	/* (non-Javadoc)
	 * @see org.quickserver.net.server.ClientEventHandler#gotConnected(org.quickserver.net.server.ClientHandler)
	 */
	@Override
	public void gotConnected(ClientHandler handler) throws SocketTimeoutException, IOException {
		log.debug("CMP connection opened: "+handler.getHostAddress());
		handler.setDataMode(DataMode.BINARY, DataType.IN);
		handler.setDataMode(DataMode.BINARY, DataType.OUT);
	}

	/* (non-Javadoc)
	 * @see org.quickserver.net.server.ClientEventHandler#lostConnection(org.quickserver.net.server.ClientHandler)
	 */
	@Override
	public void lostConnection(ClientHandler handler) throws IOException {
		log.warn("Connection lost: "+handler.getHostAddress());
	}
	/**
	 * Sets the proxy configuration
	 * @param c
	 */
	static void setConfig(CmpProxyConfig c) {
		config = c;
	}
}
