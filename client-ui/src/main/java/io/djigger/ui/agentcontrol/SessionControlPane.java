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

package io.djigger.ui.agentcontrol;

import io.djigger.ui.Session;
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
	
	private final CommandButton startButton;
	private final CommandButton stopButton;
	
	public SessionControlPane(final Session parent) {
		super();

		setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setMaximumSize(new Dimension(Integer.MAX_VALUE,30));

		this.parent = parent;

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
		
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		controlPanel.add(separator);

		controlPanel.add(new CommandButton("refresh.png", "Refresh", new Runnable() {
			@Override
			public void run() {
				parent.refreshAll();
			}
		}));
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

		samplerSettingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		samplerSettingPanel.add(new JLabel("Sampler interval (ms)"));
		samplerRateTextField = new JTextField("1000",5);
		samplerRateTextField.addActionListener(this);
		samplerSettingPanel.add(samplerRateTextField);

		add(samplerSettingPanel);
		
		stopButton.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		parent.setSamplingInterval(Integer.decode(e.getActionCommand()));
	}
}
