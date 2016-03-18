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
package io.djigger.ui;

import io.djigger.ui.Session.SessionType;

import java.util.HashMap;

public class SessionConfiguration {
	
	private final String name;
	
	private final SessionType type;
	
	private final HashMap<SessionParameter, String> parameters = new HashMap<SessionParameter, String>();
	
	public SessionConfiguration(String name, SessionType type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public SessionType getType() {
		return type;
	}

	public HashMap<SessionParameter, String> getParameters() {
		return parameters;
	}

	public enum SessionParameter {
		PROCESSID, 
		
		HOSTNAME,
		
		PORT,
		
		USERNAME,
		
		PASSWORD,
		
		FILE;
	}

}
