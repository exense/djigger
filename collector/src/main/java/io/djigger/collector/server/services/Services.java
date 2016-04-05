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
