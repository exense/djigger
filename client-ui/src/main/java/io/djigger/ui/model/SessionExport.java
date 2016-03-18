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

package io.djigger.ui.model;

import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.store.Store;
import io.djigger.ui.Session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class SessionExport implements Serializable {

	private static final long serialVersionUID = 3414592768882821440L;

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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
			} else if (o instanceof ThreadInfo || o instanceof InstrumentationSample) {
				return memoryOptimizedRead(file);
			} else {
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static SessionExport memoryOptimizedRead(File file) {
		ArrayList<ThreadInfo> threads = new ArrayList<ThreadInfo>();
		ArrayList<InstrumentationSample> samples = new ArrayList<InstrumentationSample>();
		try {
			ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			Object o;
			while((o = stream.readObject())!=null) {
				if(o instanceof ThreadInfo) {
					threads.add((ThreadInfo)o);					
				} else if (o instanceof InstrumentationSample) {
					samples.add((InstrumentationSample)o);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
