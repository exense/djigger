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
package io.djigger.ui.storebrowser;

import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.OSQL;
import io.djigger.ui.Session;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import org.antlr.runtime.RecognitionException;
import org.bson.conversions.Bson;

public class StoreBrowserPane extends JPanel implements ActionListener {
	
	private final Session parent;
	
	private final JTextField queryTextField;
	
	private final JSpinner fromDateSpinner;
	
	private final JSpinner toDateSpinner;

	public StoreBrowserPane(final Session parent) {
		super();
		
		this.parent = parent;
		
		queryTextField = new JTextField("",5);
		queryTextField.addActionListener(this);
		add(queryTextField);
		
		fromDateSpinner = new JSpinner();
		fromDateSpinner.setModel(new SpinnerDateModel());
		fromDateSpinner.setEditor(new JSpinner.DateEditor(fromDateSpinner, "dd.MM.yyyy h:mm a"));
	    add(fromDateSpinner);
	    
	    toDateSpinner = new JSpinner();
		toDateSpinner.setModel(new SpinnerDateModel());
		toDateSpinner.setEditor(new JSpinner.DateEditor(toDateSpinner, "dd.MM.yyyy h:mm a"));
	    add(toDateSpinner);
		
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		parent.clear();
		
		final Bson query;
		try {
			query = OSQL.toMongoQuery(queryTextField.getText());
			final Date from = ((SpinnerDateModel)fromDateSpinner.getModel()).getDate();
			final Date to = ((SpinnerDateModel)toDateSpinner.getModel()).getDate();
			
			final MonitoredExecution execution = new MonitoredExecution(parent.getMain().getFrame(), "Opening session... Please wait.", new MonitoredExecutionRunnable() {
				protected void run(MonitoredExecution execution) {
    				
					execution.setText("Calculating execution time...");
    				long maxValue = parent.getStoreClient().getThreadInfoAccessor().count(query, from, to);
    				execution.setMaxValue(maxValue);
    				execution.setText("Retrieving data...");
    				
    				try {
    					int count = 0;
    					Iterator<ThreadInfo> it = parent.getStoreClient().getThreadInfoAccessor().query(query, from, to).iterator();
    					
    					ThreadInfo thread;
    					while(it.hasNext() && !execution.isInterrupted()) {
    						count++;
    						
    						execution.setValue(count);
    						
    						thread=it.next();

							parent.getStore().addThreadInfo(thread);
    					}
    					parent.getStore().processBuffers();
    				
    					System.out.println("Fetched " + count + " stacktraces.");
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}, true);
    		execution.run();
    		
			parent.refreshAll();
		} catch (RecognitionException e) {
			
		}
	}

}
