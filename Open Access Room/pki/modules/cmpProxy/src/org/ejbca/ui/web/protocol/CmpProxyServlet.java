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

package org.ejbca.ui.web.protocol;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.ejbca.core.model.InternalEjbcaResources;
import org.ejbca.core.protocol.cmp.CMPSendHTTP;
import org.ejbca.core.protocol.cmp.CMPSendTCP;
import org.ejbca.core.protocol.cmp.CmpProxyConfig;
import org.ejbca.ui.web.LimitLengthASN1Reader;

/**
 * @author lars
 * @version $Id: CmpProxyServlet.java 14351 2012-03-15 12:01:24Z anatom $
 *
 */
@SuppressWarnings("serial")
public class CmpProxyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(CmpProxyServlet.class);
    /** Internal localization of logs and errors */
    private static final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();
	private CmpProxyConfig config;
	private Connection connection;
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType("text/html");
		final File currentDir = new File(".");
		final PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Hello World!</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>Use post for CMP!</h1>");
		final String line = "Current directory: '"+currentDir.getCanonicalPath()+"'";
		out.println("<p>"+line+"</p>");
		log.info(line);
		out.println("</body>");
		out.println("</html>");
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest servletReq, HttpServletResponse servletResp) throws ServletException, IOException {
		try {
			log.trace(">doPost()");
			if ( this.config==null ) {
				throw new ServletException("Servlet not initialized.");
			}
			final ServletInputStream sin = servletReq.getInputStream();
			final ASN1Primitive message;
			try {
				message = new LimitLengthASN1Reader(sin, servletReq.getContentLength()).readObject();
			} catch ( IOException e ) {
				servletResp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				log.error( intres.getLocalizedMessage("cmp.errornoasn1"), e );
				return;
			}
			log.info( intres.getLocalizedMessage("cmp.receivedmsg", servletReq.getRemoteAddr()) );
			if ( log.isDebugEnabled() ) {
				log.debug( ASN1Dump.dumpAsString(message, true) );
			}
			final long startTime = System.currentTimeMillis();
			// Send back CMP response
			this.connection.send(message.getEncoded(), servletResp, servletReq.getContentType());
			final long endTime = System.currentTimeMillis();
			log.info( intres.getLocalizedMessage("cmp.sentresponsemsg", servletReq.getRemoteAddr(), Long.valueOf(endTime - startTime)) );
		} catch (Throwable t) { // NOPMD: catch all to report error back to client
			log.error("Error in CmpServlet:", t);
			servletResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
			if ( t instanceof ServletException ) {
				throw (ServletException)t;
			}
			if (t instanceof IOException ) {
				throw (IOException)t;
			}
			if ( t instanceof RuntimeException ) {
				throw (RuntimeException)t;
			}
			throw new ServletException(t);
		} finally {
			log.trace("<doPost()");
		}
	}
	interface Connection {
		/**
		 * @param message
		 * @param responseMessage
		 * @throws Exception
		 */
		void send( byte message[], HttpServletResponse servletResp, String contentType ) throws Exception;
	}
	class HttpConnection implements Connection {
		HttpConnection() {
			super();
			log.info("HTTP used to connect to the CA on the HTTP proxy");
		}
		/* (non-Javadoc)
		 * @see org.ejbca.ui.web.protocol.CmpProxyServlet.Connection#send(byte)
		 */
		@Override
		public void send(byte message[], HttpServletResponse servletResp, String contentType) throws Exception {
			final CMPSendHTTP result = CMPSendHTTP.doIt(message, CmpProxyServlet.this.config.serverAddress, CmpProxyServlet.this.config.serverPort, null, false);
			if ( result.responseCode!=HttpURLConnection.HTTP_OK ) {
				servletResp.sendError(result.responseCode);
				return;
			}
			sendReturn(servletResp, result.response, result.contentType);
		}	
	}
	class TcpConnection implements Connection {
		final private Set<Socket> sockets = new HashSet<Socket>();
		private boolean isSocketSetUsed = false;
		TcpConnection() {
			super();
			log.info("TCP used to connect to the CA on the HTTP proxy");
		}
		private synchronized Socket getFree() throws UnknownHostException, IOException, InterruptedException {
			while ( this.isSocketSetUsed ) {
				wait();
			}
			this.isSocketSetUsed = true;
			try {
				while ( true ) {
					final Iterator<Socket> i = this.sockets.iterator();
					if ( i==null || !i.hasNext() ) {
						break;
					}
					final Socket socket = i.next();
					this.sockets.remove(socket);
					if ( socket!=null && !socket.isClosed() && socket.isBound() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown() && socket.getInputStream().available()==0 ) {
						return socket;
					}
					log.info("Socket discarded.");
					if ( socket!=null ) {
						try {
							socket.close();
						} catch (IOException e1) {/*nothing to do*/}
					}
				}
				log.debug("New socket created.");
				final Socket socket = new Socket(CmpProxyServlet.this.config.serverAddress, CmpProxyServlet.this.config.serverPort);
				socket.setKeepAlive(true);
				return socket;
			} finally {
				this.isSocketSetUsed = false;
				notifyAll();
			}
		}
		/* (non-Javadoc)
		 * @see org.ejbca.ui.web.protocol.CmpProxyServlet.Connection#send(byte[], javax.servlet.http.HttpServletResponse, java.lang.String)
		 */
		@Override
		public void send(byte message[], HttpServletResponse servletResp, String contentType) throws Exception {
			final Socket socket = getFree();
			final CMPSendTCP send = new CMPSendTCP( message, socket, false );
			final boolean isServerClose = (send.flags&1)>0;
			if ( isServerClose ) {
				socket.close();
			} else {
				synchronized(this) {
					while ( this.isSocketSetUsed ) {
						wait();
					}
					this.isSocketSetUsed = true;
					try {
						this.sockets.add(socket);
					} finally {
						this.isSocketSetUsed = false;
						notifyAll();
					}
				}
			}
			if ( send.response==null || send.response.length<Integer.SIZE/8 ) {
				servletResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			sendReturn(servletResp, Arrays.copyOfRange(send.response, send.headerLength, send.response.length), "application/pkixcmp");
		}
	}
	private void sendReturn(HttpServletResponse responseMessage, byte message[],  String contentType) throws IOException {
        // Set content-type to general file
        responseMessage.setContentType(contentType);
        responseMessage.setContentLength(message.length);

        // Write the certificate
        final ServletOutputStream os = responseMessage.getOutputStream();
        os.write(message);
        responseMessage.flushBuffer();
        log.debug("Sent " + message.length + " bytes to client");
	}
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException{
		super.init(servletConfig);
		final String configFileName = servletConfig.getInitParameter("configFileName");
		this.config = CmpProxyConfig.getIt(configFileName);
		switch ( this.config.serverProtokoll ) {
		case HTTP:
			this.connection = new HttpConnection();
			break;
		case TCP:
			this.connection = new TcpConnection();
			break;
		}
	}
}
