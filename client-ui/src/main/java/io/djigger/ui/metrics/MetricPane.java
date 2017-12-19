package io.djigger.ui.metrics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import io.djigger.monitoring.java.model.Metric;
import io.djigger.ql.Filter;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.Dashlet;
import io.djigger.ui.metrics.MetricTreeModel.MetricNode;

public class MetricPane extends Dashlet {
	
	private final JTree metricTree;
	
	private MetricTreeModel metricTreeModel = new MetricTreeModel();
	
	private final TimeSeriesCollection dataset;
		
	private final Session session;
	
	private List<Metric<?>> metrics;
	
	public MetricPane(final Session session) {
		super(new BorderLayout());
		
		this.session = session;
		
		metricTree = new JTree(metricTreeModel);
		metricTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				updateChart();
			}
		});
		JScrollPane metricTreePane = new JScrollPane(metricTree);
		metricTreePane.setPreferredSize(new Dimension(400, 0));
		add(BorderLayout.WEST, metricTreePane);
		
		dataset = new TimeSeriesCollection();
		XYLineAndShapeRenderer dot = new XYLineAndShapeRenderer(true, false);
		
		DateAxis xax = new DateAxis();
		NumberAxis yax = new NumberAxis("");
		
		XYPlot plot = new XYPlot(dataset, xax, yax, dot);
		
		JFreeChart chart = new JFreeChart(plot);
		
		ChartPanel chartPanel = new ChartPanel(chart);
		add(BorderLayout.CENTER,chartPanel);	
		
		queryMetrics();
	}
	
	private void queryMetrics() {
		Store store = session.getStore();
		
		final StoreFilter storeFilter = this.session.getStoreFilter();
		Filter<Metric<?>> mergedFilter = new Filter<Metric<?>>() {

			@Override
			public boolean isValid(Metric<?> metric) {
				return (storeFilter==null||storeFilter.getMetricFilter().isValid(metric));
			}
		};
		
		metrics = store.getMetrics().query(mergedFilter);
		
		metricTreeModel = new MetricTreeModel(metrics);
		TreePath[] selectionBefore = metricTree.getSelectionPaths();
		Enumeration<TreePath> expandedDesc = metricTree.getExpandedDescendants(new TreePath(metricTree.getModel().getRoot()));
		
		metricTree.setModel(metricTreeModel);
		if(expandedDesc!=null) {
			while(expandedDesc.hasMoreElements()) {
				TreePath path = expandedDesc.nextElement();
				metricTree.expandPath(path);
			}
		}
		metricTree.setSelectionPaths(selectionBefore);
	}
	
	private void updateChart() {
		dataset.removeAllSeries();
		TreePath[] selectedPaths = metricTree.getSelectionModel().getSelectionPaths();
		for(TreePath path:selectedPaths) {
			Object[] reportNodePath = path.getPath();
			if(reportNodePath.length>1) {
				MetricNode firstNode = (MetricNode) reportNodePath[1];
			
				StringBuilder serieName = new StringBuilder();
				for(int i=1;i<reportNodePath.length;i++) {
					MetricNode node = (MetricNode) reportNodePath[i];
					serieName.append("/").append(node.name);
				}
				TimeSeries series1 = new TimeSeries(serieName.toString().replaceFirst("/", ""));
				
				for(Metric<?> m:metrics) {
					if(m.getName().equals(firstNode.name)) {
						if(reportNodePath.length>2) {
							Object value = m.getValue();
							if(value instanceof JsonValue) {
								JsonValue json = (JsonValue) value;
								for(int i=2;i<reportNodePath.length;i++) {
									MetricNode node = (MetricNode) reportNodePath[i];
									if(json instanceof JsonObject) {
										if(((JsonObject)json).containsKey(node.name)) {
											json = ((JsonObject)json).get(node.name);
										} else {
											json = null;
											break;
											// TODO
										}
									}
								}
								if(json!=null) {
									if(json instanceof JsonNumber) {
										Number longValue = ((JsonNumber)json).numberValue();
										series1.addOrUpdate(new Second(new Date(m.getTime())), longValue);
										
									}
								}
							}
						}
						
					}
					
				}
				
				dataset.addSeries(series1);
			}
		}
	}
	
	@Override
	public void refresh() {
		queryMetrics();
		updateChart();
	}
	
}
