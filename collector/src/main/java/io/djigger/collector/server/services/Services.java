/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/
package io.djigger.collector.server.services;

import io.djigger.client.Facade;
import io.djigger.collector.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/services")
public class Services {

	@Inject
	Server server;
	
	@Context
	ServletContext context;
	
	public class FacadeStatus {
		Properties properties;
		
		boolean connected;

		public FacadeStatus(Properties properties, boolean connected) {
			super();
			this.properties = properties;
			this.connected = connected;
		}

		public Properties getProperties() {
			return properties;
		}

		public boolean isConnected() {
			return connected;
		}
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FacadeStatus> getStatus() {
		List<FacadeStatus> result = new ArrayList<>();
		for(Facade facade:server.getClients()) {
			//TODO do this in a more generic way
			Properties newProperties = new Properties();
			newProperties.putAll(facade.getProperties());
			if(newProperties.get("password")!=null) {
				newProperties.put("password", "*****");	
			}
			result.add(new FacadeStatus(newProperties, facade.isConnected()));
		}
		return result;
	}

}
