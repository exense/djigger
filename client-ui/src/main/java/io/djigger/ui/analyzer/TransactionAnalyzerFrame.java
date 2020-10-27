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
package io.djigger.ui.analyzer;

import java.awt.Dimension;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.extensions.java.JavaBridge;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.InstrumentationEventWrapper;
import io.djigger.ui.model.PseudoInstrumentationEvent;

public class TransactionAnalyzerFrame extends JPanel {

    private Session main;

    private JFrame frame;

    private AnalyzerGroupPane analyzerGroupPane;

    private final InstrumentationStatisticsCache statisticsCache;

    private final NodePresentationHelper presentationHelper;

    public TransactionAnalyzerFrame(Session main, final InstrumentationEvent event) {
        super();
        this.main = main;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }

        final StoreFilter filter;
        if (event instanceof PseudoInstrumentationEvent) {
            filter = new StoreFilter(new Filter<ThreadInfo>() {

                @Override
                public boolean isValid(ThreadInfo dump) {
                    return (event.getGlobalThreadId() == dump.getGlobalId() &&
                        event.getStart() <= dump.getTimestamp() && event.getEnd() >= dump.getTimestamp());
                }
            }, new Filter<InstrumentationEvent>() {

                @Override
                public boolean isValid(InstrumentationEvent sample) {
                    return event.getTransactionID() != null && event.getTransactionID().equals(sample.getTransactionID());
                }
            }, null);
        } else {
            final String transactionID = event.getTransactionID();
            filter = new StoreFilter(new Filter<ThreadInfo>() {

                @Override
                public boolean isValid(ThreadInfo dump) {
                    return transactionID.equals(dump.getTransactionID());
                }
            }, new Filter<InstrumentationEvent>() {

                @Override
                public boolean isValid(InstrumentationEvent sample) {
                    return transactionID.equals(sample.getTransactionID());
                }
            }, null);
        }
        String transactionID = event.getTransactionID();

        frame = new JFrame("djigger - Transaction " + (transactionID != null ? transactionID.toString() : ""));
        frame.setPreferredSize(new Dimension(1300, 700));

        statisticsCache = new InstrumentationStatisticsCache();

        List<InstrumentationEventWrapper> events = main.getInstrumentationEventWrapperCache().query(new Filter<InstrumentationEventWrapper>() {

            @Override
            public boolean isValid(InstrumentationEventWrapper input) {
                return filter == null || filter.getInstrumentationEventsFilter().isValid(input.getEvent());
            }
        });

        statisticsCache.reload(events);

        presentationHelper = new NodePresentationHelper(statisticsCache);

        analyzerGroupPane = new AnalyzerGroupPane(main, presentationHelper);
        //add(analyzerGroupPane);

        frame.add(analyzerGroupPane);
        frame.pack();
        frame.setVisible(true);

        analyzerGroupPane.initialize();

        List<ThreadInfo> threads = main.getStore().getThreadInfos().query(filter.getThreadInfoFilter());
        List<RealNodePathWrapper> realNodePaths = JavaBridge.toRealNodePathList(threads, false);
        analyzerGroupPane.setSamples(realNodePaths);
        analyzerGroupPane.refresh();
    }

}
