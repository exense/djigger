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

import ch.exense.commons.core.server.Registrable;
import ch.exense.commons.core.web.container.ServerContext;
import io.djigger.client.Facade;
import io.djigger.client.FacadeStatus;
import io.djigger.collector.server.ClientConnection;
import io.djigger.collector.server.Server;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Singleton
@Path("/services")
public class Services implements Registrable {

    Server server;//serverContext

    @Inject
    ServerContext context;

    @PostConstruct
    public void init() {
        server= (Server) context.get(Server.class);
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FacadeStatus> getStatus() {
        List<FacadeStatus> result = new ArrayList<>();
        for (ClientConnection connection : server.getClients()) {

            Facade facade = connection.getFacade();

            Properties newProperties = new Properties();
            newProperties.putAll(facade.getProperties());

            // mask password
            if (newProperties.get(Facade.Parameters.PASSWORD) != null) {
                newProperties.put(Facade.Parameters.PASSWORD, "*****");
            }

            // if sampling, show at which interval
            // if sampling, show at which interval
            if (facade.isSampling()) {
                newProperties.put("samplingRate", facade.getSamplingInterval() + "");
            }

            result.add(new FacadeStatus(facade.getConnectionId(), facade.getClass().getSimpleName(),
                sorted(connection.getAttributes()), sorted(newProperties),
                facade.isConnected(),facade.getSamplingInterval(),facade.isSampling()));
        }
        return result;
    }

    @GET
    @Path("/toggleConnection/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void toggleConnection(@PathParam("id") String id) throws Exception {
        ClientConnection clientConnection = server.getClientConnection(id);
        if (clientConnection != null) {
            clientConnection.getFacade().toggleConnection();
        } else {
            throw new RuntimeException("The related connection could not be found");
        }
    }

    @GET
    @Path("/toggleSampling/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void toggleSampling(@PathParam("id") String id) {
        ClientConnection clientConnection = server.getClientConnection(id);
        if (clientConnection != null) {
            clientConnection.getFacade().setSampling(!clientConnection.getFacade().isSampling());
        } else {
            throw new RuntimeException("The related connection could not be found");
        }
    }

    @GET
    @Path("/samplingRate/{id}/{samplingInterval}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void toggleSampling(@PathParam("id") String id, @PathParam("samplingInterval") int samplingInterval) {
        ClientConnection clientConnection = server.getClientConnection(id);
        if (clientConnection != null) {
            clientConnection.getFacade().setSamplingInterval(samplingInterval);
        } else {
            throw new RuntimeException("The related connection could not be found");
        }

    }

    private SortedMap<String, String> sorted(Map<String, String> map) {
        // force a simple (alphabetical) sort
        TreeMap<String, String> sorted = new TreeMap<>((Comparator<String>) null);
        sorted.putAll(map);
        return sorted;
    }

    private SortedMap<String, String> sorted(Properties props) {
        // sort according to the logic defined in Facade.Parameters
        SortedMap<String, String> sorted = new TreeMap<>(Facade.Parameters.SORT_COMPARATOR);
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            sorted.put(key, value);
        }
        return sorted;
    }

}
