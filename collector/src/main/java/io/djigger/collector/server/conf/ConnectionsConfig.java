/*******************************************************************************
 * (C) Copyright  2016 Dorian Cransac and others.
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
 *    - Dorian Cransac (dcransac)
 *    - Jérôme Comte
 *******************************************************************************/


/*******************************************************************************
 * ConnectionsConfig : separate abstraction containing a representation of the connections  
/*******************************************************************************/

package io.djigger.collector.server.conf;

public class ConnectionsConfig {

	ConnectionGroup connectionGroup;
	
	
	public ConnectionGroup getConnectionGroup() {
		return connectionGroup;
	}

	public void setConnectionGroup(ConnectionGroup connectionGroup) {
		this.connectionGroup = connectionGroup;
	}

	
}
