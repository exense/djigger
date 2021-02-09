package io.djigger.collector.server;

import java.util.List;

import org.junit.Test;

import io.djigger.client.conf.CollectorConfig;
import io.djigger.client.conf.Configurator;
import io.djigger.client.conf.ConnectionsConfig;
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
        Assert.assertEquals(6, clients.size());
        clients.forEach(c->Assert.assertEquals(4, c.getFacade().getInstrumentationSubscriptions().size()));
	}

}
