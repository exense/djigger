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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.RegexSubscription;
import io.djigger.ui.Session;
import io.djigger.ui.SessionListener;
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;

public class SubscriptionPane extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionPane.class);
	
	private final Session parent;
	
	private JTable table;
	
	public SubscriptionPane(final Session parent) {
		super(new BorderLayout());
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
					//parent.subscriptionSelected();
				}
			}
		});
		
		parent.addListener(new SessionListener() {
			@Override
			public void subscriptionChange() {
				load();
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
		commandPanel.add(new CommandButton("add.png", "Add subscription", new Runnable() {
			@Override
			public void run() {
				new NewInstrumentationPane();
			}
		}));
		commandPanel.add(new CommandButton("remove.png", "Discard selection", new Runnable() {
			@Override
			public void run() {
				for(InstrumentSubscription subscription:getSelection()) {
					parent.removeSubscription(subscription);
				}
			}
		}));
		commandPanel.add(new CommandButton("importConfig.png", "Import subscriptions", new Runnable() {
			@Override
			public void run() {
				XStream xstream = new XStream();
	            File file = FileChooserHelper.selectFile("Import subscriptions", "Open");
				if(file!=null) {
	            	Object o = xstream.fromXML(file);
	            	if(o instanceof HashSet) {
	            		HashSet<InstrumentSubscription> subscriptions = (HashSet<InstrumentSubscription>)o;
	            		for(InstrumentSubscription subscription:subscriptions) {
	            			parent.addSubscription(subscription);
	            		}
	            	}
	            }
			}
		}));
		commandPanel.add(new CommandButton("save.png", "Export subscriptions", new Runnable() {
			@Override
			public void run() {
				XStream xstream = new XStream();
	            File file = FileChooserHelper.selectFile("Export subscriptions", "Save");
				if(file!=null) {
	            	try {
						xstream.toXML(parent.getSubscriptions(), new FileWriter(file));
					} catch (IOException e) {
						logger.error("Error while exporting subscriptions"+file, e);
					}
	            }
			}
		}));
		
		add(commandPanel,BorderLayout.SOUTH);
		
		load();
	}
	
	public void load() {
		Set<InstrumentSubscription> subscriptions = parent.getSubscriptions();
		
		if(subscriptions!=null) {
			Object[][] data = new Object[subscriptions.size()][4];
			int i=0;
			for(InstrumentSubscription subscription:subscriptions) {
				data[i][0] = subscription;
				data[i][1] = subscription.isTagEvent();
				data[i][2] = subscription.captureThreadInfo();
				i++;
	 		}
			
			
			DefaultTableModel model = new DefaultTableModel(data, new Object[]{"Name", "Tagged", "Captures path"}) {
				public Class getColumnClass(int c) {				
		            switch(c) {
		            case 0:return InstrumentSubscription.class;
		            case 1:return boolean.class;
		            case 2:return boolean.class;
		            default:throw new RuntimeException();
		            }
		        }
			};
			
			table.setModel(model);
		}
	}
	
	public Set<InstrumentSubscription> getSelection() {
		Set<InstrumentSubscription> result = new HashSet<InstrumentSubscription>();
		for(int i:table.getSelectedRows()) {
			result.add((InstrumentSubscription)table.getValueAt(i, 0));
		}
		return result;
	}

	public class NewInstrumentationPane extends JPanel implements ActionListener {

		private final JDialog frame;

		private final JTextField classname;

		private final JTextField methodname;

		private final JButton button;

		public NewInstrumentationPane(){
			super();

			classname = new JTextField("",20);
			methodname = new JTextField("",20);
			button = new JButton("Add");

			setLayout(new GridLayout(0,1,0,2));

			button.addActionListener(this);

			add(new JLabel("Class name pattern (regex)"));
			add(classname);
			add(new JLabel("Method name pattern (regex)"));
			add(methodname);
			add(new JSeparator());
			add(button);


			frame = new JDialog();
	        frame.add(this);
	        frame.pack();
	        frame.setResizable(false);
	        frame.setVisible(true);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Add")) {
				InstrumentSubscription subscription = new RegexSubscription(classname.getText(), methodname.getText(), false);
				parent.addSubscription(subscription);
				frame.setVisible(false);
			}
		}
	}

}
