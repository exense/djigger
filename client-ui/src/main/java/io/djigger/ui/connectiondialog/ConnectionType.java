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
package io.djigger.ui.connectiondialog;

import io.djigger.ui.Session.SessionType;

public enum ConnectionType {

	STORE("Store", SessionType.STORE, HostConnectionParameter.class),
	
	JMX("JMX", SessionType.JMX, HostConnectionParameter.class),
	
	AGENT("Agent", SessionType.AGENT, HostConnectionParameter.class),
	
	ATTACH("Attach", SessionType.AGENT, AttachConnectionParameters.class),
	
	FILE("jstack", SessionType.FILE, FileChooserParameters.class),
	
	SESSION("Saved session", SessionType.AGENT_CAPTURE, FileChooserParameters.class);
	
	private String description;
	
	private SessionType sessionType;
	
	private Class<? extends ConnectionParameterFrame> parameterDialogClass;

	private ConnectionType(String description, SessionType sessionType,
			Class<? extends ConnectionParameterFrame> parameterDialogClass) {
		this.description = description;
		this.sessionType = sessionType;
		this.parameterDialogClass = parameterDialogClass;
	}

	public String getDescription() {
		return description;
	}

	public SessionType getSessionType() {
		return sessionType;
	}

	public Class<? extends ConnectionParameterFrame> getParameterDialogClass() {
		return parameterDialogClass;
	}
}
