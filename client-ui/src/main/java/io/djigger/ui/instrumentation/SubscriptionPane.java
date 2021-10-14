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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.table.TableCellRenderer;

import io.djigger.xstream.XStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import io.djigger.agent.InstrumentationError;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.CapturingSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.RegexSubscription;
import io.djigger.ui.Session;
import io.djigger.ui.SessionListener;
import io.djigger.ui.analyzer.Dashlet;
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.FileMetadata;

public class SubscriptionPane extends Dashlet {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPane.class);

    private final Session parent;

    private JTable table;
    private DefaultTableModel tableModel;
    
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

                if (!e.getValueIsAdjusting()) {
                    //parent.subscriptionSelected();
                }
            }
        });

        parent.addListener(new SessionListener() {
            @Override
            public void subscriptionChange() {
                load();
            }

			@Override
			public void instrumentationError(InstrumentationError error) {
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
                for (InstrumentSubscription subscription : getSelection()) {
                    parent.removeSubscription(subscription);
                }
            }
        }));
        commandPanel.add(new CommandButton("importConfig.png", "Import subscriptions", new Runnable() {
            @Override
            public void run() {
                XStream xstream = XStreamFactory.createWithAllTypesPermission();
                File file = FileChooserHelper.loadFile(FileMetadata.SUBSCRIPTIONS);
                if (file != null) {
                    Object o = xstream.fromXML(file);
                    if (o instanceof HashSet) {
                        HashSet<InstrumentSubscription> subscriptions = (HashSet<InstrumentSubscription>) o;
                        for (InstrumentSubscription subscription : subscriptions) {
                            parent.addSubscription(subscription);
                        }
                    }
                }
            }
        }));
        commandPanel.add(new CommandButton("save.png", "Export subscriptions", new Runnable() {
            @Override
            public void run() {
                XStream xstream = XStreamFactory.createWithAllTypesPermission();
                File file = FileChooserHelper.saveFile(FileMetadata.SUBSCRIPTIONS);
                if (file != null) {
                    try {
                        xstream.toXML(parent.getSubscriptions(), new FileWriter(file));
                    } catch (IOException e) {
                        logger.error("Error while exporting subscriptions" + file, e);
                    }
                }
            }
        }));

        add(commandPanel, BorderLayout.SOUTH);

        table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent evt) {
				int rowIndex = table.rowAtPoint(evt.getPoint());
			    int colIndex = table.columnAtPoint(evt.getPoint());
			    
			    if(colIndex==2) {
			    	System.out.println(table.getModel().getValueAt(rowIndex, colIndex));			    	
			    }
			}
		});
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        tableModel = new DefaultTableModel(new Object[][] {}, new Object[]{"ID", "Name", "Status", "Tagged"}) {
            public Class getColumnClass(int c) {
                switch (c) {
                	case 0:
                		return int.class;
                    case 1:
                        return InstrumentSubscription.class;
                    case 2:
                        return InstrumentSubscription.class;
                    case 3:
                        return boolean.class;
                    default:
                        throw new RuntimeException();
                }
            }
        };
        
        table.setModel(tableModel);
        
        table.getColumnModel().getColumn(2).setPreferredWidth(20);
        table.getColumnModel().getColumn(2).setCellRenderer(new InstrumentationErrorRenderer());
        
        load();
    }

    public void load() {
        Set<InstrumentSubscription> subscriptions = parent.getSubscriptions();

        if (subscriptions != null) {
            Object[][] data = new Object[subscriptions.size()][4];
            int i = 0;
            for (InstrumentSubscription subscription : subscriptions) {
            	data[i][0] = subscription.getId();
            	data[i][1] = subscription;
            	data[i][2] = subscription;
                data[i][3] = subscription.isTagEvent();
                
                i++;
            }

            tableModel.setDataVector(data, new Object[]{"ID", "Name", "Status", "Tagged"});
            
            table.getColumnModel().getColumn(2).setPreferredWidth(20);
            table.getColumnModel().getColumn(2).setCellRenderer(new InstrumentationErrorRenderer());
        }
    }
    
	public class InstrumentationErrorRenderer extends JLabel implements TableCellRenderer {
		
		public InstrumentationErrorRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object error_, boolean isSelected, boolean hasFocus,
				int row, int column) {

			setPreferredSize(new Dimension(10, 10));
			
			InstrumentSubscription subscription = (InstrumentSubscription) error_;

            boolean hasError = false;
            InstrumentationError firstError = null;
            for (InstrumentationError instrumentationError : parent.getInstrumentationErrors()) {
				if(instrumentationError.getSubscription() != null && instrumentationError.getSubscription().equals(subscription)) {
					firstError = instrumentationError;
					hasError = true;
					break;
				}
			}
            
			if(hasError) {
				setBackground(Color.RED);
				String message = firstError.getException().getMessage();
				setText(message);
				setToolTipText(message);
			} else {
				setBackground(Color.GREEN);
				setText("");
				setToolTipText("Successfully instrumented");
			}
			return this;
		}
	}

    public Set<InstrumentSubscription> getSelection() {
        Set<InstrumentSubscription> result = new HashSet<InstrumentSubscription>();
        for (int i : table.getSelectedRows()) {
            result.add((InstrumentSubscription) table.getValueAt(i, 1));
        }
        return result;
    }

    public class NewInstrumentationPane extends JPanel implements ActionListener {

        private final JDialog frame;

        private final JTextField classname;

        private final JTextField methodname;

        private final JTextField capture;

        private final JButton button;

        public NewInstrumentationPane() {
            super();

            classname = new JTextField("", 50);
            methodname = new JTextField("", 50);

            capture = new JTextField("", 50);

            button = new JButton("Add");

            setLayout(new GridLayout(0, 1, 0, 2));

            button.addActionListener(this);

            add(new JLabel("Class name pattern (regex)"));
            add(classname);
            add(new JLabel("Method name pattern (regex)"));
            add(methodname);

            add(new JLabel("Capture expression (javassist) [optional]"));
            add(capture);

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
            if (e.getActionCommand().equals("Add")) {
                InstrumentSubscription subscription;
                if (capture.getText().isEmpty()) {
                    subscription = new RegexSubscription(classname.getText(), methodname.getText(), false);
                } else {
                    subscription = new CapturingSubscription(classname.getText(), methodname.getText(), false);
                    ((CapturingSubscription) subscription).setCapture(capture.getText());
                }
                parent.addSubscription(subscription);
                frame.setVisible(false);
            }
        }
    }

    @Override
    public void refresh() {
    }

}
