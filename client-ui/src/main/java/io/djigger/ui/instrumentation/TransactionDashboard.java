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
