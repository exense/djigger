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
package io.djigger.collector.server.conf;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;


public class CollectorConfig {
	
	String servicePort;

	MongoDBParameters db;
	
	Long dataTTL;
	
	List<String> connectionFiles;
	
	public List<String> getConnectionFiles() {
		return connectionFiles;
	}

	public void setConnectionFiles(List<String> connectionFiles) {
		this.connectionFiles = connectionFiles;
	}

	public MongoDBParameters getDb() {
		return db;
	}

	public void setDb(MongoDBParameters db) {
		this.db = db;
	}

	public String getServicePort() {
		return servicePort;
	}

	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	public Long getDataTTL() {
		return dataTTL;
	}

	public void setDataTTL(Long dataTTL) {
		this.dataTTL = dataTTL;
	}
}
