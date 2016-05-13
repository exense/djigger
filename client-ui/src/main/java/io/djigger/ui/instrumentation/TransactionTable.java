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
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class TransactionTable extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(TransactionTable.class);
	
	private final InstrumentationPane parent;
	
	private JTable table;
	
	private InstrumentationStatisticsCache cache;
	
	private DecimalFormat format1 = new DecimalFormat("#");
	
	private DecimalFormat format2 = new DecimalFormat("#.####");
	
	public TransactionTable(final InstrumentationStatisticsCache cache, final InstrumentationPane parent) {
		super(new BorderLayout());
		this.cache = cache;
		this.parent = parent;
		
		
		format1.setRoundingMode(RoundingMode.CEILING);
		format2.setRoundingMode(RoundingMode.CEILING);
		
		table = new JTable();
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setAutoCreateRowSorter(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					parent.subscriptionSelected();
				}
			}
		});
		
//		JPopupMenu popupMenu = new JPopupMenu();
//		JMenuItem item = new JMenuItem("Export as CSV");
//		item.addActionListener(this);
//		popupMenu.add(item);
//		item = new JMenuItem("Stop instrumentation");
//		item.addActionListener(this);
//		popupMenu.add(item);
//		item = new JMenuItem("New instrumentation");
//		item.addActionListener(this);
//		popupMenu.add(item);
		
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel commandPanel = new JPanel();
		commandPanel.add(new CommandButton("add.png", "Add transaction entry point", new Runnable() {
			@Override
			public void run() {
				parent.new NewInstrumentationPane();
			}
		}));
		commandPanel.add(new CommandButton("remove.png", "Discard selection", new Runnable() {
			@Override
			public void run() {
				for(InstrumentSubscription subscription:getSelection()) {
					parent.getSession().removeSubscription(subscription);
				}
				TransactionTable.this.load();
			}
		}));
		commandPanel.add(new CommandButton("importConfig.png", "Import transaction definitions from file", new Runnable() {
			@Override
			public void run() {
				XStream xstream = new XStream();
	            File file = FileChooserHelper.selectFile("Import transaction definitions", "Open");
				if(file!=null) {
	            	Object o = xstream.fromXML(file);
	            	if(o instanceof HashSet) {
	            		HashSet<InstrumentSubscription> subscriptions = (HashSet<InstrumentSubscription>)o;
	            		for(InstrumentSubscription subscription:subscriptions) {
	            			parent.getSession().addSubscription(subscription);
	            		}
	            	}
	            }
			}
		}));
		commandPanel.add(new CommandButton("save.png", "Save transaction definitions to file", new Runnable() {
			@Override
			public void run() {
				XStream xstream = new XStream();
	            File file = FileChooserHelper.selectFile("Save transaction definitions", "Save");
				if(file!=null) {
	            	try {
						xstream.toXML(parent.getSession().getSubscriptions(), new FileWriter(file));
					} catch (IOException e) {
						logger.error("Error while saving transaction definitions to file "+file, e);
					}
	            }
			}
		}));
		commandPanel.add(new CommandButton("export.png", "Export all transactions as CSV", new Runnable() {
			@Override
			public void run() {
				final JFileChooser chooser = new JFileChooser();
	            chooser.setDialogTitle("Export all transactions");
	            chooser.setApproveButtonText("Export");

	            int returnVal = chooser.showOpenDialog(null);
	            if(returnVal == JFileChooser.APPROVE_OPTION) {
            		MonitoredExecution execution = new MonitoredExecution(parent.getSession().getMain().getFrame(), "Exporting... Please wait.", new MonitoredExecutionRunnable() {
						@Override
						public void run(MonitoredExecution execution) {
							try {
								cache.exportSamples(chooser.getSelectedFile());
							} catch (IOException e) {
								JOptionPane.showMessageDialog(table,
										"An error occured when trying to export the samples.",
										"Error",
										JOptionPane.ERROR_MESSAGE);
							}								
						}
					});
            		execution.run();
	            }
			}
		}));
		
		add(commandPanel,BorderLayout.SOUTH);
	}

	private String formatAverageResponseTime(long timeInNano) {
		if(timeInNano>100000) {
			return format1.format(timeInNano/1000000);
		} else {
			return format2.format(timeInNano/1000000);
		}
	}
	
	public void load() {
		Set<InstrumentSubscription> subscriptions = parent.getSession().getSubscriptions();
		
		Object[][] data = new Object[subscriptions.size()][4];
		int i=0;
		for(InstrumentSubscription subscription:subscriptions) {
			data[i][0] = subscription;
			InstrumentationStatistics stats = cache.getInstrumentationStatistics(subscription);
			data[i][1] = stats.getAverageResponseTime();
			data[i][2] = stats.getRealCount();
			data[i][3] = stats.getThroughput();
			i++;
 		}
		
		DefaultTableModel model = new DefaultTableModel(data, new Object[]{"Name","Avg. (ms)","Calls","Calls/s"}) {
			public Class getColumnClass(int c) {				
	            switch(c) {
	            case 0:return InstrumentSubscription.class;
	            case 1:return Double.class;
	            case 2:return Integer.class;
	            case 3:return Double.class;
	            default:throw new RuntimeException();
	            }
	        }
		};
		
		table.setModel(model);
		
	}
	
	public Set<InstrumentSubscription> getSelection() {
		Set<InstrumentSubscription> result = new HashSet<InstrumentSubscription>();
		for(int i:table.getSelectedRows()) {
			result.add((InstrumentSubscription)table.getValueAt(i, 0));
		}
		return result;
	}

}
