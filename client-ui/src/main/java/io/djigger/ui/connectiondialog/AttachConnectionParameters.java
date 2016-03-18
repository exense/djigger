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
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.SessionConfiguration.SessionParameter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class AttachConnectionParameters implements ConnectionParameterFrame {
	
	private final JPanel panel;

	private JList<VirtualMachineDescriptor> processList; 
	
	private final DefaultListModel<VirtualMachineDescriptor> listModel;

	public AttachConnectionParameters() {
		super();		
		listModel = new DefaultListModel<>();
		for(VirtualMachineDescriptor vm:VirtualMachine.list()) {
			listModel.addElement(vm);
		}

		
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
		panel = new JPanel();
		panel.add(listScroller);
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
