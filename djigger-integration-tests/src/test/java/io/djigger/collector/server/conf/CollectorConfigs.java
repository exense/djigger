package io.djigger.collector.server.conf;

import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Test helper to build {@link CollectorConfig} / {@link ConnectionsConfig} instances programmatically.
 * It lives in this package so it can instantiate the package-private {@link ConnectionGroup}.
 */
public final class CollectorConfigs {

    private CollectorConfigs() {
    }

    public static CollectorConfig collectorConfig(String host, int port, String user, String password,
                                                  String database, Long dataTtlSeconds) {
        MongoDBParameters db = new MongoDBParameters();
        db.setHost(host);
        db.setPort(String.valueOf(port));
        db.setUser(user);
        db.setPassword(password);
        db.setDatabase(database);

        CollectorConfig config = new CollectorConfig();
        config.setDb(db);
        config.setDataTTL(dataTtlSeconds);
        return config;
    }

    /**
     * A single connection group with one AgentFacade connection pointing at a djigger agent listening on
     * {@code localhost:agentPort}, sampling thread dumps at the given interval.
     */
    public static ConnectionsConfig singleAgentConnection(int agentPort, int samplingRateMs) {
        return singleAgentConnection(agentPort, samplingRateMs, null, null);
    }

    public static ConnectionsConfig singleAgentConnection(int agentPort, int samplingRateMs,
                                                          List<InstrumentSubscription> subscriptions,
                                                          MetricCollectionConfiguration metrics) {
        Connection connection = new Connection();
        connection.setConnectionClass("io.djigger.client.AgentFacade");

        Properties props = new Properties();
        props.setProperty("host", "localhost");
        props.setProperty("port", String.valueOf(agentPort));
        connection.setConnectionProperties(props);

        SamplingParameters sampling = new SamplingParameters();
        sampling.setSamplingRate(samplingRateMs);
        connection.setSamplingParameters(sampling);

        connection.setAttributes(new HashMap<>());
        connection.setSubscriptions(subscriptions != null ? subscriptions : new ArrayList<>());
        connection.setSubscriptionFiles(new ArrayList<>());
        connection.setMetrics(metrics);

        ConnectionGroup group = new ConnectionGroup();
        group.addGroup(connection);

        ConnectionsConfig cc = new ConnectionsConfig();
        cc.setConnectionGroup(group);
        return cc;
    }
}
