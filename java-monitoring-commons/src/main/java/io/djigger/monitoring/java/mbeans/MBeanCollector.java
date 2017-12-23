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
package io.djigger.monitoring.java.mbeans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;

import io.djigger.monitoring.eventqueue.EventQueue;
import io.djigger.monitoring.java.model.GenericArray;
import io.djigger.monitoring.java.model.GenericObject;
import io.djigger.monitoring.java.model.Metric;

public class MBeanCollector {

	private static final Logger logger = Logger.getLogger(EventQueue.class.getName());

	private MBeanServerConnection mBeanServerConnection;

	private Set<ObjectInstance> mBeans = new HashSet<ObjectInstance>();

	private Set<MBeanOperation> mBeanOperations = new HashSet<MBeanOperation>();

	public MBeanCollector(MBeanServerConnection connection) {
		super();
		this.mBeanServerConnection = connection;
	}
	
	private static class MBeanOperation {
		ObjectName objectName;
		String operationName;
		String[] operationArguments;
	}
	
	// "java.lang:*"
	public void registerMBeanAttribute(String objectName) {
		try {
			mBeans.addAll(mBeanServerConnection.queryMBeans(new ObjectName(objectName), null));
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while querying MBeans. Query:"+objectName, e);
		}
	}
	
	public void registerMBeanAttributeExclusion(String objectName) {
		try {
			mBeans.removeAll(mBeanServerConnection.queryMBeans(new ObjectName(objectName), null));
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while querying black listed MBeans. Query:"+objectName, e);
		}
	}
	
	public void registerMBeanOperation(String objectName, String operationName,  String... operationArguments) throws MalformedObjectNameException {
		MBeanOperation operation = new MBeanOperation();
		operation.objectName = new ObjectName(objectName);
		operation.operationName = operationName;
		operation.operationArguments = operationArguments;
		mBeanOperations.add(operation);
	}

	@FunctionalInterface
	public interface ValueListener {
		public void valueReceived(Metric<?> metric);
	}

	public void collect(ValueListener listener) {
		collectMBeanAttributes(listener);
		collectMBeanOperationResults(listener);
	}

	protected void collectMBeanAttributes(ValueListener listener) {
		long timestamp = System.currentTimeMillis();
		for (ObjectInstance mBean : mBeans) {
			collectMBeanAttributes(timestamp, listener, mBean.getObjectName());
		}
	}
	
	protected void collectMBeanOperationResults(ValueListener listener) {
		long timestamp = System.currentTimeMillis();
		for(MBeanOperation operation:mBeanOperations) {
			invokeMBeanOperationAndCollectResults(timestamp, listener, operation.objectName, operation.operationName, operation.operationArguments);
		}
	}
	
	protected void invokeMBeanOperationAndCollectResults(long timestamp, ValueListener listener, ObjectName mbeanName, String operationName, String... operationArguments) {
		MBeanInfo info;
		try {
			info = mBeanServerConnection.getMBeanInfo(mbeanName);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while getting MBeanInfo for " + mbeanName.toString(), e);
			return;
		}
		
		MBeanOperationInfo[] operations = info.getOperations();
		for (MBeanOperationInfo mBeanOperationInfo : operations) {
			if(mBeanOperationInfo.getName().equals(operationName)) {
				try {
					String[] argTypes = new String[operationArguments.length];
					for(int i=0;i<operationArguments.length;i++) {
						argTypes[i] = String.class.getName();
					}
					Object result = mBeanServerConnection.invoke(mbeanName, operationName, operationArguments, argTypes);
					GenericObject object = new GenericObject();
					recursive(object, operationName, result);
					invokeListener(timestamp, listener, mbeanName, object);
				} catch (Exception e) {
					logger.log(Level.SEVERE,"Error while invoking operation "+ mBeanOperationInfo.toString(), e);
				}
				
			}
		}
	}

	private void collectMBeanAttributes(long timestamp, ValueListener listener, ObjectName mbeanName) {
		MBeanInfo info;
		try {
			info = mBeanServerConnection.getMBeanInfo(mbeanName);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while getting MBeanInfo for " + mbeanName.toString(), e);
			return;
		}
		MBeanAttributeInfo[] attrInfos = info.getAttributes();

		GenericObject object = new GenericObject();
		for (MBeanAttributeInfo attr : attrInfos) {
			if (!attr.isReadable()) {
				logger.log(Level.WARNING,"MBeanInfo " + mbeanName.toString() + " is not readable");
				continue;
			}
			Object value;
			try {
				value = mBeanServerConnection.getAttribute(mbeanName, attr.getName());
			} catch (Exception e) {
				// logger.("Error while getting attribute "+
				// attr.getName()+" for MBean "+mbeanName.toString(),e);
				continue;
			}
			recursive(object, attr.getName(), value);
		}
		invokeListener(timestamp, listener, mbeanName, object);

	}

	public void invokeListener(long timestamp, ValueListener listener, ObjectName mbeanName, GenericObject object) {
		String metricName = mbeanName.getDomain() + "/" + mbeanName.getKeyPropertyListString();
		Metric<GenericObject> metric = new Metric<GenericObject>(timestamp, metricName, object);
		listener.valueReceived(metric);
	}

	private void recursive(GenericObject object, String key, Object value) {
		if (value instanceof CompositeData) {
			CompositeData composite = (CompositeData) value;
			object.put(key, toGenericObject(composite));
		} else if (value instanceof TabularData) {
			TabularData tabularData = (TabularData) value;
			@SuppressWarnings("unchecked")
			Collection<CompositeData> values = (Collection<CompositeData>) tabularData.values();
			GenericArray array = new GenericArray();
			for (CompositeData data : values) {
				array.add(toGenericObject(data));
			}
			object.put(key, array);
		} else if (value instanceof Number || value instanceof Boolean || value instanceof String){
			object.put(key, value);
		} else {
			// todo log
		}
	}

	private GenericObject toGenericObject(CompositeData compositeData) {
		GenericObject object = new GenericObject();
		CompositeType type = compositeData.getCompositeType();

		for (String key_ : type.keySet()) {
			Object value_ = compositeData.get(key_);
			recursive(object, key_, value_);
		}
		return object;
	}
}
