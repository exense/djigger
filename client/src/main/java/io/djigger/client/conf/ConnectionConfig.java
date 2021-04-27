package io.djigger.client.conf;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import io.djigger.mbeans.MetricCollectionConfiguration;
import io.djigger.model.Connection;
import io.djigger.model.SamplingParameters;
import io.djigger.model.Subscription;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import java.util.*;

public class ConnectionConfig implements ConnectionGroupNode {

	@XStreamAsAttribute
	private String connectionClass;

	private Properties connectionProperties;

	private SamplingParameters samplingParameters;

	private MetricCollectionConfiguration metrics;

	private List<InstrumentSubscription> subscriptions;

	List<String> subscriptionFiles;

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

	public List<String> getSubscriptionFiles() {
		return subscriptionFiles;
	}

	public void setSubscriptionFiles(List<String> subscriptionFiles) {
		this.subscriptionFiles = subscriptionFiles;
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
		hiddenPassword.setProperty(Connection.Parameters.PASSWORD, "***");
		return connectionClass + ";" +
				hiddenPassword + ";" +
				samplingParameters.toString() + ";" +
				attributes + ";" +
				subscriptions + ";";
	}


	public Connection convertToDBObject() {
		Connection con = new Connection();
		con.setProperties(this.connectionProperties);
		con.setAttributes(this.attributes);
		con.setMetrics(this.metrics);
		con.setConnectionClass(this.connectionClass);
		con.setConnectionEnabled(true);
		con.setSamplingEnabled(true);
		con.setSamplingParameters(this.samplingParameters);
		if (this.getSubscriptions() != null) {
			con.setSubscriptions(new ArrayList());
			this.subscriptions.forEach(s-> con.getSubscriptions().add(new Subscription(s)));
		}
		return con;
	}

}
