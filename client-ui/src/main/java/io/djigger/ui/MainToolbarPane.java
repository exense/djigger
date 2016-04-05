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
package io.djigger.ui;

import io.djigger.ui.common.CommandButton;
import io.djigger.ui.connectiondialog.AgentConnectionDialog;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class MainToolbarPane extends JPanel {
	
	private static final long serialVersionUID = -4984036099816274265L;
	
	private final MainFrame main;

	public MainToolbarPane(final MainFrame main) {
		super(new BorderLayout());
		
		this.main = main;
		
		JPanel commandPanel = new JPanel();
		commandPanel.add(new CommandButton("add.png", "Add session", new Runnable() {
			@Override
			public void run() {
				addSession();
			}
		}));
		commandPanel.add(new CommandButton("remove.png", "Close selected session", new Runnable() {
			@Override
			public void run() {
				removeSession();
			}
		}));
		commandPanel.add(new CommandButton("importConfig.png", "Load session configuration", new Runnable() {
			@Override
			public void run() {
				main.importSessions();
			}
		}));
		commandPanel.add(new CommandButton("save.png", "Save session configuration", new Runnable() {
			@Override
			public void run() {
				main.exportSessions();
			}
		}));
		
		add(commandPanel);
		
	}
	
	public void addSession() {
		AgentConnectionDialog dialog = new AgentConnectionDialog(main);
		if(dialog.showAndWait()) {
			SessionConfiguration config =  dialog.getSessionConfiguration();
			Session session = new Session(config, main);
			main.addSession(session);
		}
	}
	
	public void removeSession() {
		main.removeCurrentSession();
	}
}
