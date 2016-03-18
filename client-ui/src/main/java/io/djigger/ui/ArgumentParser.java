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
