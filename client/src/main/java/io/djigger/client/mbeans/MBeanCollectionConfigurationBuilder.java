package io.djigger.client.mbeans;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.djigger.monitoring.java.mbeans.MBeanCollectorConfiguration;
import io.djigger.monitoring.java.mbeans.MBeanOperation;

public class MBeanCollectionConfigurationBuilder {

	public static MBeanCollectorConfiguration parse(Properties properties) {
		String mbeanAttributeList = properties.getProperty("metrics.mbeans.attributes");
		String mbeanOperationList = properties.getProperty("metrics.mbeans.operations");
		MBeanCollectorConfiguration result = new MBeanCollectorConfiguration();
		
		result.addMBeanAttribute("java.lang:*");
		if(mbeanAttributeList!=null) {
			String[] list = mbeanAttributeList.split("#");
			for(String mBeanAttribute:list) {
				result.addMBeanAttribute(mBeanAttribute);
			}
		}
		if(mbeanOperationList!=null) {
			String[] list = mbeanOperationList.split("#");
			for(String mBeanOperation:list) {
				Pattern mBeanOperationPattern = Pattern.compile("(.*)\\.([^\\.]+)\\((.?)\\)");
				Matcher mBeanOperationMatcher = mBeanOperationPattern.matcher(mBeanOperation);
				if(mBeanOperationMatcher.matches()) {
					MBeanOperation operation = new MBeanOperation();
					operation.setObjectName(mBeanOperationMatcher.group(1));
					operation.setOperationName(mBeanOperationMatcher.group(2));
					operation.setOperationArguments(mBeanOperationMatcher.group(3).split(","));
					result.addMBeanOperation(operation);
				}
			}
		}
		return result;
	}
}
