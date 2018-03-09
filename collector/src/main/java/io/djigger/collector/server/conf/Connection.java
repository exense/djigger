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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Connection implements ConnectionGroupNode {

    @XStreamAsAttribute
    private String connectionClass;

    private Properties connectionProperties;

    private SamplingParameters samplingParameters;

    private MetricCollectionConfiguration metrics;

    private List<InstrumentSubscription> subscriptions;

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

    public List<InstrumentSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<InstrumentSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public MetricCollectionConfiguration getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricCollectionConfiguration metrics) {
        this.metrics = metrics;
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

    public String toString() {
        Properties hiddenPassword = ((Properties) connectionProperties.clone());
        hiddenPassword.setProperty("password", "***");
        return connectionClass + ";" +
            hiddenPassword + ";" +
            samplingParameters.toString() + ";" +
            attributes + ";" +
            subscriptions + ";";
    }

}
