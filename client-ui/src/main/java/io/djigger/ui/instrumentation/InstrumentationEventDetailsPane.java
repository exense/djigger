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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventData;
import io.djigger.monitoring.java.instrumentation.InstrumentationEventWithThreadInfo;
import io.djigger.monitoring.java.instrumentation.StringInstrumentationEventData;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ui.MainFrame;

@SuppressWarnings("serial")
public class InstrumentationEventDetailsPane extends JDialog {

	public InstrumentationEventDetailsPane(MainFrame main, InstrumentationEvent event) {
		super(main.getFrame(), "Event details", false);
		
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
		
		JPanel panel = new JPanel(new GridLayout(0,1));
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
		
		
		setContentPane(panel);
		
		setResizable(true);
		setVisible(true);
		setSize(800, 500);
		
	}
	
	private Vector<Object> addEntry(String label, Object	 value) {
		Vector<Object> v = new Vector<>(2);
		v.add(label);
		v.add(value);
		return v;
	}
	
}
