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
package io.djigger.ui.connectiondialog;

import io.djigger.ui.SessionConfiguration;
import io.djigger.ui.SessionConfiguration.SessionParameter;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FileChooserParameters implements ConnectionParameterFrame {

	JFileChooser chooser;
	
	JPanel panel;
	
	ConnectionType type;
	
	public FileChooserParameters() {
		super();
		
		LookAndFeel saved = UIManager.getLookAndFeel();
		if(System.getProperty("os.name").toLowerCase().trim().contains("mac"))
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		
		chooser = new JFileChooser();
		
		try {
			UIManager.setLookAndFeel(saved);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
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

	@Override
	public void setReloadListener(ReloadListener listener) {}

}
