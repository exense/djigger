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
package io.djigger.ui.connectiondialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import io.djigger.ui.MainFrame;
import io.djigger.ui.SessionConfiguration;


public class AgentConnectionDialog implements ActionListener {

	private final JDialog dialog;
	
	private final JButton connectionButton;
	
	private ButtonGroup sessionTypeGroup;
	
	private final JPanel agentPane;
	
	private ConnectionParameterFrame currentFrame;

	private boolean result = false;
	
	private ConnectionType type;
	
	private MainFrame main;

	public AgentConnectionDialog(MainFrame main) {
		super();
		
		this.main = main;
		
		this.type = ConnectionType.JMX;
		
		dialog = new JDialog(main.getFrame(), "Agent connection", true);
		dialog.setFocusable(true);
		dialog.setLayout(new BorderLayout());		
		
		JPanel connectionTypePane = new JPanel();

		JPanel buttonGroupPane = new JPanel(new GridLayout(1,0,10,10));
		sessionTypeGroup = new ButtonGroup();
		
		for(ConnectionType type:ConnectionType.values()) {
			addOption(buttonGroupPane, type);			
		}
		
		connectionTypePane.add(buttonGroupPane);
		dialog.add(connectionTypePane, BorderLayout.NORTH);
		
		JPanel buttonPane = new JPanel();
		connectionButton = new JButton("Open");
		connectionButton.addActionListener(this);
		buttonPane.add(connectionButton);
	
		dialog.getRootPane().setDefaultButton(connectionButton);
		
		agentPane = new JPanel();
		
		dialog.add(agentPane, BorderLayout.CENTER);
		dialog.add(buttonPane, BorderLayout.SOUTH);
		
		switchPanes();
		
	}

	private void addOption(JPanel pane, final ConnectionType connectionType) {
		@SuppressWarnings("serial")
		Action l = new AbstractAction(connectionType.getDescription()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = connectionType;
				switchPanes();
			}
		};
		
		JRadioButton button = new JRadioButton(l);
		button.setSelected(connectionType==type);
		sessionTypeGroup.add(button);
		pane.add(button);

//		
//		JToggleButton option1 = new JToggleButton(l);
//		
//		option1.setUI(new MetalToggleButtonUI());
//        //Make it transparent
//		//option1.setContentAreaFilled(false);
//        //No need to be focusable
//		//option1.setFocusable(false);
//		//option1.setBorder(BorderFactory.createEtchedBorder());
//		//option1.setBorderPainted(false);
//		
//		option1.setPreferredSize(new Dimension(80, 60));
//		sessionTypeGroup.add(option1);
//		pane.add(option1);
	}

	public SessionConfiguration getSessionConfiguration() {
		return currentFrame.getSessionConfiguration();
	}
	
	public boolean showAndWait() {
		dialog.pack();
		dialog.setLocationRelativeTo(main);
		dialog.setVisible(true);
		return result;
	}

	private boolean validateInput() {
		result = true;
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (validateInput()) {
			dialog.setVisible(false);
		}
	}

	private void switchPanes() {
		
		agentPane.removeAll();
		
		try {
			currentFrame = type.getParameterDialogClass().newInstance();
			currentFrame.setConnectionType(type);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		agentPane.add(currentFrame.getPanel());
		agentPane.revalidate();
		agentPane.repaint();
		dialog.pack();
	}

}
