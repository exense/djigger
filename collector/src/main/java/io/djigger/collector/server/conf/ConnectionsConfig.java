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


/*******************************************************************************
 * ConnectionsConfig : separate abstraction containing a representation of the connections  
 /*******************************************************************************/

package io.djigger.collector.server.conf;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsConfig {

    ConnectionGroup connectionGroup;


    public ConnectionGroup getConnectionGroup() {
        return connectionGroup;
    }

    public void setConnectionGroup(ConnectionGroup connectionGroup) {
        this.connectionGroup = connectionGroup;
    }

    /*
     * @author dcransac
     * @since 20.05.2016
     *
     * 	For debugging purposes
     */

    public String toString() {
        List<String> result = new ArrayList<String>();
        for (ConnectionGroupNode cgn : connectionGroup.getGroups())
            result.add(cgn.toString());
        return result.toString();
    }

}
