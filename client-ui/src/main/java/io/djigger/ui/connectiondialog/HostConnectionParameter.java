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

import io.djigger.ui.SessionConfiguration;
import io.djigger.ui.SessionConfiguration.SessionParameter;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class HostConnectionParameter implements CaretListener, ConnectionParameterFrame {

	private boolean defaultName;
	
	private final JPanel hostParametersPane;
	
	private final JTextField name;

	private final JTextField host;

	private final JTextField port;
	
	private final JTextField username;

	private final JTextField password;
	
	private ConnectionType connectionType;

	public HostConnectionParameter() {
		super();
				
		defaultName = true;
		
		JPanel namePane = new JPanel(new GridLayout(0, 2));
		namePane.add(new JLabel("Name"));
		name = new JTextField(20);
		name.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				defaultName = false;
			}
		});
		namePane.add(name);
		
		
		hostParametersPane = new JPanel(new GridLayout(0, 2));
		hostParametersPane.add(new JLabel("Host"));
		host = new JTextField(20);
		host.addCaretListener(this);
		hostParametersPane.add(host);
		hostParametersPane.add(new JLabel("Port"));
		port = new JTextField(5);
		port.addCaretListener(this);
		hostParametersPane.add(port);		
		hostParametersPane.add(new JLabel("Username"));
		username = new JTextField(20);
		hostParametersPane.add(username);
		hostParametersPane.add(new JLabel("Password"));
		password = new JTextField(20);
		hostParametersPane.add(password);
		
		host.requestFocusInWindow();
		
		
	}
	

	@Override
	public void caretUpdate(CaretEvent e) {
		if(defaultName) {
			name.setText(host.getText()+":"+port.getText());
			defaultName = true;
		}
	}
	
	public String getName() {
		return name.getText();
	}

	public String getHost() {
		return host.getText();
	}

	public String getPort() {
		return port.getText();
	}

	public String getUsername() {
		return username.getText();
	}

	public String getPassword() {
		return password.getText();
	}
	
	public SessionConfiguration getSessionConfiguration() {
		SessionConfiguration config = new SessionConfiguration(getName(), connectionType.getSessionType());
		
		config.getParameters().put(SessionParameter.HOSTNAME, getHost());
		config.getParameters().put(SessionParameter.PORT, getPort());
		config.getParameters().put(SessionParameter.USERNAME, getUsername());
		config.getParameters().put(SessionParameter.PASSWORD, getPassword());
		
		return config;
	}


	@Override
	public JPanel getPanel() {
		return hostParametersPane;
	}


	@Override
	public void setConnectionType(ConnectionType type) {
		connectionType = type;
	}
	
}
