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
package io.djigger.ui.storebrowser;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.Metric;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.ql.OQLMongoDBBuilder;
import io.djigger.ui.Session;
import io.djigger.ui.common.EnhancedTextField;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.*;

@SuppressWarnings("serial")
public class StoreBrowserPane extends JPanel implements ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(StoreBrowserPane.class);
	
	private final Session parent;
	
	private final EnhancedTextField queryTextField;
	
	private final JSpinner fromDateSpinner;
	
	private final JSpinner toDateSpinner;
	
	private final JComboBox<DatePresets> datePresets;

	enum DatePresets {
		MINS5("Last 5 mins", 300000, 0),
		MINS15("Last 15 mins", 900000, 0),
		MINS60("Last 60 mins", 3600000, 0),
		HOURS4("Last 4 hours", 14400000, 0),
		HOURS24("Last 24 hours", 86400000, 0),
		CUSTOM("Custom", 0, 0);

		String label;
		
		int fromOffset;
		
		int toOffset;

		DatePresets(String label, int fromOffset, int toOffset) {
			this.label = label;
			this.fromOffset = fromOffset;
			this.toOffset = toOffset;
		}
	}

	private static class SpinnerHighlight {
		final int start;
		final int end;

		private SpinnerHighlight(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
	
	private static final String LABEL = "Store filter (and, or, not operators allowed)";

	private static final String SPINNER_PATTERN = "dd.MM.yyyy HH:mm";

	// keep this in sync with the SPINNER_PATTERN
	private static final SpinnerHighlight[] SPINNER_HIGHLIGHTS = new SpinnerHighlight[] {
			new SpinnerHighlight(0,2), // dd
			new SpinnerHighlight(3,5), // MM
			new SpinnerHighlight(6,10), // yyyy
			new SpinnerHighlight(11,13), // HH
			new SpinnerHighlight(14,16), // mm
	};

	private static final Action NO_ACTION = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// do nothing
		}
	};
	
	public StoreBrowserPane(final Session parent) {
		super();
		
		this.parent = parent;
		
		queryTextField = new EnhancedTextField(LABEL);
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
					onDatePresetSelection();
				}				
			}
		}); 
	    
		
		
		add(datePresets);
		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		
		onDatePresetSelection();
	}

	private SpinnerHighlight findSpinnerHighlight(int caretPosition) {
		for (SpinnerHighlight candidate: SPINNER_HIGHLIGHTS) {
			if (candidate.start <= caretPosition && candidate.end >= caretPosition) {
				return candidate;
			}
		}
		return null;
	}

	private JSpinner initSpinner() {
		final JSpinner spinner = new JSpinner();
		
		spinner.setModel(new SpinnerDateModel());
		
		final JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, SPINNER_PATTERN);
		spinner.setEditor(editor);

		final JFormattedTextField textField = editor.getTextField();

		// overwrite default behavior for left/right arrows with no-op actions
		// we handle them below, and the default actions would ruin our changes
		ActionMap am = textField.getActionMap();
		am.put(DefaultEditorKit.forwardAction, NO_ACTION);
		am.put(DefaultEditorKit.selectionForwardAction, NO_ACTION);
		am.put(DefaultEditorKit.backwardAction, NO_ACTION);
		am.put(DefaultEditorKit.selectionBackwardAction, NO_ACTION);

		textField.addKeyListener(
				new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						int keyCode = e.getKeyCode();

						if(keyCode==KeyEvent.VK_ENTER) {
							try {
								editor.commitEdit();
							} catch (ParseException e1) {
								logger.error("Error while parsing expression: "+textField.getText(), e);
							}
							search();							
						}
						if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
							SpinnerHighlight currentHighlight = findSpinnerHighlight(textField.getCaretPosition());
							if (currentHighlight != null) {
								int nextPosition = keyCode == KeyEvent.VK_LEFT ? currentHighlight.start - 1 : currentHighlight.end + 1;
								SpinnerHighlight nextHighlight = findSpinnerHighlight(nextPosition);
								if (nextHighlight == null) {
									nextHighlight = currentHighlight;
								}
								textField.select(nextHighlight.start, nextHighlight.end);
							}
						}
					}
				}
		);

		textField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
					int caret = textField.viewToModel(e.getPoint());
					SpinnerHighlight highlight = findSpinnerHighlight(caret);
					if (highlight != null) {
						textField.select(highlight.start, highlight.end);
					}
				}
			}
		});


		add(spinner);
	    
	    return spinner;
	}

	private void onDatePresetSelection() {
		DatePresets preset = (DatePresets) datePresets.getSelectedItem();
		if(preset!=DatePresets.CUSTOM) {
			fromDateSpinner.setVisible(false);
			toDateSpinner.setVisible(false);
		} else {
			fromDateSpinner.setVisible(true);
			toDateSpinner.setVisible(true);
		}
	}
	
	public void setQuery(String query) {
		queryTextField.setText(query);
	}

	public void setTimeinterval(Date start, Date end) {
		datePresets.setSelectedItem(DatePresets.CUSTOM);
		fromDateSpinner.getModel().setValue(start);
		toDateSpinner.getModel().setValue(end);
	}
	
	private void applyPreset() {
		DatePresets preset = (DatePresets) datePresets.getSelectedItem();
		if(preset!=DatePresets.CUSTOM) {
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MILLISECOND, -preset.fromOffset);
			fromDateSpinner.getModel().setValue(cal.getTime());
			cal.setTime(new Date());
			cal.add(Calendar.MILLISECOND, -preset.toOffset);
			toDateSpinner.getModel().setValue(cal.getTime());
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		search();
	}

	public void search() {
		parent.clear();
		
		applyPreset();
		
		final Bson query;
		try {
			query = parseQuery();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Unable to parse query: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
			
		final Date from = ((SpinnerDateModel)fromDateSpinner.getModel()).getDate();
		final Date to = ((SpinnerDateModel)toDateSpinner.getModel()).getDate();
		
		if(to.after(from)) {
			
			final MonitoredExecution execution = new MonitoredExecution(parent.getMain().getFrame(), "Opening session... Please wait.", new MonitoredExecutionRunnable() {
				protected void run(MonitoredExecution execution) {
					retrieveMetrics(query, from, to, execution);
					retrieveThreadInfos(query, from, to, execution);
    				retrieveInstumentationEvents(query, from, to, execution);
    			}

				private void retrieveInstumentationEvents(final Bson query, final Date from, final Date to,	MonitoredExecution execution) {
					execution.setText("Retrieving instrumentation events...");
    				execution.setIndeterminate();
					int count = 0;
					Iterator<InstrumentationEvent> it = parent.getStoreClient().getInstrumentationAccessor().getTaggedEvents(query, from, to);
					
					InstrumentationEvent event;
					while(it.hasNext() && !execution.isInterrupted()) {
						count++;
						execution.setValue(count);
						event=it.next();
						parent.getStore().getInstrumentationEvents().add(event);
					}
					logger.debug("Fetched " + count + " instrumentation events.");
				}

				private void retrieveThreadInfos(final Bson query, final Date from, final Date to,
						MonitoredExecution execution) {
					execution.setText("Retrieving sampling events...");
					execution.setIndeterminate();
					int count = 0;
					Iterator<ThreadInfo> it = parent.getStoreClient().getThreadInfoAccessor().query(query, from, to).iterator();
					
					ThreadInfo thread;
					while(it.hasNext() && !execution.isInterrupted()) {
						count++;
						thread=it.next();
						parent.getStore().getThreadInfos().add(thread);
					}
				
					logger.debug("Fetched " + count + " stacktraces.");
				}
				
				private void retrieveMetrics(final Bson query, final Date from, final Date to,
						MonitoredExecution execution) {
					execution.setText("Retrieving metrics...");
					execution.setIndeterminate();
					int count = 0;
					Iterator<Metric<?>> it = parent.getStoreClient().getMetricAccessor().get(query, from, to);
					
					Metric<?> metrics;
					while(it.hasNext() && !execution.isInterrupted()) {
						count++;
						metrics=it.next();
						parent.getStore().getMetrics().add(metrics);
					}
				
					logger.debug("Fetched " + count + " metrics.");
				}
    		}, true);
			
			try {
				execution.run();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(parent, "Error while fetching data: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
    		
			parent.refreshAll();
		} else {
			JOptionPane.showMessageDialog(parent, "Invalid timerange: MaxDate<MinDate", "Error",
			        JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private Bson parseQuery() {
		Bson query=null;
		String expression = queryTextField.getText();
		if(expression!=null && expression.trim().length()>0) {
			query = OQLMongoDBBuilder.build(queryTextField.getText());
		}
		return query;
	}
}
