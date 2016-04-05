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
import java.util.Map;
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Connection implements ConnectionGroupNode {
	
	@XStreamAsAttribute
	private String connectionClass;
	
	private Properties connectionProperties;
	
	private SamplingParameters samplingParameters;
	
	private Map<String, String> attributes;

	public String getConnectionClass() {
		return connectionClass;
	}

	public void setConnectionClass(String connectionClass) {
		this.connectionClass = connectionClass;
	}

	public Properties getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public SamplingParameters getSamplingParameters() {
		return samplingParameters;
	}

	public void setSamplingParameters(SamplingParameters samplingParameters) {
		this.samplingParameters = samplingParameters;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public List<ConnectionGroupNode> getGroups() {
		return null;
	}

}
