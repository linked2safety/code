/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ubitech.fistar.ejbcarestproxy.server;

import org.eclipse.jetty.server.Server;

/**
 *
 * @author Chris Paraskeva - www.ubitech.eu
 */
public enum ServerFactory {

    INSTANCE;

    private int DEFAULT_PORT = 8888;
    private Server jettyServer = null;

    public Server getJettyServer(int port) {
        if (jettyServer == null) {
            this.jettyServer = new Server(port);
        }
        return this.jettyServer;
    }

    public Server getJettyServer() {
        return this.getJettyServer(DEFAULT_PORT);
    }

}
