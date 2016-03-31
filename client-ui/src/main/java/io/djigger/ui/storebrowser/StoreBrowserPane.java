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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.antlr.runtime.RecognitionException;
import org.bson.conversions.Bson;

import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.OSQL;
import io.djigger.ui.Session;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;

@SuppressWarnings("serial")
public class StoreBrowserPane extends JPanel implements ActionListener, KeyListener {
	
	private final Session parent;
	
	private final JTextField queryTextField;
	
	private final JSpinner fromDateSpinner;
	
	private final JSpinner toDateSpinner;
	
	private final JComboBox<DatePresets> datePresets;
	
	private boolean changeLock = false;

	enum DatePresets {

		MINS15("Last 15 mins", 900000, 0),
		MINS60("Last 60 mins", 3600000, 0),
		HOURS4("Last 4 hours", 14400000, 0),
		HOURS24("Last 24 hours", 86400000, 0),
		CUSTOM("Custom", 0, 0);

		String label;
		
		int fromOffset;
		
		int toOffset;

		private DatePresets(String label, int fromOffset, int toOffset) {
			this.label = label;
			this.fromOffset = fromOffset;
			this.toOffset = toOffset;
		}
	}
	
	public StoreBrowserPane(final Session parent) {
		super();
		
		this.parent = parent;
		
		queryTextField = new JTextField("",5);
		queryTextField.addActionListener(this);
		add(queryTextField);
		
		fromDateSpinner = initSpinner();
	    toDateSpinner = initSpinner();
		
	    DatePresets[] presets = DatePresets.values();
		
		datePresets = new JComboBox<>(presets);
		datePresets.setSelectedIndex(0);
		datePresets.setRenderer(new ListCellRenderer<DatePresets>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends DatePresets> arg0, DatePresets arg1, int arg2, boolean arg3, boolean arg4) {
				return new JLabel(arg1.label);
			}
		});
		datePresets.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {					
					DatePresets preset = (DatePresets) event.getItem();
					applyPreset(preset);
				}				
			}
		}); 
	    
		add(datePresets);
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		
		applyPreset((DatePresets) datePresets.getSelectedItem());
	}

	private JSpinner initSpinner() {
		JSpinner spinner = new JSpinner();
		
		spinner.setModel(new SpinnerDateModel());
		spinner.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				selectCustomPresetIfNeeded();
			}
		});
		spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy HH:mm"));
		((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().addKeyListener(this);
	    add(spinner);
	    
	    return spinner;
	}

	private void applyPreset(DatePresets preset) {
		if(preset!=DatePresets.CUSTOM) {
			changeLock = true;
			try {	
				Calendar cal = new GregorianCalendar();
				cal.add(Calendar.MILLISECOND, -preset.fromOffset);
				fromDateSpinner.getModel().setValue(cal.getTime());
				cal.setTime(new Date());
				cal.add(Calendar.MILLISECOND, -preset.toOffset);
				toDateSpinner.getModel().setValue(cal.getTime());
				
			} finally {
				changeLock = false;
			}
		}
	}

	private void selectCustomPresetIfNeeded() {
		if(datePresets!=null && !changeLock) {	
			datePresets.setSelectedIndex(datePresets.getItemCount()-1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		search();
	}

	private void search() {
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

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			search();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
