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

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.client.AgentFacade;
import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.client.JMXClientFacade;
import io.djigger.client.ProcessAttachFacade;
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
import io.djigger.ui.common.Closeable;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.instrumentation.InstrumentationPane;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.SessionExport;
import io.djigger.ui.storebrowser.StoreBrowserPane;
import io.djigger.ui.threadselection.ThreadSelectionPane;


@SuppressWarnings("serial")
public class Session extends JPanel implements FacadeListener, Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(Session.class);
	
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
        
        facade = createFacade(config);
        
        if(facade!=null) {
    		facade.addListener(this);
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
        
        add(new SessionControlPane(this),BorderLayout.PAGE_END);

        threadSelectionPane.initialize();
        analyzerGroupPane.initialize();

    	splitPane.setDividerLocation(0.7);
    }

	private Facade createFacade(SessionConfiguration config) {
		Facade facade;
		if(config.getType() == SessionType.AGENT) {
        	final Properties prop = new Properties();
        	if(config.getParameters().containsKey(SessionParameter.PROCESSID)) {        		
        		prop.put(ProcessAttachFacade.PROCESSID, config.getParameters().get(SessionParameter.PROCESSID));
        		facade = new ProcessAttachFacade(prop, false);
        	} else {        		
        		prop.put("host", config.getParameters().get(SessionParameter.HOSTNAME));
        		prop.put("port", config.getParameters().get(SessionParameter.PORT));
        		facade = new AgentFacade(prop, false);
        	}
        } else if (config.getType() == SessionType.JMX) {
        	Properties prop = new Properties();
        	prop.put("host", config.getParameters().get(SessionParameter.HOSTNAME));
        	prop.put("port", config.getParameters().get(SessionParameter.PORT));
        	prop.put("username", config.getParameters().get(SessionParameter.USERNAME));
        	prop.put("password", config.getParameters().get(SessionParameter.PASSWORD));
			facade = new JMXClientFacade(prop, false);
		} else {
			facade = null;
		}
		return facade;
	}
    
    public void start() throws Exception {
    	if(facade!=null) {
    		facade.connect();
    	}
    	
    	if(getSessionType()==SessionType.STORE) {
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
    					logger.error("Error while parsing thread dumps from file "+file, e);
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
    					logger.error("Error while opening session from file "+file, e);
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
    
    public void showLineNumbers(boolean show) {
    	analyzerGroupPane.showLineNumbers(show);
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

    public String getSessionName() {
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
