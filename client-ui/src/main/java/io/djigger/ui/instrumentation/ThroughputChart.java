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

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class ThroughputChart extends MonitoringChart {
	
	private ChartPanel chartPanel;

	private TimeSeriesCollection dataset;

	public ThroughputChart() {
		super();

		dataset = new TimeSeriesCollection();
		XYLineAndShapeRenderer dot = new XYLineAndShapeRenderer();

		DateAxis xax = new DateAxis();
		NumberAxis yax = new NumberAxis("calls/min");

		XYPlot plot = new XYPlot(dataset, xax, yax, dot);

		JFreeChart chart = new JFreeChart(plot);

		chartPanel = new ChartPanel(chart);
		//chartPanel.setPreferredSize(new Dimension(300, 300));

		add(chartPanel);
	}

	protected void init() {		
		dataset.removeAllSeries();
	}
	
	protected void addSerie(InstrumentSubscription index, InstrumentationStatistics stats) {
		TimeSeries series1 = new TimeSeries(index.toString());
//		for(Sample bundle:stats.getSamples()) {
//			series1.addOrUpdate(new TimeSeriesDataItem(new Millisecond(new Date(bundle.getStart())),
//					bundle.getThroughput()));
//		}
		dataset.addSeries(series1);
	}
}
