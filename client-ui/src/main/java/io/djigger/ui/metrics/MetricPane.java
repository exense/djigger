package io.djigger.ui.metrics;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import io.djigger.collector.accessors.MetricAccessor;
import io.djigger.db.client.StoreClient;
import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.analyzer.Dashlet;

public class MetricPane extends Dashlet {

	private final JTable metricNameList;
	
	private final AnalyzerGroupPane parent;
	
	public MetricPane(AnalyzerGroupPane parent) {
		super(new BorderLayout());
		
		this.parent = parent;
		
		metricNameList = new JTable();
		
		add(BorderLayout.WEST, new JScrollPane(metricNameList));
		
	}
	
	private void queryMetrics() {
		final StoreClient storeClient = this.parent.getMain().getStoreClient();
		if(storeClient!=null) {
			MetricAccessor metricAccessor = storeClient.getMetricAccessor();
			//metricAccessor.
		}
		
		
	}
	

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

}
