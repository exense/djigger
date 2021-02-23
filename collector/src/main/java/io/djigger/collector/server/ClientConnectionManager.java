package io.djigger.collector.server;

import io.djigger.client.Facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnectionManager {

	private Map<String,ClientConnection> clients = new ConcurrentHashMap<>();
	private Map<String, List<String>> connectionsPropList = new ConcurrentHashMap<>();


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

	public Collection<ClientConnection> getClients() {
		return clients.values();
	}

	public ClientConnection getConnectionById(String id) {
		return clients.get(id);
	}
}
