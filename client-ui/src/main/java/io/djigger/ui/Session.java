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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import io.djigger.ui.analyzer.AnalyzerPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.djigger.agent.InstrumentationError;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.client.AgentFacade;
import io.djigger.client.Facade;
import io.djigger.client.FacadeListener;
import io.djigger.client.JMXClientFacade;
import io.djigger.client.JstackLogTailFacade;
import io.djigger.client.ProcessAttachFacade;
import io.djigger.client.mbeans.MetricCollectionConfiguration;
import io.djigger.db.client.StoreClient;
import io.djigger.model.Capture;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.mbeans.MBeanCollectorConfiguration;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;
import io.djigger.samplig.PseudoInstrumentationEventsGenerator;
import io.djigger.samplig.PseudoInstrumentationEventsGenerator.Listener;
import io.djigger.samplig.SequenceGenerator;
import io.djigger.store.Store;
import io.djigger.store.StoreCollection;
import io.djigger.store.StoreCollection.StoreCollectionListener;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.SessionConfiguration.SessionParameter;
import io.djigger.ui.agentcontrol.SessionControlPane;
import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.common.Closeable;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.extensions.java.JavaBridge;
import io.djigger.ui.instrumentation.InstrumentationEventPane;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.InstrumentationEventWrapper;
import io.djigger.ui.model.PseudoInstrumentationEvent;
import io.djigger.ui.model.SessionExport;
import io.djigger.ui.storebrowser.StoreBrowserPane;
import io.djigger.ui.threadselection.ThreadSelectionPane;


@SuppressWarnings("serial")
public class Session extends JPanel implements FacadeListener, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);

    private final SessionConfiguration config;

    private Facade facade;

    private StoreClient storeClient;

    private final JSplitPane splitPane;

    private final AnalyzerGroupPane analyzerGroupPane;

    private final Store stagingStore = new Store();

    private final Store store;

    private final InstrumentationStatisticsCache statisticsCache;

    private final ArgumentParser options;

    private final StoreBrowserPane storeBrowserPane;

    private final ThreadSelectionPane threadSelectionPane;

    private final SessionControlPane controlPane;

    protected final NodePresentationHelper presentationHelper;

    private final MainFrame main;

    private boolean active;

    private boolean showLineNumbers;

    private boolean calculatePseudoEvents;

    private StoreFilter currentStoreFilter;

    private final StoreCollection<RealNodePathWrapper> realNodePathCache;

    private final StoreCollection<RealNodePathWrapper> realNodePathCacheWithLineNumbers;

    private final StoreCollection<InstrumentationEventWrapper> instrumentationEventWrapperCache;
    
    private final List<InstrumentationError> instrumentationErrors = new ArrayList<>();

    public Session(SessionConfiguration config, MainFrame main) {
        super(new BorderLayout());
        this.config = config;
        this.main = main;
        this.options = main.getOptions();

        store = new Store();

        ReadWriteLock lock = new ReentrantReadWriteLock();

        realNodePathCache = new StoreCollection<>(lock);
        realNodePathCacheWithLineNumbers = new StoreCollection<>(lock);
        instrumentationEventWrapperCache = new StoreCollection<>(lock);

        store.getThreadInfos().setListener(new StoreCollectionListener<ThreadInfo>() {

            @Override
            public void onAdd(ThreadInfo entry) {
                realNodePathCache.add(JavaBridge.toRealNodePath(entry, false));
                realNodePathCacheWithLineNumbers.add(JavaBridge.toRealNodePath(entry, true));
            }

            @Override
            public void onClear() {
                realNodePathCache.clear();
                realNodePathCacheWithLineNumbers.clear();
            }

            @Override
            public void onRemove(Filter<ThreadInfo> filter) {
                throw new RuntimeException("Not implemented!");
            }
        });

        store.getInstrumentationEvents().setListener(new StoreCollectionListener<InstrumentationEvent>() {

            @Override
            public void onAdd(InstrumentationEvent entry) {
                RealNodePathWrapper pathWrapper = null;
                if (entry instanceof InstrumentationEventWithThreadInfo) {
                    ThreadInfo threadInfo = ((InstrumentationEventWithThreadInfo) entry).getThreadInfo();
                    pathWrapper = JavaBridge.toRealNodePath(threadInfo, false);
                }
                InstrumentationEventWrapper wrapper = new InstrumentationEventWrapper(entry, pathWrapper);
                instrumentationEventWrapperCache.add(wrapper);
            }

            @Override
            public void onClear() {
                instrumentationEventWrapperCache.clear();
            }

            @Override
            public void onRemove(final Filter<InstrumentationEvent> filter) {
                if (filter != null) {
                    instrumentationEventWrapperCache.remove(new Filter<InstrumentationEventWrapper>() {
                        @Override
                        public boolean isValid(InstrumentationEventWrapper input) {
                            return filter.isValid(input.getEvent());
                        }
                    });
                }
            }
        });

        statisticsCache = new InstrumentationStatisticsCache();
        presentationHelper = new NodePresentationHelper(statisticsCache);

        facade = createFacade(config);

        if (facade != null) {
            facade.addListener(this);

            // TODO expose the metric collection parameters to the GUI
            MetricCollectionConfiguration metricCollectionConfig = new MetricCollectionConfiguration();
            MBeanCollectorConfiguration mBeanCollectionConfig = new MBeanCollectorConfiguration();
            mBeanCollectionConfig.addMBeanAttribute("java.lang:*");
            metricCollectionConfig.setmBeans(mBeanCollectionConfig);
            facade.setMetricCollectionConfiguration(metricCollectionConfig);
        }

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        threadSelectionPane = new ThreadSelectionPane(this);
        splitPane.add(threadSelectionPane);

        analyzerGroupPane = new AnalyzerGroupPane(this, presentationHelper);
        splitPane.add(analyzerGroupPane);
        splitPane.setDividerLocation(300);
        add(splitPane);

        if (config.getType() == SessionType.STORE) {
            storeBrowserPane = new StoreBrowserPane(this);
            add(storeBrowserPane, BorderLayout.PAGE_START);
        } else {
            storeBrowserPane = null;
        }

        controlPane = new SessionControlPane(this);
        add(controlPane, BorderLayout.PAGE_END);

        threadSelectionPane.initialize();
        analyzerGroupPane.initialize();
    }

    private Facade createFacade(SessionConfiguration config) {
        Facade facade;
        if (config.getType() == SessionType.AGENT) {
            final Properties prop = new Properties();
            if (config.getParameters().containsKey(SessionParameter.PROCESSID)) {
                prop.put(Facade.Parameters.PROCESS_ID, config.getParameters().get(SessionParameter.PROCESSID));
                facade = new ProcessAttachFacade(prop, false);
            } else {
                prop.put(Facade.Parameters.HOST, config.getParameters().get(SessionParameter.HOSTNAME));
                prop.put(Facade.Parameters.PORT, config.getParameters().get(SessionParameter.PORT));
                facade = new AgentFacade(prop, false);
            }
        } else if (config.getType() == SessionType.FILE) {
            Properties prop = new Properties();
            prop.put(Facade.Parameters.FILE, config.getParameters().get(SessionParameter.FILE));
            prop.put(Facade.Parameters.START_AT_FILE_BEGIN, "true");

            facade = new JstackLogTailFacade(prop, false);
        } else if (config.getType() == SessionType.JMX) {
            Properties prop = new Properties();
            prop.put(Facade.Parameters.HOST, config.getParameters().get(SessionParameter.HOSTNAME));
            prop.put(Facade.Parameters.PORT, config.getParameters().get(SessionParameter.PORT));
            prop.put(Facade.Parameters.USERNAME, config.getParameters().get(SessionParameter.USERNAME));
            prop.put(Facade.Parameters.PASSWORD, config.getParameters().get(SessionParameter.PASSWORD));
            facade = new JMXClientFacade(prop, false);
        } else {
            facade = null;
        }
        return facade;
    }

    public void start() throws Exception {
        if (facade != null) {
            MonitoredExecution execution = new MonitoredExecution(main.getFrame(), "Connecting... Please wait.", new MonitoredExecutionRunnable() {
                @Override
                public void run(MonitoredExecution execution) throws Exception {
                    facade.connect();
                }
            });
            execution.run();
            refreshAll();
        } else {
            if (getSessionType() == SessionType.STORE) {
                storeClient = new StoreClient();

                Map<SessionParameter, String> params = config.getParameters();
                String hostname = params.get(SessionParameter.HOSTNAME);
                int port;
                try {
                    port = Integer.parseInt(params.get(SessionParameter.PORT));
                } catch (NumberFormatException e) {
                    port = 27017;
                }
                storeClient.connect(hostname, port, params.get(SessionParameter.USERNAME), params.get(SessionParameter.PASSWORD));
            } else if (getSessionType() == SessionType.AGENT_CAPTURE) {
                final File file = new File(config.getParameters().get(SessionParameter.FILE));
                MonitoredExecution execution = new MonitoredExecution(main.getFrame(), "Opening session... Please wait.", new MonitoredExecutionRunnable() {
                    @Override
                    public void run(MonitoredExecution execution) {
                        SessionExport export = SessionExport.read(file);
                        export.getStore().drainTo(store);
                    }
                });
                execution.run();
                refreshAll();
            }
        }
    }

    public void configure() {
    }

    public void setupInitialState() {
        String query = config.getParameters().get(SessionParameter.QUERY);
        if (query != null) {
            storeBrowserPane.setQuery(query);
        }

        String preset = config.getParameters().get(SessionParameter.TIMEINTERVAL_PRESETS);
        if (preset!=null && !preset.equals("")) {
            storeBrowserPane.setSelectedPreset(preset);
        }
        String start = config.getParameters().get(SessionParameter.TIMEINTERVAL_START);
        if (start != null) {
            String end = config.getParameters().get(SessionParameter.TIMEINTERVAL_END);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            try {
                storeBrowserPane.setTimeinterval(format.parse(start), format.parse(end));
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse timeinterval " + start + " - " + end, e);
            }
            storeBrowserPane.search();
        }

        String calculatePseudoEvents = config.getParameters().get(SessionParameter.CALCULATE_PSEUDO_EVENTS);
        if (calculatePseudoEvents != null && Boolean.parseBoolean(calculatePseudoEvents)) {
            this.controlPane.selectPseudoEventsButton();
        }

        String initialPaneSelection = config.getParameters().get(SessionParameter.INITIAL_PANE_SELECTION);
        if (initialPaneSelection != null) {
            analyzerGroupPane.selectTabByName(initialPaneSelection);
        }

        String threadFilter = config.getParameters().get(SessionParameter.THREAD_FILTER);
        if (threadFilter != null) {
            threadSelectionPane.setThreadnameFilter(threadFilter);
        }

        setAnalyserPaneFilters("Tree view",
                config.getParameters().get(SessionParameter.TREE_STACK_FILTER),
                config.getParameters().get(SessionParameter.TREE_NODE_FILTER));

        setAnalyserPaneFilters("Reverse tree view",
                config.getParameters().get(SessionParameter.RTREE_STACK_FILTER),
                config.getParameters().get(SessionParameter.RTREE_NODE_FILTER));

        setAnalyserPaneFilters("Block view",
                config.getParameters().get(SessionParameter.BLOCK_STACK_FILTER),
                config.getParameters().get(SessionParameter.BLOCK_NODE_FILTER));

        setAnalyserPaneFilters("Reverse block view",
                config.getParameters().get(SessionParameter.RBLOCK_STACK_FILTER),
                config.getParameters().get(SessionParameter.RBLOCK_NODE_FILTER));

        String eventListQuery = config.getParameters().get(SessionParameter.EVENT_LIST_QUERY);
        if (eventListQuery != null) {
            InstrumentationEventPane pane = (InstrumentationEventPane) analyzerGroupPane.getTabByName("Events");
            pane.setQueryAndSearch(eventListQuery);
        }

    }

    private void setAnalyserPaneFilters(String paneName, String stackFilter, String nodeFilter) {
        AnalyzerPane pane = null;
        try {
            pane = (AnalyzerPane) analyzerGroupPane.getTabByName(paneName);
        } catch (RuntimeException e) {
            logger.warn("Could not apply filters for tab " + paneName + ". The tab was not found or is not supported.");
        }
        if (pane != null) {
            if (stackFilter != null) {
                pane.setStacktraceFilter(stackFilter);
            }
            if (nodeFilter != null) {
                pane.setNodeFilter(nodeFilter);
            }
        }
    }

    public SessionConfiguration cloneConfiguration() {
        SessionConfiguration newSessionConfig = config.clone();
        HashMap<SessionParameter, String> newParams = newSessionConfig.getParameters();
        //Add Time frame config
        if (config.getType() == Session.SessionType.STORE) {
            String query = storeBrowserPane.getQuery();
            if (query != null) {
                newParams.put(SessionConfiguration.SessionParameter.QUERY, query);
            }
            StoreBrowserPane.DatePresets selectedDatePresets = storeBrowserPane.getSelectedDatePresets();
            newParams.put(SessionParameter.TIMEINTERVAL_PRESETS,selectedDatePresets.toString());
            if (selectedDatePresets.label.equals(StoreBrowserPane.DatePresets.CUSTOM.label)) {
                Date start = storeBrowserPane.getFromDate();
                if (start != null) {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    newParams.put(SessionParameter.TIMEINTERVAL_START, format.format(start));
                    Date end = storeBrowserPane.getToDate();
                    if (end != null) {
                        newParams.put(SessionParameter.TIMEINTERVAL_END, format.format(end));
                    }
                }
            }
        }
        //Add Thread Filter
        String threadnameFilter = threadSelectionPane.getThreadnameFilter();
        if (threadnameFilter != null) {
            newParams.put(SessionParameter.THREAD_FILTER, threadnameFilter);
        }
        //Add AnalyserPanes filters
        addAnalyserPaneFiltersParameters(newParams,"Tree view",
                SessionParameter.TREE_STACK_FILTER, SessionParameter.TREE_NODE_FILTER);

        addAnalyserPaneFiltersParameters(newParams,"Reverse tree view",
                SessionParameter.RTREE_STACK_FILTER, SessionParameter.RTREE_NODE_FILTER);

        addAnalyserPaneFiltersParameters(newParams,"Block view",
                SessionParameter.BLOCK_STACK_FILTER, SessionParameter.BLOCK_NODE_FILTER);

        addAnalyserPaneFiltersParameters(newParams,"Reverse block view",
                SessionParameter.RBLOCK_STACK_FILTER, SessionParameter.RBLOCK_NODE_FILTER);

        //Add event config
        InstrumentationEventPane ePane = (InstrumentationEventPane) analyzerGroupPane.getTabByName("Events");
        String eventFilter = ePane.getEventFilter();
        if (eventFilter != null) {
            newParams.put(SessionParameter.EVENT_LIST_QUERY, eventFilter);
        }
        return newSessionConfig;
    }

    private void addAnalyserPaneFiltersParameters(HashMap<SessionParameter, String> params, String paneName,
                                                  SessionParameter stackFilterParam, SessionParameter nodeFilterParam) {
        AnalyzerPane pane = null;
        try {
            pane = (AnalyzerPane) analyzerGroupPane.getTabByName(paneName);
        } catch (RuntimeException e) {
            logger.warn("Could not get filters for tab " + paneName + ". The tab was not found or is not supported.");
        }
        if (pane != null) {
            String stackFilter = pane.getStacktraceFilter();
            String nodeFilter = pane.getNodeFilter();
            if (stackFilter != null) {
                params.put(stackFilterParam, stackFilter);
            }
            if (nodeFilter != null) {
                params.put(nodeFilterParam, nodeFilter);
            }
        }
    }

    public Facade getFacade() {
        return facade;
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

    public StoreBrowserPane getStoreBrowserPane() {
        return storeBrowserPane;
    }

    public InstrumentationStatisticsCache getStatisticsCache() {
        return statisticsCache;
    }

    public void refreshAll() {
        stagingStore.drainTo(store);

        clearPseudoEvents();
        if (calculatePseudoEvents) {
            generatePseudoInstrumentationEvents();
        }

        reloadStatisticsCache();

        threadSelectionPane.refresh();
        threadSelectionPane.selectionChanged();
        refreshAnalyzerGroupPane();
    }

    void refreshAnalyzerGroupPane() {
        List<RealNodePathWrapper> realNodePaths = queryRealNodePaths();
        analyzerGroupPane.setSamples(realNodePaths);
        analyzerGroupPane.refresh();
    }

    private List<RealNodePathWrapper> queryRealNodePaths() {
        Filter<RealNodePathWrapper> filter = getRealNodePathWrapperFilter();

        List<RealNodePathWrapper> realNodePaths;
        if (showLineNumbers) {
            realNodePaths = realNodePathCacheWithLineNumbers.query(filter);
        } else {
            realNodePaths = realNodePathCache.query(filter);
        }
        return realNodePaths;
    }

    private Filter<RealNodePathWrapper> getRealNodePathWrapperFilter() {
        Filter<RealNodePathWrapper> filter = new Filter<RealNodePathWrapper>() {
            @Override
            public boolean isValid(RealNodePathWrapper input) {
                return currentStoreFilter == null || currentStoreFilter.getThreadInfoFilter().isValid(input.getThreadInfo());
            }
        };
        return filter;
    }

    private void generatePseudoInstrumentationEvents() {
    	// Query all paths. Ignore the current filter here as the filter will be applied on the pseudo events
        List<RealNodePathWrapper> realNodePaths = realNodePathCache.query(null);

        SequenceGenerator sequenceGenerator = new SequenceGenerator();
        List<io.djigger.aggregation.Thread> threads = sequenceGenerator.buildThreads(realNodePaths);

        PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
            @Override
            public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
                store.getInstrumentationEvents().add(event);
            }
        }, getSubscriptions());
        a.generateApproximatedEvents(threads);
    }

    private void clearPseudoEvents() {
        store.getInstrumentationEvents().remove(new Filter<InstrumentationEvent>() {
            @Override
            public boolean isValid(InstrumentationEvent input) {
                return input instanceof PseudoInstrumentationEvent;
            }
        });
    }

    public void showLineNumbers(boolean show) {
        showLineNumbers = show;
        refreshAnalyzerGroupPane();
    }

    public void calculatePseudoEvents(boolean show) {
        calculatePseudoEvents = show;
        refreshAll();
    }

    public void onThreadSelection(StoreFilter filter) {
        this.currentStoreFilter = filter;

        reloadStatisticsCache();

        refreshAnalyzerGroupPane();
    }

    private void reloadStatisticsCache() {
        List<InstrumentationEventWrapper> samples = queryInstrumentationEventWrappers();
        statisticsCache.reload(samples);
    }

    public StoreCollection<InstrumentationEventWrapper> getInstrumentationEventWrapperCache() {
        return instrumentationEventWrapperCache;
    }

    private List<InstrumentationEventWrapper> queryInstrumentationEventWrappers() {
        List<InstrumentationEventWrapper> samples = instrumentationEventWrapperCache.query(new Filter<InstrumentationEventWrapper>() {
            @Override
            public boolean isValid(InstrumentationEventWrapper input) {
                return currentStoreFilter == null || currentStoreFilter.getInstrumentationEventsFilter().isValid(input.getEvent());
            }
        });
        return samples;
    }

    public void clear() {
        store.clear();
        refreshAll();
    }

    public ArgumentParser getOptions() {
        return options;
    }

    Capture currentCapture;

    public void setSampling(boolean state) {
        if (facade != null) {
            facade.setSampling(state);
            if (state) {
                currentCapture = new Capture(facade.getSamplingInterval());
                addOrUpdateCapture(currentCapture);
            } else {
                currentCapture.setEnd(System.currentTimeMillis());
                addOrUpdateCapture(currentCapture);
            }
        }
    }

    public void setSamplingInterval(int interval) {
        if (facade != null) {
            facade.setSamplingInterval(interval);
            if (facade.isSampling()) {
                currentCapture.setEnd(System.currentTimeMillis());
                addOrUpdateCapture(currentCapture);
                currentCapture = new Capture(facade.getSamplingInterval());
                addOrUpdateCapture(currentCapture);
            }
        }
    }

    private void addOrUpdateCapture(Capture capture) {
        boolean update = false;
        for (Capture c : store.getCaptures().queryAll()) {
            if (c.getStart() == capture.getStart()) {
                c.setEnd(capture.getEnd());
                update = true;
                break;
            }
        }
        if (!update) {
            store.getCaptures().add(capture);
        }
    }

    protected final Set<InstrumentSubscription> subscriptions = new HashSet<>();

    public Set<InstrumentSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(InstrumentSubscription subscription) {
        if (facade != null && facade.hasInstrumentationSupport()) {
            facade.addInstrumentation(subscription);
            subscriptions.add(subscription);
        }
        fireSubscriptionChangeEvent();
    }

    public void removeSubscription(InstrumentSubscription subscription) {
        if (facade != null && facade.hasInstrumentationSupport()) {
            facade.removeInstrumentation(subscription);
            subscriptions.remove(subscription);
            instrumentationErrors.removeIf(e->e.getSubscription().equals(subscription));
        }
        fireSubscriptionChangeEvent();
    }

    public final List<SessionListener> listeners = new ArrayList<>();

    public void addListener(SessionListener listener) {
        listeners.add(listener);
    }

    public void fireSubscriptionChangeEvent() {
        for (SessionListener sessionListener : listeners) {
            sessionListener.subscriptionChange();
        }
    }

    public void startCapture(Capture capture) {
        addOrUpdateCapture(capture);
    }

    public void stopCapture(Capture capture) {
        addOrUpdateCapture(capture);
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
        if (facade != null) {
            facade.destroy();
        }
    }

    @Override
    public void threadInfosReceived(List<ThreadInfo> threads) {
    	// Update the global thread id with the runtime id of the facade
    	threads.forEach(t->t.getGlobalId().setRuntimeId(getFacade().getConnectionId()));
        store.getThreadInfos().addAll(threads);
    }

    @Override
    public void instrumentationSamplesReceived(List<InstrumentationEvent> events) {
        store.getInstrumentationEvents().addAll(events);
    }

    @Override
    public void metricsReceived(List<Metric<?>> metrics) {
        store.getMetrics().addAll(metrics);
    }

    @Override
    public void connectionClosed() {
        active = false;
    }

    @Override
    public void connectionEstablished() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public StoreClient getStoreClient() {
        return storeClient;
    }

    public StoreFilter getStoreFilter() {
        return currentStoreFilter;
    }
    
	public List<InstrumentationError> getInstrumentationErrors() {
		return instrumentationErrors;
	}

	@Override
	public void instrumentationErrorReceived(InstrumentationError error) {
		logger.warn("Error while applying subscription "+error.getSubscription()+" on class "+error.getClassname(), error.getException());
		instrumentationErrors.add(error);
		for (SessionListener sessionListener : listeners) {
			sessionListener.instrumentationError(error);
		}
	}    
}
