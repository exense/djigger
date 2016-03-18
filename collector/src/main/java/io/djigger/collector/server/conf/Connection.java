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
