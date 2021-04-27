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
package io.djigger.collector.server;

import io.djigger.client.Facade;
import io.djigger.model.Connection;

import java.util.Map;

public class ClientConnection {

    Facade facade;

    Connection connection;

    public ClientConnection(Facade facade, Connection connection) {
        super();
        this.facade = facade;
        this.connection = connection;
    }

    public Facade getFacade() {
        return facade;
    }

    public Connection getConnection() {
        return connection;
    }

}
