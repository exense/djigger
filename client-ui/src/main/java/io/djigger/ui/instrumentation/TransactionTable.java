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
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public TransactionTable(final InstrumentationStatisticsCache cache, final InstrumentationPane parent) {
		super(new BorderLayout());
		this.cache = cache;
		this.parent = parent;
		
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
						xstream.toXML(parent.getSession().getStore().getSubscriptions(), new FileWriter(file));
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

	public void load() {
		Set<InstrumentSubscription> subscriptions = parent.getSession().getStore().getSubscriptions();
		
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
		
		DefaultTableModel model = new DefaultTableModel(data, new Object[]{"Name","Avg. (ms)","Calls","Calls/min"}) {
			public Class getColumnClass(int c) {				
	            switch(c) {
	            case 0:return InstrumentSubscription.class;
	            case 1:return Integer.class;
	            case 2:return Integer.class;
	            case 3:return Integer.class;
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
