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
package io.djigger.client.mbeans;

import java.util.Collection;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.monitoring.java.model.Metric;

public class MBeanCollector {

	private static final Logger logger = LoggerFactory.getLogger(MBeanCollector.class);

	private MBeanServerConnection connection;

	private Set<ObjectInstance> mBeans;

	public MBeanCollector(MBeanServerConnection connection, Set<ObjectInstance> mBeans) {
		super();
		this.connection = connection;
		this.mBeans = mBeans;
	}

	@FunctionalInterface
	public interface ValueListener {
		public void valueReceived(Metric<?> metric);
	}

	public void collect(ValueListener listener) {
		long timestamp = System.currentTimeMillis();
		for (ObjectInstance mBean : mBeans) {
			collectBean(timestamp, listener, mBean.getObjectName());
		}
	}

	private void collectBean(long timestamp, ValueListener listener, ObjectName mbeanName) {
		MBeanInfo info;
		try {
			info = connection.getMBeanInfo(mbeanName);
		} catch (Exception e) {
			logger.error("Error while getting MBeanInfo for " + mbeanName.toString(), e);
			return;
		}
		MBeanAttributeInfo[] attrInfos = info.getAttributes();

		String metricName = mbeanName.getDomain() + "/" + mbeanName.getKeyPropertyListString();

		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (MBeanAttributeInfo attr : attrInfos) {
			if (!attr.isReadable()) {
				logger.warn("MBeanInfo " + mbeanName.toString() + " is not readable");
				continue;
			}
			Object value;
			try {
				value = connection.getAttribute(mbeanName, attr.getName());
			} catch (Exception e) {
				// logger.("Error while getting attribute "+
				// attr.getName()+" for MBean "+mbeanName.toString(),e);
				continue;
			}
			recursive(builder, attr.getName(), value);
		}
		Metric<JsonObject> metric = new Metric<JsonObject>(timestamp, metricName, builder.build());
		listener.valueReceived(metric);

	}

	private void recursive(JsonObjectBuilder builder, String key, Object value) {
		if (value instanceof Double) {
			builder.add(key, (Double) value);
		} else if (value instanceof Long) {
			builder.add(key, (Long) value);
		} else if (value instanceof Integer) {
			builder.add(key, (Integer) value);
		} else if (value instanceof String) {
			builder.add(key, (String) value);
		} else if (value instanceof Boolean) {
			builder.add(key, (Boolean) value);
		} else if (value instanceof CompositeData) {
			CompositeData composite = (CompositeData) value;
			builder.add(key, toJson(composite));
		} else if (value instanceof TabularData) {
			TabularData tabularData = (TabularData) value;
			@SuppressWarnings("unchecked")
			Collection<CompositeData> values = (Collection<CompositeData>) tabularData.values();
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (CompositeData data : values) {
				arrayBuilder.add(toJson(data));
			}
			builder.add(key, arrayBuilder);
		}
	}

	private JsonObjectBuilder toJson(CompositeData compositeData) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		CompositeType type = compositeData.getCompositeType();

		for (String key_ : type.keySet()) {
			Object value_ = compositeData.get(key_);
			recursive(objectBuilder, key_, value_);
		}
		return objectBuilder;
	}
}
