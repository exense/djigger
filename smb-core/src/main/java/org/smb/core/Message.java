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
package org.smb.core;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = -8516543608547851386L;

	private final String type;

	private final Object content;

	public Message(String type, Object content) {
		super();
		this.type = type;
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public Object getContent() {
		return content;
	}

	public String getStringContent() {
		if(content instanceof String) {
			return (String)content;
		} else {
			return null;
		}
	}

	public Integer getIntegerContent() {
		if(content instanceof Integer) {
			return (Integer)content;
		} else {
			return null;
		}
	}
}
