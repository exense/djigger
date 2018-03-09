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

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.client.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


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

    StoreCollection<RealNodePathWrapper> realNodePathCache;

    StoreCollection<RealNodePathWrapper> realNodePathCacheWithLineNumbers;

    StoreCollection<InstrumentationEventWrapper> instrumentationEventWrapperCache;

    public Session(SessionConfiguration config, MainFrame main) {
        super(new BorderLayout());
        this.config = config;
        this.main = main;
        this.options = main.getOptions();

        store = new Store();

        realNodePathCache = new StoreCollection<>();
        realNodePathCacheWithLineNumbers = new StoreCollection<>();
        instrumentationEventWrapperCache = new StoreCollection<>();

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
                prop.put(ProcessAttachFacade.PROCESSID, config.getParameters().get(SessionParameter.PROCESSID));
                facade = new ProcessAttachFacade(prop, false);
            } else {
                prop.put("host", config.getParameters().get(SessionParameter.HOSTNAME));
                prop.put("port", config.getParameters().get(SessionParameter.PORT));
                facade = new AgentFacade(prop, false);
            }
        } else if (config.getType() == SessionType.FILE) {
            Properties prop = new Properties();
            prop.put(JstackLogTailFacade.FILE_PARAM, config.getParameters().get(SessionParameter.FILE));
            prop.put(JstackLogTailFacade.START_AT_FILE_BEGIN_PARAM, "true");

            facade = new JstackLogTailFacade(prop, false);
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

        String eventListQuery = config.getParameters().get(SessionParameter.EVENT_LIST_QUERY);
        if (eventListQuery != null) {
            InstrumentationEventPane pane = (InstrumentationEventPane) analyzerGroupPane.getTabByName("Events");
            pane.setQueryAndSearch(eventListQuery);
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
        List<RealNodePathWrapper> realNodePaths = queryRealNodePaths();

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

    Set<InstrumentSubscription> subscriptions = new HashSet<>();

    public Set<InstrumentSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(InstrumentSubscription subscription) {
        subscriptions.add(subscription);
        if (facade != null && facade.hasInstrumentationSupport()) {
            facade.addInstrumentation(subscription);
        }
        fireSubscriptionChangeEvent();
    }

    public void removeSubscription(InstrumentSubscription subscription) {
        subscriptions.remove(subscription);
        if (facade != null && facade.hasInstrumentationSupport()) {
            facade.removeInstrumentation(subscription);
        }
        fireSubscriptionChangeEvent();
    }

    public List<SessionListener> listeners = new ArrayList<>();

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

    public StoreFilter getStoreFilter() {
        return currentStoreFilter;
    }
}
