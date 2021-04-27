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

import ch.exense.commons.core.access.Secured;
import ch.exense.commons.core.server.Registrable;
import ch.exense.commons.core.web.container.ServerContext;
import io.djigger.client.Facade;
import io.djigger.collector.server.ClientConnection;
import io.djigger.collector.server.ClientConnectionManager;
import io.djigger.model.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Path("/connections")
public class ConnectionsServices implements Registrable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsServices.class);

    ClientConnectionManager ccMgr;

    @Inject
    ServerContext context;

    @PostConstruct
    public void init() {
        ccMgr= (ClientConnectionManager) context.get(ClientConnectionManager.class);
    }

    @GET
    @Secured
    @Path("/connection/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Connection getConnection(@PathParam("id") String id) {
        return ccMgr.getConnectionById(id).getConnection();
    }

    @GET
    @Secured
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FacadeProperties> getStatus() {
        List<FacadeProperties> result = new ArrayList<>();
        for (ClientConnection connection : ccMgr.browseClients()) {

            Facade facade = connection.getFacade();

            Properties newProperties = new Properties();
            newProperties.putAll(facade.getProperties());

            // mask password
            if (newProperties.get(Connection.Parameters.PASSWORD) != null) {
                newProperties.put(Connection.Parameters.PASSWORD, "*****");
            }

            // if sampling, show at which interval
            // if sampling, show at which interval
            if (facade.isSampling()) {
                newProperties.put("samplingRate", facade.getSamplingInterval() + "");
            }

            result.add(new FacadeProperties(connection));
        }
        return result;
    }

    @GET
    @Secured
    @Path("/toggleConnection/{id}/{state}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleConnection(@PathParam("id") String id, @PathParam("state") boolean newStateOn)  {
        try {
            ccMgr.changeConnectionState(id,newStateOn);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    @POST
    @Secured
    @Path("/disableConnections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response disableConnections(List<String> ids)  {
        try {
            changeManyConnectionState(ids, false);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    @POST
    @Secured
    @Path("/enableConnections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableConnections(List<String> ids) {
        try {
            changeManyConnectionState(ids, true);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    private void changeManyConnectionState(List<String> ids, boolean state) throws Exception {
        AtomicInteger errors = new AtomicInteger();
        ids.forEach(id -> {
            try {
                ccMgr.changeConnectionState(id,state);
            } catch (Exception e) {
                logger.error("Disable connection service failed for id " + id,e);
                errors.incrementAndGet();
            }
        });
        if (errors.get() > 0) {
            throw new RuntimeException("Changing the connection state failed for " + errors.get() + " connections. More details can be found in the logs.");
        }
    }

    @GET
    @Secured
    @Path("/toggleSampling/{id}/{newState}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleSampling(@PathParam("id") String id, @PathParam("newState") boolean newState) {
        try {
            ccMgr.changeSamplingState(id, newState);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    @POST
    @Secured
    @Path("/stopSampling")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopSampling(List<String> ids)  {
        try {
            changeManySamplingState(ids,false);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    @POST
    @Secured
    @Path("/startSampling")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startSampling(List<String> ids) {
        try {
            changeManySamplingState(ids,true);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }

    private void changeManySamplingState(List<String> ids, boolean state) throws Exception {
        AtomicInteger errors = new AtomicInteger();
        ids.forEach(id -> {
            try {
                ccMgr.changeSamplingState(id,state);
            } catch (Exception e) {
                logger.error("Disable connection service failed for id: " + id,e);
                errors.incrementAndGet();
            }
        });
        if (errors.get() > 0) {
            throw new RuntimeException("Changing the sampling state failed for " + errors.get() + " connections. More details can be found in the logs.");
        }
    }

    @GET
    @Secured
    @Path("/samplingRate/{id}/{samplingInterval}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editSampling(@PathParam("id") String id, @PathParam("samplingInterval") int samplingInterval) {
        try {
            ccMgr.changeSamplingInterval(id, samplingInterval);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type("text/plain").build();
        }
    }



}
