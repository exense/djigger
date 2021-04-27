package io.djigger.collector.server;

import io.djigger.accessors.ConnectionAccessor;
import io.djigger.client.Facade;
import io.djigger.model.Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnectionManager {

	private Map<String,ClientConnection> clients = new ConcurrentHashMap<>();
	private Map<String, List<String>> connectionsPropList = new ConcurrentHashMap<>();
	private ConnectionAccessor connectionAccessor;

	public ClientConnectionManager(ConnectionAccessor connectionAccessor) {
		this.connectionAccessor = connectionAccessor;
	}

	public boolean hasConnectionId(String id) {
		return clients.containsKey(id);
	}

	public void addClient(ClientConnection clientConnection) {
		clients.put(clientConnection.getFacade().getConnectionId(), clientConnection);
		String propAsString = clientConnection.getFacade().getConnectionPropertiesString();
		List<String> ids = connectionsPropList.get(propAsString);
		if (ids == null) {
			ids = new ArrayList<>();
		}
		ids.add(clientConnection.getFacade().getConnectionId());
		connectionsPropList.put(propAsString, ids);
	}

	public boolean singleConnection(Facade client) {
		List<String> ids = connectionsPropList.get(client.getConnectionPropertiesString());;
		return (ids != null && ids.size()==1);
	}

	protected Collection<ClientConnection> getClients() {
		return clients.values();
	}

	public ClientConnection getConnectionById(String id) {
		return clients.get(id);
	}

	public Collection<ClientConnection> browseClients() {
		synchronized (this) {
			return new ArrayList<>(this.getClients());
		}
	}

	public void changeConnectionState(String id, boolean state) throws Exception {
		ClientConnection clientConnection = this.getConnectionById(id);
		if (clientConnection != null) {
			clientConnection.getFacade().toggleConnection(state);
			Connection connection = clientConnection.getConnection();
			connection.setConnectionEnabled(state);
			connectionAccessor.save(connection);
		} else {
			throw new RuntimeException("The related connection could not be found");
		}
	}

	public void changeSamplingState(String id, boolean state) {
		ClientConnection clientConnection = this.getConnectionById(id);
		if (clientConnection != null) {
			clientConnection.getFacade().setSampling(state);
			Connection connection = clientConnection.getConnection();
			connection.setSamplingEnabled(state);
			connectionAccessor.save(connection);
		} else {
			throw new RuntimeException("The related connection could not be found");
		}
	}

	public void changeSamplingInterval(String id, int samplingInterval) {
		ClientConnection clientConnection = this.getConnectionById(id);
		if (clientConnection != null) {
			clientConnection.getFacade().setSamplingInterval(samplingInterval);
			Connection connection = clientConnection.getConnection();
			connection.getSamplingParameters().setSamplingRate(samplingInterval);
			connectionAccessor.save(connection);
		} else {
			throw new RuntimeException("The related connection could not be found");
		}
	}
}
