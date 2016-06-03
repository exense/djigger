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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventData;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.instrumentation.StringInstrumentationEventData;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.Filter;
import io.djigger.ql.FilterFactory;
import io.djigger.ql.OQLFilterBuilder;
import io.djigger.store.Store;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.analyzer.Dashlet;
import io.djigger.ui.analyzer.TransactionAnalyzerFrame;
import io.djigger.ui.common.EnhancedTextField;

public class InstrumentationEventPane extends Dashlet {
	
	private final static Integer MAX_SAMPLES = 1000;
	
	private final AnalyzerGroupPane parent;
	
	private EnhancedTextField filterTextField;
		
	private final JTable sampleList;
	
	private final JTextField status;
	
	private List<InstrumentationEvent> samples;
	
	private Store store;
	
	private Session session;
	
	private final String EVENT_FILTER = "Event filter (and, or, not operators allowed)";

	public InstrumentationEventPane(final Session main, AnalyzerGroupPane parent) {
		this(main, null, parent);
	}
	
	@SuppressWarnings("serial")
	public InstrumentationEventPane(final Session main, final String query, final AnalyzerGroupPane parent) {
		super(new BorderLayout());
		
		this.parent = parent;
		this.session = main;
		
		store = main.getStore();
		
		status = new JTextField();
		status.setEditable(false);
		
		
		filterTextField = new EnhancedTextField(EVENT_FILTER);
		filterTextField.setToolTipText(EVENT_FILTER);
		filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		filterTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				query();
			}
		});
		add(filterTextField,BorderLayout.NORTH);
		
		
		JPopupMenu sampleListPopupMenu = new JPopupMenu();
		
		sampleListPopupMenu.add(new JMenuItem(new AbstractAction("Show event details") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final InstrumentationEvent event = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));

				Vector<Vector<Object>> data = new Vector<Vector<Object>>();
				
				Vector<String> header = new Vector<>(3);
				header.add("Key");
				header.add("Value");
				DefaultTableModel model = new DefaultTableModel(data, header) {
					public Class getColumnClass(int c) {				
			            switch(c) {
			            case 0:return String.class;
			            case 1:return String.class;
			            default:throw new RuntimeException();
			            }
			        }
				};
				
				
				
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.S");
				data.add(addEntry("Event ID:", event.getId().toString()));
				data.add(addEntry("Transaction ID:", event.getTransactionID().toString()));

				data.add(addEntry("Thread ID:", Long.toString(event.getThreadID())));
				data.add(addEntry("Start:", format.format(new Date(event.getStart()))));
				data.add(addEntry("Duration (ms):", Double.toString(event.getDuration() / 1000000.0)));
				data.add(addEntry("Classname: ", event.getClassname()));
				data.add(addEntry("Methodname: ", event.getMethodname()));

				InstrumentationEventData eventData = event.getData();
				if(eventData!=null) {
					if(eventData instanceof StringInstrumentationEventData) {
						data.add(addEntry("Data: ", ((StringInstrumentationEventData)eventData).getPayload()));
					}
				}
				
				JDialog dialog = new JDialog(parent.getMain().getMain().getFrame(), "New session", false);			
				
				JPanel panel = new JPanel(new GridLayout(2,1));
				JTable table = new JTable(model);
				
				JPanel attributesPanel = new JPanel(new BorderLayout());
				attributesPanel.add(new JLabel("Attributes"), BorderLayout.NORTH);
				attributesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
				panel.add(attributesPanel);
				
				
				if(event instanceof InstrumentationEventWithThreadInfo) {
					ThreadInfo info = ((InstrumentationEventWithThreadInfo)event).getThreadInfo();
					
					Vector<Vector<Object>> stackTraceTableData = new Vector<Vector<Object>>();
					
					Vector<String> stacktraceTableHeader = new Vector<>(2);
					stacktraceTableHeader.add("Class");
					stacktraceTableHeader.add("Method");
					DefaultTableModel stacktraceTableModel = new DefaultTableModel(stackTraceTableData, stacktraceTableHeader) {
						public Class getColumnClass(int c) {				
				            switch(c) {
				            case 0:return String.class;
				            case 1:return String.class;
				            default:throw new RuntimeException();
				            }
				        }
					};
					
					StackTraceElement[] stackstrace = info.getStackTrace();
					
					for(StackTraceElement el:stackstrace) {
						Vector<Object> v = new Vector<>(2);
						v.add(el.getClassName());
						v.add(el.getMethodName());
						stackTraceTableData.add(v);
					}
					
					JTable stacktraceTable = new JTable(stacktraceTableModel);

					JPanel stackTracePanel = new JPanel(new BorderLayout());
					stackTracePanel.add(new JLabel("Stacktrace"), BorderLayout.NORTH);
					stackTracePanel.add(new JScrollPane(stacktraceTable), BorderLayout.CENTER);
					panel.add(stackTracePanel);
					
				}
				
				
				dialog.setContentPane(panel);
				
				dialog.setResizable(true);
				dialog.setVisible(true);
				dialog.setSize(800, 500);
				
			}
			
			private Vector<Object> addEntry(String label, Object	 value) {
				Vector<Object> v = new Vector<>(2);
				v.add(label);
				v.add(value);
				return v;
			}
		}));
		
		
		
		sampleListPopupMenu.add(new JMenuItem(new AbstractAction("Build sampling trees for this transaction") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationEvent sample = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));
				final UUID transactionID = sample.getTransactionID();
				if(transactionID!=null) {
					new TransactionAnalyzerFrame(main, new StoreFilter() {
						
						@Override
						public boolean match(InstrumentationEvent sample) {
							return sample.getTransactionID().equals(transactionID);
						}
						
						@Override
						public boolean match(ThreadInfo dump) {
							return transactionID.equals(dump.getTransactionID());
						}
					});
					
				}
			}
		}));
		sampleListPopupMenu.add(new JMenuItem(new AbstractAction("Transaction tree") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationEvent sample = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));
				UUID transactionID = sample.getTransactionID();
				
				main.getAnalyzerGroupPane().addTransactionPane(transactionID);
			}
		}));
		sampleListPopupMenu.add(new JMenuItem(new AbstractAction("List events in this transaction") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationEvent sample = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));
				UUID transactionID = sample.getTransactionID();
				
				main.getAnalyzerGroupPane().addInstrumentationEventPaneForTransaction(transactionID);
			}
		}));
		

		sampleList = new JTable();
		sampleList.setComponentPopupMenu(sampleListPopupMenu);
		

		
		add(new JScrollPane(sampleList),BorderLayout.CENTER);
		add(status,BorderLayout.SOUTH);

		if(query!=null) {
			filterTextField.setText(query);
		}
		
		query();
	}

	private void query() {
		Filter<InstrumentationEvent> eventFilter=null;
		String filter = filterTextField.getText();
		if(filter!=null) {
			try {
				eventFilter = OQLFilterBuilder.getFilter(filter, new FilterFactory<InstrumentationEvent>() {

					@Override
					public Filter<InstrumentationEvent> createFullTextFilter(final String expression) {
						return new Filter<InstrumentationEvent>() {
							@Override
							public boolean isValid(InstrumentationEvent input) {
								return parent.getPresentationHelper().getFullname(input).contains(expression) || instrumentationDataMatches(input.getData(), expression);
							}
						};
					}

					@Override
					public Filter<InstrumentationEvent> createAttributeFilter(final String operator, final String attribute, final String value) {
						
						if(attribute.equals("trid")&&operator.equals("=")) { 
							final UUID uuid = UUID.fromString(value);
							return new Filter<InstrumentationEvent>() {
								@Override
								public boolean isValid(InstrumentationEvent input) {
									return uuid.equals(input.getTransactionID());
								}
							};
						} else {
							throw new RuntimeException("not implemented");
						}
					}
				});
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		final StoreFilter storeFilter = this.parent.getStoreFilter();
		final Filter<InstrumentationEvent> localFilter = eventFilter;
		Filter<InstrumentationEvent> mergedFilter = new Filter<InstrumentationEvent>() {

			@Override
			public boolean isValid(InstrumentationEvent input) {
				return (storeFilter==null||storeFilter.match(input))&&(localFilter==null||localFilter.isValid(input));
			}
		};
		
		samples = store.queryInstrumentationEvents(mergedFilter);
		
		Collections.sort(samples, new Comparator<InstrumentationEvent>() {
			@Override
			public int compare(InstrumentationEvent o1, InstrumentationEvent o2) {
				return -Long.compare(o1.getStart(), o2.getStart());
			}
		});

		if(samples.size()>MAX_SAMPLES) {
			status.setText("Displaying the " + MAX_SAMPLES + " newest transactions.");
		}
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for(int i=0;i<Math.min(samples.size(), MAX_SAMPLES);i++) {
			Vector<Object> vector = new Vector<Object>(3);
			InstrumentationEvent sample = samples.get(i);
			vector.add(sample.getClassname()+"."+sample.getMethodname());
			InstrumentationEventData eventData = sample.getData();
			String eventDataStr = null;
			if(eventData!=null) {
				if(eventData instanceof StringInstrumentationEventData) {
					eventDataStr = ((StringInstrumentationEventData)eventData).getPayload();
				}
			} 
			vector.add(eventDataStr!=null?eventDataStr:"");
			vector.add(new Date(sample.getStart()));
			vector.add(sample.getDuration()/1000000.0);
			data.add(vector);
		}
		
		Vector<String> vector = new Vector<>(3);
		vector.add("Name");
		vector.add("Data");
		vector.add("Time");
		vector.add("Duration (ms)");
		DefaultTableModel model = new DefaultTableModel(data, vector) {
			public Class getColumnClass(int c) {				
	            switch(c) {
	            case 0:return String.class;
	            case 1:return String.class;
	            case 2:return Date.class;
	            case 3:return Double.class;
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
		sampleList.getColumnModel().getColumn(2).setCellRenderer(tableCellRenderer);
		sampleList.setAutoCreateRowSorter(true);
		ListSelectionModel cellSelectionModel = sampleList.getSelectionModel();
	    cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(sampleList.getSelectedRow()!=-1) {
					InstrumentationEvent event = samples.get(sampleList.convertRowIndexToModel(sampleList.getSelectedRow()));
					Set<Long> selectedThreadIds = new HashSet<>();
					selectedThreadIds.add(event.getThreadID());
					parent.fireSelection(selectedThreadIds);					
				}
			}
	    	
	    });
	}
	
	private boolean instrumentationDataMatches(InstrumentationEventData data, String expression) {
		if(data!=null && data instanceof StringInstrumentationEventData) {
			String payload = ((StringInstrumentationEventData)data).getPayload();
			return payload!=null?payload.contains(expression):false;
		} else {
			return false;
		}
	}

	@Override
	public void refresh() {
		query();
	}
}
