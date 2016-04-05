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
package io.djigger.ui.agentcontrol;

import io.djigger.ui.Session;
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.MonitoredExecution;
import io.djigger.ui.common.MonitoredExecutionRunnable;
import io.djigger.ui.model.SessionExport;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class SessionControlPane extends JPanel implements ActionListener {

	private static final long serialVersionUID = 4629402700741888731L;

	private final Session parent;

	private final JTextField samplerRateTextField;

	private final JPanel samplerSettingPanel;
	
	private CommandButton startButton;
	private CommandButton stopButton;
	private CommandButton showLinenumbers;
	private CommandButton hideLinenumbers;
	
	public SessionControlPane(final Session parent) {
		super();

		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setMaximumSize(new Dimension(Integer.MAX_VALUE,30));

		this.parent = parent;

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		boolean realTimeSession = parent.getConfig().getType() == SessionType.AGENT || parent.getConfig().getType() == SessionType.JMX;
        	
		if(realTimeSession) {
			startButton = new CommandButton("play.png", "Start capture", new Runnable() {
				@Override
				public void run() {
					parent.setSamplingInterval(Integer.decode(samplerRateTextField.getText()));
					parent.setSampling(true);
					startButton.setEnabled(false);
					stopButton.setEnabled(true);
				}
			});
			stopButton = new CommandButton("stop.png", "Stop capture", new Runnable() {
				@Override
				public void run() {
					parent.setSampling(false);
	        		startButton.setEnabled(true);
	        		stopButton.setEnabled(false);
				}
			});
			stopButton.setEnabled(false);
	
			controlPanel.add(startButton);
			controlPanel.add(stopButton);
		}
		
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		controlPanel.add(separator);

		if(realTimeSession) {
			controlPanel.add(new CommandButton("refresh.png", "Refresh", new Runnable() {
				@Override
				public void run() {
					parent.refreshAll();
				}
			}));
		}
		controlPanel.add(new CommandButton("saveas.png", "Save capture as", new Runnable() {
			@Override
			public void run() {
	            final File file = FileChooserHelper.selectFile("Save store", "Save");
				if(file!=null) {
	            	MonitoredExecution execution = new MonitoredExecution(parent.getMain().getFrame(), "Saving... Please wait.", new MonitoredExecutionRunnable() {
						@Override
						public void run(MonitoredExecution execution) {
			            	SessionExport.save(parent, file);							
						}
					});
	            	execution.run();
	            }
			}
		}));
		controlPanel.add(new CommandButton("clear.png", "Delete all data collected", new Runnable() {
			@Override
			public void run() {
				Object[] options = {"Yes",
                "No"};
				int n = JOptionPane.showOptionDialog(parent.getMain(),
				"This will delete all the data collected until now. Continue?",
				"Clear Store",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
				
				if(n == 0) {
				parent.clear();
				}
			}
		}));

		add(controlPanel);
		
		showLinenumbers = new CommandButton("numbered-list.png", "Show line numbers", new Runnable() {
			@Override
			public void run() {
				parent.showLineNumbers(true);
				hideLinenumbers.setVisible(true);
				showLinenumbers.setVisible(false);
			}
		});
		
		hideLinenumbers = new CommandButton("list.png", "Hide line numbers", new Runnable() {
			@Override
			public void run() {
				parent.showLineNumbers(false);
				hideLinenumbers.setVisible(false);
				showLinenumbers.setVisible(true);
			}
		});
		hideLinenumbers.setVisible(false);
		
		controlPanel.add(showLinenumbers);
		controlPanel.add(hideLinenumbers);

		

		samplerSettingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		samplerSettingPanel.add(new JLabel("Sampler interval (ms)"));
		samplerRateTextField = new JTextField("1000",5);
		samplerRateTextField.addActionListener(this);
		samplerSettingPanel.add(samplerRateTextField);

		add(samplerSettingPanel);
		

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		parent.setSamplingInterval(Integer.decode(e.getActionCommand()));
	}
}
