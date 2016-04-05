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
package io.djigger.ui;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentParser {
	private HashMap<String, String> options = new HashMap<String, String>();
	private Pattern p = Pattern.compile("-(.+?)(?:=(.+?))?$");

	public ArgumentParser(String[] paramArrayOfString) {
		Matcher localMatcher = null;
		for (int i = 0; i < paramArrayOfString.length; i++) {
			if (!(localMatcher = this.p.matcher(paramArrayOfString[i])).find())
				continue;
			this.options.put(localMatcher.group(1).toLowerCase(),
					localMatcher.group(2));
		}
	}

	public boolean hasOption(String paramString) {
		return this.options.containsKey(paramString.toLowerCase());
	}

	public String getOption(String paramString) {
		return (String) this.options.get(paramString.toLowerCase());
	}
}
