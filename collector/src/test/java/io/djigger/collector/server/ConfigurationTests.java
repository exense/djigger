package io.djigger.collector.server;

import java.util.List;
import java.util.stream.Collectors;

import io.djigger.client.AgentFacade;
import org.junit.Test;

import io.djigger.collector.server.conf.CollectorConfig;
import io.djigger.collector.server.conf.Configurator;
import io.djigger.collector.server.conf.ConnectionsConfig;
import junit.framework.Assert;


public class ConfigurationTests {
	
	@Test
	public void testSubscriptionsFile() throws Exception {
    	Server srv = new Server();

        String collConfigFilename = "./src/test/conf/Collector.xml";

        CollectorConfig config = Configurator.parseCollectorConfiguration(collConfigFilename);
        ConnectionsConfig cc = Configurator.parseConnectionsConfiguration(config.getConnectionFiles());


        srv.processGroup(null, cc.getConnectionGroup());
        
        List<ClientConnection> clients = srv.getClients();
        Assert.assertEquals(8, clients.size());
        List<ClientConnection> collect = clients.stream().filter(c -> c.getFacade() instanceof AgentFacade).collect(Collectors.toList());
        collect.forEach(c->Assert.assertEquals(4, c.getFacade().getInstrumentationSubscriptions().size()));
	}

}
