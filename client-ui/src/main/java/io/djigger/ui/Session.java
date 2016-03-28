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

import io.djigger.client.AgentFacade;
import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.client.JMXClientFacade;
import io.djigger.db.client.StoreClient;
import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.parser.Parser;
import io.djigger.parser.Parser.Format;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.SessionConfiguration.SessionParameter;
import io.djigger.ui.agentcontrol.SessionControlPane;
import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.instrumentation.InstrumentationPane;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.SessionExport;
import io.djigger.ui.storebrowser.StoreBrowserPane;
import io.djigger.ui.threadselection.ThreadSelectionPane;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.JarFile;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.sun.tools.attach.VirtualMachine;


public class Session extends JPanel implements FacadeListener {
	
	private final SessionConfiguration config;
		
    private Facade facade;
    
    private StoreClient storeClient;

    private final JSplitPane splitPane1;

	private final JSplitPane splitPane;

    private final AnalyzerGroupPane analyzerGroupPane;

    private final Store store;

    private final InstrumentationStatisticsCache statisticsCache;

    private final ArgumentParser options;

    private final InstrumentationPane instrumentationPane;
    
    private final StoreBrowserPane storeBrowserPane;

    private final ThreadSelectionPane threadSelectionPane;
    
	protected final NodePresentationHelper presentationHelper;
	
	private final MainFrame main;
	
	private boolean active;

    public Session(SessionConfiguration config, MainFrame main) {
		super(new BorderLayout());
		this.config = config;
		this.main = main;
		this.options = main.getOptions();
		
		store = new Store();
		statisticsCache = new InstrumentationStatisticsCache(store);
		presentationHelper = new NodePresentationHelper(statisticsCache);
        
        if(config.getType() == SessionType.AGENT) {
        	final Properties prop = new Properties();
        	if(config.getParameters().containsKey(SessionParameter.PROCESSID)) {        		
        		VirtualMachine vm;

				try {
					final ServerSocket s = new ServerSocket(0);
					int port = s.getLocalPort();
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							Socket socket;
							try {
								socket = s.accept();
								createFacade(prop, socket);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								try {
									s.close();
								} catch (IOException e) {}
							}
						}
					}).start();
					
					vm = VirtualMachine.attach (config.getParameters().get(SessionParameter.PROCESSID));
					
					InputStream is = getClass().getClassLoader().getResourceAsStream("agent.jar");
					File agentJar = File.createTempFile("agent-"+UUID.randomUUID(),".jar");
					try {
						FileOutputStream os = new FileOutputStream(agentJar);		
						byte[] buffer = new byte[1024];
			            int bytesRead;
			            while((bytesRead = is.read(buffer)) !=-1){
			                os.write(buffer, 0, bytesRead);
			            }
			            is.close();
			            os.flush();
			            os.close();
			      
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					vm.loadAgent(agentJar.getAbsolutePath(),"host:"+Inet4Address.getLocalHost().getHostName()+",port:"+port);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	} else {        		
        		prop.put("host", config.getParameters().get(SessionParameter.HOSTNAME));
        		prop.put("port", config.getParameters().get(SessionParameter.PORT));
        		facade = new AgentFacade(prop);
        		facade.addListener(this);
        		try {
					facade.connect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        } else if (config.getType() == SessionType.JMX) {
        	Properties prop = new Properties();
        	prop.put("host", config.getParameters().get(SessionParameter.HOSTNAME));
        	prop.put("port", config.getParameters().get(SessionParameter.PORT));
        	prop.put("username", config.getParameters().get(SessionParameter.USERNAME));
        	prop.put("password", config.getParameters().get(SessionParameter.PASSWORD));
			facade = new JMXClientFacade(prop, true);
			facade.addListener(this);
    		try {
				facade.connect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			facade = null;
		}


        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        threadSelectionPane = new ThreadSelectionPane(this);
        splitPane1.add(threadSelectionPane);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);        
        
        analyzerGroupPane = new AnalyzerGroupPane(this, presentationHelper);
        splitPane.add(analyzerGroupPane);

        instrumentationPane = new InstrumentationPane(this, presentationHelper);
        splitPane.add(instrumentationPane);

        if(config.getType() == SessionType.AGENT || config.getType() == SessionType.AGENT_CAPTURE ) {
        	instrumentationPane.setVisible(true);
        } else {
        	instrumentationPane.setVisible(false);
        }

        splitPane1.add(splitPane);
        splitPane1.setDividerLocation(300);

        add(splitPane1);

        if(config.getType() == SessionType.STORE) {
        	storeBrowserPane = new StoreBrowserPane(this);
        	add(storeBrowserPane, BorderLayout.PAGE_START);
        } else {
        	storeBrowserPane = null;
        }
        
        if(config.getType() == SessionType.AGENT || config.getType() == SessionType.JMX) {
        	add(new SessionControlPane(this),BorderLayout.PAGE_END);
        }

        threadSelectionPane.initialize();
        analyzerGroupPane.initialize();

    	splitPane.setDividerLocation(0.7);
    }
    
    private void createFacade(Properties props, Socket socket) {
    	try {
			facade = new AgentFacade(props, socket);
			facade.addListener(this);
    		facade.connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() throws Exception {
    	if(getSessionType()==SessionType.AGENT) {
    		//facade.connect();
    	} else if(getSessionType()==SessionType.JMX) {    		
    		//facade.connect();
    	} else if(getSessionType()==SessionType.STORE) {
    		storeClient = new StoreClient();
    		storeClient.connect(config.getParameters().get(SessionParameter.HOSTNAME));
    	} else if (getSessionType()==SessionType.FILE) {
    		final File file = new File(config.getParameters().get(SessionParameter.FILE));
    		MonitoredExecution execution = new MonitoredExecution(main.getFrame(), "Parsing threaddumps... Please wait.", new MonitoredExecutionRunnable() {
    			@Override
    			public void run(MonitoredExecution execution) {
    				try {
    	    			List<ThreadInfo> dumps = parseThreadDumpFile(file);
    	    			threadInfosReceived(dumps);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		});
    		execution.run();
    		refreshAll();
    	} else if (getSessionType()==SessionType.AGENT_CAPTURE) {
    		final File file = new File(config.getParameters().get(SessionParameter.FILE));
    		MonitoredExecution execution = new MonitoredExecution(main.getFrame(), "Opening session... Please wait.", new MonitoredExecutionRunnable() {
    			@Override
    			public void run(MonitoredExecution execution) {
    				try {
    					SessionExport export = SessionExport.read(file);
    					store.addThreadInfos(export.getStore().queryThreadDumps(null));
    					store.addCaptures(export.getStore().queryCaptures(0, Long.MAX_VALUE));
    					store.addInstrumentationSamples(export.getStore().queryInstrumentationSamples(null));
    					store.getSubscriptions().addAll(export.getStore().getSubscriptions());
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		});
    		execution.run();
    		refreshAll();
    	}
    }
    
    private List<ThreadInfo> parseThreadDumpFile(File file) throws IOException {
        Format format = Parser.detectFormat(file);
        Parser parser = new Parser(format);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        List<ThreadInfo> threadDumps = parser.parse(reader);
        reader.close();
        
        return threadDumps;
    }

    public MainFrame getMain() {
		return main;
	}

	public SessionConfiguration getConfig() {
		return config;
	}

	public enum SessionType {    	
        FILE("Thread dumps files"),

        AGENT("Agent sessions"),
        
        JMX("JMX Connection"),

        AGENT_CAPTURE("Saved agent sessions"),
        
        STORE("Store");
        
        private final String description;

		private SessionType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
    }

    public SessionType getSessionType() {
        return config.getType();
    }

    public Store getStore() {
        return store;
    }
    
    public InstrumentationStatisticsCache getStatisticsCache() {
    	return statisticsCache;
    }

    public void refreshAll() {
    	store.processBuffers();
    	statisticsCache.reload();
    	threadSelectionPane.refresh();
    	analyzerGroupPane.refresh();
    	instrumentationPane.refresh();
    }

    public void onThreadSelection(StoreFilter filter) {
    	statisticsCache.setStoreFilter(filter);
    	statisticsCache.reload();
    	
    	analyzerGroupPane.setStoreFilter(filter);
    	analyzerGroupPane.refresh();
    	
    	instrumentationPane.setStoreFilter(filter);
    	instrumentationPane.refresh();
    }

    public void clear() {
    	store.clear();
    	refreshAll();
    }

	public ArgumentParser getOptions() {
		return options;
	}

	public InstrumentationPane getInstrumentationPane() {
		return instrumentationPane;
	}
	
	Capture currentCapture;
	public void setSampling(boolean state) {
		if(facade!=null) {
			facade.setSampling(state);
			if(state) {
				currentCapture = new Capture(facade.getSamplingInterval());
				store.addOrUpdateCapture(currentCapture);
			} else {
				currentCapture.setEnd(System.currentTimeMillis());
				store.addOrUpdateCapture(currentCapture);
			}
		}
	}
	
	public void setSamplingInterval(int interval) {
		if(facade!=null) {
			facade.setSamplingInterval(interval);
			if(facade.isSampling()) {
				currentCapture.setEnd(System.currentTimeMillis());
				store.addOrUpdateCapture(currentCapture);
				currentCapture = new Capture(facade.getSamplingInterval());
				store.addOrUpdateCapture(currentCapture);
			}
		}
	}

	public void addSubscription(InstrumentSubscription subscription) {
		store.addSubscription(subscription);
		if(facade!=null) {
			facade.addInstrumentation(subscription);
		}
		instrumentationPane.setVisible(true);
		if(splitPane.getHeight()-splitPane.getDividerLocation()<100) {
			splitPane.setDividerLocation(0.7);
		}
		instrumentationPane.refresh();
		
	}

	public void removeSubscription(InstrumentSubscription subscription) {
		store.removeSubscription(subscription);
		if(facade!=null) {
			facade.removeInstrumentation(subscription);
		}
	}

	public void startCapture(Capture capture) {
		store.addOrUpdateCapture(capture);
	}

	public void stopCapture(Capture capture) {
		store.addOrUpdateCapture(capture);
	}

	public AnalyzerGroupPane getAnalyzerGroupPane() {
		return analyzerGroupPane;
	}

    public String getName() {
		return config.getName();
	}

	@Override
	public String toString() {
		return config.getName();
	}
	
	public void close() {
		if(facade!=null) {
			facade.destroy();			
		}
	}
	
	@Override
	public void threadInfosReceived(List<ThreadInfo> threads) {
		if(!main.isOutOfMemoryPreventionActive()) {
	        store.addThreadInfos(threads);
		} else {
			System.out.println("Ignoring incoming message to prevent JVM from OutOfMemory!");
		}
	}

	@Override
	public void instrumentationSamplesReceived(
			List<InstrumentationSample> samples) {
		if(!main.isOutOfMemoryPreventionActive()) {
	         store.addInstrumentationSamples(samples);
		} else {
			System.out.println("Ignoring incoming message to prevent JVM from OutOfMemory!");
		}
	}

	@Override
	public void connectionClosed() {
		active = false;
		main.handleSessionEvent(this, SessionEvent.CONNECTION_CLOSED);
	}

	@Override
	public void connectionEstablished() {
		active = true;
		main.handleSessionEvent(this, SessionEvent.CONNECTION_ESTABLISHED);
	}

	public boolean isActive() {
		return active;
	}

	public StoreClient getStoreClient() {
		return storeClient;
	}
}
