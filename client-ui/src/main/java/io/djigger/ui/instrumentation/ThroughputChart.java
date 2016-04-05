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
