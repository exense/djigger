package io.djigger.collector.server;

import ch.exense.commons.core.server.AbstractStandardServer;
import ch.exense.commons.core.web.container.ServerContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DjiggerServer extends AbstractStandardServer {

	private static final Logger logger = LoggerFactory.getLogger(DjiggerServer.class);

	@Override
	protected void registerExplicitly_(ResourceConfig resourceConfig) {

	}

	@Override
	protected void configure_() {

	}

	@Override
	public void postInitContext(ServerContext serverContext) {
		Server server = new Server();
		try {
			server.start(context);
			serverContext.put(Server.class,server);
		} catch (Exception e) {
			logger.error("Unable to start the server", e);
		}
	}

	@Override
	public void initContext(ServerContext serverContext) {

	}

	@Override
	public String provideWebappFolderName() {
		return "webapp";
	}

	@Override
	public String provideServiceContextPath() {
		return "/rest";
	}
}
