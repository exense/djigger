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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import java.net.InetSocketAddress;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class ServiceServer {

    private final io.djigger.collector.server.Server collectorServer;

    private String serverListenAddress;
    private int serverPort;

    public ServiceServer(io.djigger.collector.server.Server collectorServer) {
        super();
        this.collectorServer = collectorServer;
    }

    public void start(int serverPort, String serverListenAddress) throws Exception {
        this.serverPort = serverPort;
        this.serverListenAddress = serverListenAddress;

        Server webServer = configureServer();
        webServer.start();
        webServer.join();
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

        ServletContextHandler restContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        restContext.setContextPath("/rest");
        restContext.addServlet(sh, "/*");

        WebAppContext webContext = new WebAppContext();
        webContext.setServer(server);
        webContext.setContextPath("/djigger");
        webContext.setResourceBase(Resource.newClassPathResource("webapp").getURI().toString());

        WebAppContext rootContext = new WebAppContext();
        rootContext.setContextPath("/");
        rootContext.setResourceBase(Resource.newClassPathResource("webroot").getURI().toString());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{restContext, webContext, rootContext});
        server.setHandler(contexts);

        return server;
    }
}
