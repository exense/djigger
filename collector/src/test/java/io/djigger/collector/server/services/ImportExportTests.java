package io.djigger.collector.server.services;

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.model.utils.EmbeddedMongoTestbench;
import ch.exense.commons.core.web.container.ServerContext;
import io.djigger.accessors.*;

import io.djigger.model.Connection;
import io.djigger.model.Subscription;


import junit.framework.Assert;
import org.junit.Test;


public class ImportExportTests extends EmbeddedMongoTestbench {
	
	@Test
	//don't know yet how to inject the accessor to the service object from junit
	public void testSubscriptionsFile() throws Exception {
		ServerContext context = new ServerContext();
		context.setConfiguration(new Configuration());
		ConnectionAccessor connectionAccessor = new ConnectionAccessor(session);
		SubscriptionAccessor subscriptionAccessor = new SubscriptionAccessor(session);
		//note: Initialisation of accessor with index creation not working with embedded mongo
		context.put(Connection.class.getName(), connectionAccessor);
		context.put(Subscription.class.getName(), subscriptionAccessor);

		ImportExportServices importSvc = new ImportExportServices();
		importSvc.context = context;
		importSvc.init();

		ImportExportServices.ImportRequest req = new ImportExportServices.ImportRequest();
        req.setPath("./src/test/conf/Collector.xml");

		importSvc.importConfiguration(req);

		Assert.assertEquals(6,connectionAccessor.getAllAsCollection().size());
		Assert.assertEquals(24,subscriptionAccessor.getAllAsCollection().size());

	}

}

