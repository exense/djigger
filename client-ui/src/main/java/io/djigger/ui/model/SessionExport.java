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
package io.djigger.ui.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.store.Store;
import io.djigger.ui.Session;


public class SessionExport implements Serializable {

	private static final long serialVersionUID = 3414592768882821440L;
	
	private static final Logger logger = LoggerFactory.getLogger(SessionExport.class);

	private Store store;

	public SessionExport(Store store) {
		super();
		this.store = store;
	}

	public static synchronized void save(Session main, File file) {
		//if(HeapMonitor.isCloseToOutOfMemory()) {
		normalSave(main, file);
		//}
	}
	
	public static synchronized void memoryOptimizedSave(Session main, File file) {
		Store store = main.getStore();
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			for(ThreadInfo dump:store.queryThreadDumps(null)) {
				stream.writeObject(dump);
				stream.reset();
			}
			stream.flush();
		} catch (IOException e) {
			logger.error("Error whil saving session to file "+file, e);
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public static void normalSave(Session main, File file) {
		Store store = main.getStore();
		SessionExport sessionExport = new SessionExport(store);
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			stream.writeObject(sessionExport);
			stream.flush();
		} catch (IOException e) {
			logger.error("Error whil saving session to file "+file, e);
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}

	public static SessionExport read(File file) {
		
		return normalRead(file);
	}
	
	private static SessionExport normalRead(File file) {
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

			Object o = stream.readObject();
			if(o instanceof SessionExport) {
				return (SessionExport)o;
			} else if (o instanceof ThreadInfo || o instanceof InstrumentationEvent) {
				return memoryOptimizedRead(file);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error while reading session from file "+file, e);
			return null;
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}
	
	private static SessionExport memoryOptimizedRead(File file) {
		ArrayList<ThreadInfo> threads = new ArrayList<ThreadInfo>();
		ArrayList<InstrumentationEvent> samples = new ArrayList<InstrumentationEvent>();
		
		ObjectInputStream stream=null;
		try {
			stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			Object o;
			while((o = stream.readObject())!=null) {
				if(o instanceof ThreadInfo) {
					threads.add((ThreadInfo)o);					
				} else if (o instanceof InstrumentationEvent) {
					samples.add((InstrumentationEvent)o);
				}
			}
		} catch (Exception e) {
			logger.error("Error while reading session from file "+file, e);
		} finally {
			if(stream!=null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
		
		Store store = new Store();
		store.addThreadInfos(threads);
		store.addInstrumentationSamples(samples);
		store.processBuffers();
		SessionExport export = new SessionExport(store);
		return export;
	}

	public Store getStore() {
		return store;
	}
}
