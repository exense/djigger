package io.djigger.monitoring.java.mbeans;

import java.io.Serializable;

public class MBeanOperation implements Serializable {

	private static final long serialVersionUID = 3932001915399575874L;
	
	String objectName;
	String operationName;
	String[] operationArguments;
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public String[] getOperationArguments() {
		return operationArguments;
	}
	public void setOperationArguments(String[] operationArguments) {
		this.operationArguments = operationArguments;
	}
}
