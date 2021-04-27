package io.djigger.collector.server.services;

import ch.exense.commons.core.access.Secured;
import ch.exense.commons.core.server.Registrable;
import ch.exense.commons.core.web.container.ServerContext;
import io.djigger.accessors.ConnectionAccessor;
import io.djigger.accessors.SubscriptionAccessor;
import io.djigger.client.conf.CollectorConfig;
import io.djigger.client.conf.Configurator;
import io.djigger.client.conf.ConnectionConfig;
import io.djigger.client.conf.ConnectionsConfig;

import io.djigger.client.conf.ConnectionGroupNode;
import io.djigger.collector.server.Server;
import io.djigger.model.Connection;
import io.djigger.model.Subscription;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Path("/config")
public class ImportExportServices implements Registrable {

	private static final Logger logger = LoggerFactory.getLogger(ImportExportServices.class);

	private ConnectionAccessor connectionAccessor;
	private SubscriptionAccessor subscriptionAccessor;
	private Server server;

	@Inject
	ServerContext context;

	@PostConstruct
	public void init() {
		connectionAccessor = (ConnectionAccessor) context.get(Connection.class.getName());
		subscriptionAccessor = (SubscriptionAccessor) context.get(Subscription.class.getName());
		server = (Server) context.get(Server.class);
	}

	@POST
	@Path("/import")
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importConfiguration(ImportRequest importRequest) {
		CollectorConfig config = null;
		try {
			config = Configurator.parseCollectorConfiguration(importRequest.path);
			ConnectionsConfig cc = Configurator.parseConnectionsConfiguration(config.getConnectionFiles());
			processGroup(null, cc.getConnectionGroup());
			if (importRequest.isReload()) {
				server.loadConnections();
			}
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			logger.error("Import failed with exception.", e);
			AbstractResponse response = new AbstractResponse(AbstractResponse.ResponseStatus.ERROR,"Error = " + e.getClass().getName() + " : " + e.getMessage(),"");
			return Response.status(500).entity(response).build();
		}
	}

	public static class ImportRequest{
		public String path;
		public boolean reload;

		public ImportRequest() {
			super();
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public boolean isReload() {
			return reload;
		}
		public void setReload(boolean reload) {
			this.reload = reload;
		}
	}

	protected void processGroup(Map<String, String> attributeStack, ConnectionGroupNode groupNode) {

		HashMap<String, String> attributes = new HashMap<>();
		if (attributeStack != null) {
			attributes.putAll(attributeStack);
		}

		if (groupNode.getAttributes() != null && groupNode.getAttributes().size() > 0)
			attributes.putAll(groupNode.getAttributes());

		if (groupNode.getGroups() != null) {
			for (ConnectionGroupNode child : groupNode.getGroups()) {
				processGroup(attributes, child);
			}
		}

		if (groupNode instanceof ConnectionConfig) {
			ConnectionConfig connectionParam = (ConnectionConfig) groupNode;
			connectionParam.setAttributes(attributes);
			try {
				mergeSubscriptions(connectionParam);
			/*	Facade client = createClient(attributes, connectionParam);
				synchronized (clients) {
					clients.add(new ClientConnection(client, attributes));
				}*/
			} catch (Exception e) {
				logger.error("An error occurred while creating client " + connectionParam.toString(), e);
			} finally {
				//config files including all subscriptions parsed -> save all to DB
				saveParsedConfigToDB(connectionParam);
			}
		}
	}

	private void saveParsedConfigToDB(ConnectionConfig connectionParam) {
		List<String> subscriptionsIds = new ArrayList();
		Connection connection = connectionParam.convertToDBObject();
		//persist subscriptions
		//TODO detect duplicated templates?
		if (connection.getSubscriptions() != null) {
			subscriptionAccessor.save(connection.getSubscriptions());
		}
		connectionAccessor.save(connection);

	}

	private void mergeSubscriptions(ConnectionConfig connectionParam) throws Exception {
		List<InstrumentSubscription> subsFromFile = Configurator.parseSubscriptionsFiles(connectionParam.getSubscriptionFiles());
		if (subsFromFile != null) {
			if (connectionParam.getSubscriptions() == null) {
				connectionParam.setSubscriptions(new ArrayList());
			}
			subsFromFile.forEach(s -> connectionParam.getSubscriptions().add(s));
		}
	}
}
