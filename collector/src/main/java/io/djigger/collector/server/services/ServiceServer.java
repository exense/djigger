/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *
 *  This file is part of djigger
 *
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.collector.server.services;

import java.net.InetSocketAddress;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class ServiceServer {

    private final io.djigger.collector.server.Server collectorServer;

    private String serverListenAddress;
    private int serverPort;

    private Server webServer;

    public ServiceServer(io.djigger.collector.server.Server collectorServer) {
        super();
        this.collectorServer = collectorServer;
    }

    /**
     * Starts the embedded web server. This method returns as soon as the server is started; call
     * {@link #join()} to block on it (as the standalone collector does) or {@link #stop()} to shut it down.
     */
    public void start(int serverPort, String serverListenAddress) throws Exception {
        this.serverPort = serverPort;
        this.serverListenAddress = serverListenAddress;

        webServer = configureServer();
        webServer.start();
    }

    /**
     * @return the port the embedded web server is actually listening on (useful when started on an
     * ephemeral port, i.e. port 0).
     */
    public int getLocalPort() {
        return ((org.eclipse.jetty.server.ServerConnector) webServer.getConnectors()[0]).getLocalPort();
    }

    public void join() throws InterruptedException {
        if (webServer != null) {
            webServer.join();
        }
    }

    public void stop() throws Exception {
        if (webServer != null) {
            webServer.stop();
        }
    }

    private Server configureServer() {
        ResourceConfig resourceConfig = new ResourceConfig();


        resourceConfig.packages(Services.class.getPackage().getName());
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(collectorServer).to(io.djigger.collector.server.Server.class);
            }
        });
        ServletContainer servletContainer = new ServletContainer(resourceConfig);
        ServletHolder sh = new ServletHolder(servletContainer);
        Server server = new Server(new InetSocketAddress(serverListenAddress, serverPort));

        ServletContextHandler restContext = new ServletContextHandler();
        restContext.setContextPath("/rest");
        restContext.addServlet(sh, "/*");

        // Jetty 12: static content is served from a base Resource obtained via a ResourceFactory
        // (Resource.newClassPathResource / setResourceBase(String) were removed).
        WebAppContext webContext = new WebAppContext();
        webContext.setServer(server);
        webContext.setContextPath("/djigger");
        webContext.setBaseResource(ResourceFactory.of(webContext).newClassLoaderResource("webapp"));

        WebAppContext rootContext = new WebAppContext();
        rootContext.setContextPath("/");
        rootContext.setBaseResource(ResourceFactory.of(rootContext).newClassLoaderResource("webroot"));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(restContext);
        contexts.addHandler(webContext);
        contexts.addHandler(rootContext);
        server.setHandler(contexts);

        return server;
    }
}
