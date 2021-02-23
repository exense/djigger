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
package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import io.djigger.mbeans.MetricCollectionConfiguration;

import java.util.*;

public class Connection extends AbstractOrganizableObject  {

    @XStreamAsAttribute
    private String connectionClass;

    private Properties connectionProperties;

    private SamplingParameters samplingParameters;

    private MetricCollectionConfiguration metrics;

    private List<Subscription> subscriptions;

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

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
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

    public String toString() {
        Properties hiddenPassword = ((Properties) connectionProperties.clone());
        hiddenPassword.setProperty(Connection.Parameters.PASSWORD, "***");
        return connectionClass + ";" +
            hiddenPassword + ";" +
            samplingParameters.toString() + ";" +
            attributes + ";" +
            subscriptions + ";";
    }

    public static class Parameters {
        // used by JStackLogTailFacade
        public static final String FILE = "file";
        public static final String START_AT_FILE_BEGIN = "startAtFileBegin";

        // used by ProcessAttachFacade
        public static final String PROCESS_ID = "processID";
        public static final String PROCESS_NAME_PATTERN = "processNamePattern";
        public static final String CONNECTION_TIMEOUT = "connectionTimeoutMs";

        // used by AgentFacade and JMXClientFacade
        public static final String HOST = "host";
        public static final String PORT = "port";

        // used by JMXClientFacade
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

        /*
         Defines the order in which the parameters should be shown when listed.
         This is relevant for the collector status page, to guarantee a consistent and
         logical order, e.g: host, port, username, password. This, in turn, also makes
         the sort capabilities on the web page more useful.

         The comparator also accepts values not contained in the list of known parameter
         names. They will be sorted in alphabetical order, after the known ones.
        */
        public static final Comparator<String> SORT_COMPARATOR = new Comparator<String>() {
            private final List<String> knownParamsOrder = Arrays.asList(
                FILE, START_AT_FILE_BEGIN,
                PROCESS_ID, PROCESS_NAME_PATTERN, CONNECTION_TIMEOUT,
                HOST, PORT, USERNAME, PASSWORD
            );

            @Override
            public int compare(String s1, String s2) {
                int i1 = knownParamsOrder.indexOf(s1);
                int i2 = knownParamsOrder.indexOf(s2);

                if (i1 >= 0 && i2 >= 0) {
                    // both are known: compare by index in knownParamsOrder
                    return Integer.compare(i1, i2);
                } else if (i1 < 0 && i2 < 0) {
                    // both are unknown: compare by name
                    return s1.compareToIgnoreCase(s2);
                } else {
                    // exactly one is known (>=0), and one is unknown (<0).
                    // put the known one first, which is equivalent to a reverse integer sort
                    return -Integer.compare(i1, i2);
                }
            }
        };

    }
}
