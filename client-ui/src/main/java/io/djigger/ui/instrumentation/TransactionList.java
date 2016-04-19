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
import io.djigger.monitoring.java.instrumentation.InstrumentationSample;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.store.filter.TimeStoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.TransactionAnalyzerFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TransactionList extends JPanel {
	
	private final static Integer MAX_SAMPLES = 200;
		
	private final JTable sampleList;
	
	private final JTextField status;
	
	private final InstrumentationPane pane;
	
	private final InstrumentationStatisticsCache cache;
	
	private List<InstrumentationSample> samples;
	
	public TransactionList(final Session main, final InstrumentationPane pane, InstrumentationStatisticsCache cache) {
		super(new BorderLayout());
		this.cache = cache;
		this.pane = pane;
		
		status = new JTextField();
		status.setEditable(false);
		
		
		
		JPopupMenu sampleListPopupMenu = new JPopupMenu();
		sampleListPopupMenu.add(new JMenuItem(new AbstractAction("Analyze") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationSample sample = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));
				Set<Long> threadId = new HashSet<Long>();
				threadId.add(sample.getAtributesHolder().getThreadID());
				TransactionAnalyzerFrame analysisFrame = new TransactionAnalyzerFrame(main, new TimeStoreFilter(null, threadId, sample.getStart(), sample.getEnd()));
			}
		}));

		sampleList = new JTable();
		sampleList.setComponentPopupMenu(sampleListPopupMenu);
		

		
		add(new JScrollPane(sampleList),BorderLayout.CENTER);
		add(status,BorderLayout.SOUTH);
	}

	public void reloadSamples() {
		status.setText("");
		
		samples = new ArrayList<InstrumentationSample>();
		for(InstrumentSubscription subscription:pane.getTransactionTable().getSelection()) {
			InstrumentationStatistics stats = cache.getInstrumentationStatistics(subscription);
			List<InstrumentationSample> subscriptionSamples = cache.getInstrumentationSamples(subscription);
			for(InstrumentationSample sample:subscriptionSamples) {
				samples.add(sample);
			}
		}
		
		Collections.sort(samples, new Comparator<InstrumentationSample>() {
			@Override
			public int compare(InstrumentationSample o1, InstrumentationSample o2) {
				if(o1.getDuration()<o2.getDuration()) {
					return 1;
				} else {
					if(o1.getDuration()==o2.getDuration()) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		});

		if(samples.size()>MAX_SAMPLES) {
			status.setText("Displaying the " + MAX_SAMPLES + " longest transactions.");
		}
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for(int i=0;i<Math.min(samples.size(), MAX_SAMPLES);i++) {
			Vector<Object> vector = new Vector<Object>(3);
			InstrumentationSample sample = samples.get(i);
			if(sample.getAtributesHolder()!=null && sample.getAtributesHolder().getStacktrace()!=null) {
				StackTraceElement lastNode = sample.getAtributesHolder().getStacktrace().getStackTrace()[0];
				vector.add(lastNode.getClassName()+"."+sample.getMethodname());
			} else {
				vector.add(sample.getClassname()+"."+sample.getMethodname());
			}
			vector.add(new Date(sample.getStart()));
			vector.add(sample.getDuration());
			data.add(vector);
		}
		
		Vector vector = new Vector(3);
		vector.add("Name");
		vector.add("Time");
		vector.add("Duration (ms)");
		DefaultTableModel model = new DefaultTableModel(data, vector) {
			public Class getColumnClass(int c) {				
	            switch(c) {
	            case 0:return String.class;
	            case 1:return Date.class;
	            case 2:return Integer.class;
	            default:throw new RuntimeException();
	            }
	        }
		};

		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {

		    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss.SSS");

		    public Component getTableCellRendererComponent(JTable table,
		            Object value, boolean isSelected, boolean hasFocus,
		            int row, int column) {
		        if( value instanceof Date) {
		            value = f.format(value);
		        }
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};

		sampleList.setModel(model);
		sampleList.getColumnModel().getColumn(1).setCellRenderer(tableCellRenderer);
		sampleList.setAutoCreateRowSorter(true);
	}

}
