package io.djigger.ui.metrics;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

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

public class MetricPane extends Dashlet {

	private final JTable metricNameList;
	
	private final DefaultTableModel model;
	
	private final TimeSeriesCollection dataset;
		
	private final Session session;
	
	private List<Metric<?>> metrics;
	
	public MetricPane(final Session session) {
		super(new BorderLayout());
		
		this.session = session;
		
		metricNameList = new JTable();
		
		Vector<String> vector = new Vector<>(3);
		vector.add("Name");
		
		model = new DefaultTableModel(null, vector) {
			public Class getColumnClass(int c) {				
	            switch(c) {
	            case 0:return String.class;
	            default:throw new RuntimeException();
	            }
	        }
		};

		metricNameList.setModel(model);
		metricNameList.setAutoCreateRowSorter(true);

		ListSelectionModel cellSelectionModel = metricNameList.getSelectionModel();
	    cellSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateChart();
			}
	    	
	    });		
		add(BorderLayout.WEST, new JScrollPane(metricNameList));
		
		dataset = new TimeSeriesCollection();
		XYLineAndShapeRenderer dot = new XYLineAndShapeRenderer();
		
		DateAxis xax = new DateAxis();
		NumberAxis yax = new NumberAxis("calls/min");
		
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
		
		Set<String> metricNameDistinct = new HashSet<>();
		
		for(Metric<?> metric:metrics) {
			metricNameDistinct.add(metric.getName());
		}
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for(String metricName:metricNameDistinct) {
			Vector<Object> vector = new Vector<Object>(1);
			vector.add(metricName);
			data.addElement(vector);
		}
		
		Vector<String> headers = new Vector<>(1);
		headers.add("Name");
		
		final Set<String> selectedMetrics = getSelectedMetrics();
		model.setDataVector(data, headers);
		
		for(int i=0;i<metricNameList.getRowCount();i++) {
			if(selectedMetrics.contains(metricNameList.getValueAt(i,0))) {
				metricNameList.getSelectionModel().addSelectionInterval(i, i);
			}
		}
		
		metricNameList.getRowSorter().toggleSortOrder(0);
	}
	
	protected void createPanel(Set<String> selectedMetrics, List<Metric<?>> metrics) {

		dataset.removeAllSeries();
		for(String metricName:selectedMetrics) {
			addSerie(dataset, metricName, metrics);
		}
		//chartPanel.setPreferredSize(new Dimension(300, 300));
		
	}
	
	protected void addSerie(TimeSeriesCollection dataset, String metricName, List<Metric<?>> metrics) {
		TimeSeries series1 = new TimeSeries(metricName);
		for(Metric<?> metric:metrics) {
			if(metric.getName().equals(metricName)) {
				series1.addOrUpdate(new Second(new Date(metric.getTime())), (Number) metric.getValue());
			}
		}
		dataset.addSeries(series1);			
	}
	
	private void updateChart() {
		if(metricNameList.getSelectedRows().length>0) {
			Set<String> selectedMetrics = getSelectedMetrics();
			createPanel(selectedMetrics, metrics);
		}
	}

	private Set<String> getSelectedMetrics() {
		Set<String> selectedMetrics = new HashSet<>();
		int[] selectedRows = metricNameList.getSelectedRows(); 
		for(int i=0;i<selectedRows.length;i++) {
			selectedMetrics.add((String)metricNameList.getValueAt(selectedRows[i],0));
		}
		return selectedMetrics;
	}

	@Override
	public void refresh() {
		queryMetrics();
		updateChart();
		
	}
	
}
