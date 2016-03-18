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
package io.djigger.ui.analyzer;

import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class TransactionAnalyzerFrame extends JPanel {
	
	private Session main;
		
	private JFrame frame;
	
	private  AnalyzerGroupPane analyzerGroupPane;
	
	private StoreFilter filter;
	
	private final InstrumentationStatisticsCache statisticsCache;
	
	private final NodePresentationHelper presentationHelper;

	public TransactionAnalyzerFrame(Session main, StoreFilter filter) {
		super();
		this.main = main;
		this.filter = filter;
		
		try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }

        frame = new JFrame("dJigger - Transaction Details");
        frame.setPreferredSize(new Dimension(1300,700));

		statisticsCache = new InstrumentationStatisticsCache(main.getStore());
		statisticsCache.setStoreFilter(filter);
		statisticsCache.reload();
		
		presentationHelper = new NodePresentationHelper(statisticsCache);

        analyzerGroupPane = new AnalyzerGroupPane(main, presentationHelper);
        //add(analyzerGroupPane);
        
        frame.add(analyzerGroupPane);
        frame.pack();
        frame.setVisible(true);

        analyzerGroupPane.initialize();
        analyzerGroupPane.setStoreFilter(filter);
        analyzerGroupPane.refresh();
	}

}
