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
package io.djigger.ui.instrumentation;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class TransactionDashboard extends JPanel {
	
	private JPanel chartPanel;
		
	private List<MonitoringChart> charts = new ArrayList<MonitoringChart>();

	public TransactionDashboard() {
		super(new GridLayout());
			
		chartPanel = new JPanel(new GridLayout(1,2));
		
		charts.add(new AverageResponseTimesChart());
		charts.add(new ThroughputChart());
		
		for(MonitoringChart chart:charts) {
			chartPanel.add(chart);
		}
		
		add(chartPanel);
	}
	
	public void update() {
		updateCharts();
	}
	
	public void updateCharts() {
//		if(parent.getEngine().getTransactionPoller()!=null) {
//			Map<TransactionIndex, List<TransactionBundle>> bundles = parent.getEngine().getTransactionPoller().getTransactionBundles();
//			for(MonitoringChart chart:charts) {
//				chart.load(bundles);
//			}
//		}
	}

}
