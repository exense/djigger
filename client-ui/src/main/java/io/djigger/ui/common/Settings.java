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
package io.djigger.ui.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
	
	private static final Logger logger = LoggerFactory.getLogger(Settings.class);

	private final static String SETTINGS_FILENAME = "djigger.settings";
		
	private static Settings INSTANCE = new Settings();
	
	private Properties settings;
	
	private Settings() {
		super();
		
		settings = new Properties();
		try {
			settings.load(new FileInputStream(SETTINGS_FILENAME));
		} catch (FileNotFoundException e) {
			logger.error("Error while loading settings from file "+SETTINGS_FILENAME, e);
		} catch (IOException e) {
			logger.error("Error while loading settings from file "+SETTINGS_FILENAME, e);
		}
	}

	public static Settings getINSTANCE() {
		return INSTANCE;
	}
	
	public synchronized String getAsString(String key) {
		return settings.getProperty(key);
	}
	
	public synchronized void put(String key, Object value) {
		settings.put(key, value);
		try {
			settings.store(new FileOutputStream(SETTINGS_FILENAME), "");
		} catch (FileNotFoundException e) {
			logger.error("Error while saving settings to file "+SETTINGS_FILENAME, e);
		} catch (IOException e) {
			logger.error("Error while saving settings to file "+SETTINGS_FILENAME, e);
		}
	}
	
}
