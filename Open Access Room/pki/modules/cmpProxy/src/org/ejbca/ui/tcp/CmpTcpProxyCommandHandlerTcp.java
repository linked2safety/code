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

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalEjbcaResources;
import org.ejbca.core.protocol.cmp.CMPSendTCP;
import org.quickserver.net.server.ClientHandler;

/**
 * Class receiving TCP messages from QuickServer and then sending the request to the server on a TCP port.
 * 
 * @author lars
 * @version $Id: CmpTcpProxyCommandHandlerTcp.java 14149 2012-02-20 22:55:04Z anatom $
 */
public class CmpTcpProxyCommandHandlerTcp extends CmpTcpProxyCommandHandler {
	private static final Logger log = Logger.getLogger(CmpTcpProxyCommandHandlerTcp.class.getName());
	/** Internal localization of logs and errors */
	private static final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();
	final private Map<ClientHandler, Socket> sockets = new HashMap<ClientHandler, Socket>();

	/* (non-Javadoc)
	 * @see org.ejbca.ui.tcp.CmpTcpProxyCommandHandler#getServerResult(byte[], boolean, org.quickserver.net.server.ClientHandler)
	 */
	@Override
	protected ServerResult getServerResult(byte[] message, boolean doClose, ClientHandler handler) {
		Socket socket = this.sockets.get(handler);
		try {
			if ( socket==null || socket.isClosed() || !socket.isBound() || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown() || socket.getInputStream().available()>0 ) {
				removeSocket(handler);
				log.debug("New socket created.");
				socket = new Socket(config.serverAddress, config.serverPort);
				socket.setKeepAlive(true);
				this.sockets.put(handler, socket);
			}
			final CMPSendTCP send = new CMPSendTCP( message, socket, doClose );
			final boolean isServerClose = (send.flags&1)>0;
			if ( isServerClose ) {
				removeSocket(handler);
			}
			return new ServerResult( send.response, isServerClose );
		} catch ( Throwable e ) { // NOPMD: catch all to report errors to client
			removeSocket(handler);
			log.error(intres.getLocalizedMessage("cmp.errorgeneral"), e);
			return new ServerResult( null, true );
		}
	}
	private void removeSocket(ClientHandler handler) {
		final Socket socket = this.sockets.remove(handler);
		if ( socket!=null ) {
			try {
				socket.close();
			} catch (IOException e1) {/*nothing to do*/}
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		final Iterator<ClientHandler> i = this.sockets.keySet().iterator();
		while ( i.hasNext() ) {
			removeSocket(i.next());
		}
	}
}
