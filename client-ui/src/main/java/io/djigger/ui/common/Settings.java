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
	
	public synchronized boolean getAsBoolean(String key, boolean default_) {
		String prop = settings.getProperty(key);
		if(prop!=null) {
			return Boolean.parseBoolean(prop);
		} else {
			return default_;
		}
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
