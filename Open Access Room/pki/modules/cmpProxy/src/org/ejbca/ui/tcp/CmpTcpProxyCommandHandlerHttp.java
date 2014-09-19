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
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalEjbcaResources;
import org.ejbca.core.protocol.cmp.CMPSendHTTP;
import org.quickserver.net.server.ClientHandler;

/**
 * Class receiving TCP messages from QuickServer and then sending the request to the server on a HTTP URL.
 * 
 * @author lars
 * @version $Id: CmpTcpProxyCommandHandlerHttp.java 14149 2012-02-20 22:55:04Z anatom $
 */
public class CmpTcpProxyCommandHandlerHttp extends CmpTcpProxyCommandHandler {
	private static final Logger log = Logger.getLogger(CmpTcpProxyCommandHandlerHttp.class.getName());
    /** Internal localization of logs and errors */
    private static final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();

	/* (non-Javadoc)
	 * @see org.ejbca.ui.tcp.CmpTcpProxyCommandHandler#getServerResult(byte[], boolean, org.quickserver.net.server.ClientHandler)
	 */
	@Override
	protected ServerResult getServerResult(byte[] message, boolean doClose, ClientHandler handler) throws IOException {
		final CMPSendHTTP send;
		try {
			send = CMPSendHTTP.doIt(message, config.serverAddress, config.serverPort, null, doClose);
		} catch( Throwable t ) { // NOPMD: catch all to report errors to client
			log.error(intres.getLocalizedMessage("cmp.errorprocess"), t);
			return getServerResult( null, false );
		}
		if ( send.responseCode!=HttpURLConnection.HTTP_OK ) {
			log.error(intres.getLocalizedMessage("cmp.responsecodenotok", Integer.valueOf(send.responseCode)));
			return getServerResult( null, false );			
		}
		return getServerResult( send.response, doClose );
	}
	private ServerResult getServerResult(byte response[], boolean doClose) throws IOException {
		final TcpReturnMessage returnM = TcpReturnMessage.createMessage(response, doClose);
		return new ServerResult(returnM.message, returnM.doClose);
	}
}
