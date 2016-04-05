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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import io.djigger.ui.Session.SessionType;
import io.djigger.ui.SessionConfiguration;
import io.djigger.ui.SessionConfiguration.SessionParameter;

public class AttachConnectionParameters implements ConnectionParameterFrame {
	
	private static final Logger logger = LoggerFactory.getLogger(AttachConnectionParameters.class);
	
	private final JPanel panel;

	private JList<VirtualMachineDescriptor> processList; 
	
	private final DefaultListModel<VirtualMachineDescriptor> listModel;

	public AttachConnectionParameters() {
		super();		
		listModel = new DefaultListModel<>();
		
		boolean toolJarAvailable = true;
		try {
			for(VirtualMachineDescriptor vm:VirtualMachine.list()) {
				listModel.addElement(vm);
			}
		} catch (NoClassDefFoundError e) {
			toolJarAvailable = false;
			logger.debug("Unable to initialize AttachConnectionParameters. The class com.sun.tools.attach.VirtualMachine cannot be found in the classpath.",e);
		}

		panel = new JPanel();
		if(toolJarAvailable) {
			
			processList = new JList<VirtualMachineDescriptor>(listModel); //data has type Object[]
			processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			processList.setLayoutOrientation(JList.VERTICAL);
			processList.setCellRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					if (value != null) {
						VirtualMachineDescriptor vm = (VirtualMachineDescriptor) value;
						value = vm.id() + " " + vm.displayName();
					}
					return super.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
				}
			});
			
			
			
			JScrollPane listScroller = new JScrollPane(processList);
			panel.add(listScroller);
		} else {
			JLabel infoMsg = new JLabel("Add tools.jar to the classpath to enable this feature");
			panel.add(infoMsg);
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public SessionConfiguration getSessionConfiguration() {
		SessionConfiguration config; 
		VirtualMachineDescriptor vm = (VirtualMachineDescriptor)processList.getSelectedValue();
		config = new SessionConfiguration(vm.displayName(), SessionType.AGENT);
		config.getParameters().put(SessionParameter.PROCESSID, vm.id());
		return config;
	}

	@Override
	public void setConnectionType(ConnectionType type) {}
	
}
