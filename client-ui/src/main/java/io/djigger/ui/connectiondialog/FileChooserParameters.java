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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class FileChooserParameters implements ConnectionParameterFrame {

	JFileChooser chooser;
	
	JPanel panel;
	
	ConnectionType type;
	
	public FileChooserParameters() {
		super();
		chooser = new JFileChooser();
		chooser.setControlButtonsAreShown(false);
		panel = new JPanel();
		panel.add(chooser);
	}

	@Override
	public void setConnectionType(ConnectionType type) {
		this.type = type;
	}

	@Override
	public SessionConfiguration getSessionConfiguration() {
		File file = chooser.getSelectedFile();
		if(file!=null) {
        	SessionConfiguration config = new SessionConfiguration(file.getName(), type.getSessionType());
			config.getParameters().put(SessionParameter.FILE, file.getAbsolutePath());
			return config;
        }
		throw new RuntimeException("No file has been selected");
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

}
