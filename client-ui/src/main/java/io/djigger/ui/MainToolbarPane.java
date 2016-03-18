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
