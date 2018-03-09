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

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.SessionConfiguration;
import io.djigger.ui.SessionConfiguration.SessionParameter;
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.common.FileChooserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.tools.ToolProvider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class AttachConnectionParameters implements ConnectionParameterFrame {

    private static final Logger logger = LoggerFactory.getLogger(AttachConnectionParameters.class);

    private final JPanel panel;

    private JList<VirtualMachineDescriptor> processList;

    private DefaultListModel<VirtualMachineDescriptor> listModel;

    private ReloadListener listener;

    public AttachConnectionParameters() {
        super();
        panel = new JPanel();
        init();
    }

    public void setReloadListener(ReloadListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("serial")
    private void init() {

        panel.removeAll();

        boolean isJdk = ToolProvider.getSystemJavaCompiler() != null;

        if (isJdk) {
            boolean toolJarAvailable = false;

            try {
                getClass().getClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
                toolJarAvailable = true;
            } catch (ClassNotFoundException e1) {
                logger.debug("Unable to initialize AttachConnectionParameters. The class com.sun.tools.attach.VirtualMachine cannot be found in the classpath.", e1);
            }


            if (toolJarAvailable) {
                listModel = new DefaultListModel<>();

                for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
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
                            String valueStr = vm.id() + " " + vm.displayName();
                            if (valueStr.length() > 200) {
                                valueStr = valueStr.substring(0, Math.min(200, valueStr.length())) + "...";
                            }
                            value = valueStr;
                        }

                        return super.getListCellRendererComponent(list, value, index,
                            isSelected, cellHasFocus);
                    }
                });

                JScrollPane listScroller = new JScrollPane(processList);
                panel.add(listScroller);
                panel.add(new CommandButton("refresh.png", "Refresh", new Runnable() {

                    @Override
                    public void run() {
                        init();
                        listener.reload();
                    }
                }));
            } else {
                JLabel infoMsg = new JLabel("Add tools.jar from your JDK (jdk_x/lib/tools.jar) to the classpath to enable this feature");
                JButton selectedToolsButton = new JButton(new AbstractAction("Select tools.jar") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File file = FileChooserHelper.selectFile("Select tools.jar", "Open");
                        if (file != null) {
                            try {
                                ClassPathHelper.addFile(file);
                                init();
                                listener.reload();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    }
                });

                panel.add(infoMsg);
                panel.add(selectedToolsButton);
            }
        } else {
            JLabel infoMsg = new JLabel("To enable this feature you have to start djigger with a JDK. The path to your JDK has to be set in the startClient.bat/sh/command");
            panel.add(infoMsg);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public SessionConfiguration getSessionConfiguration() {
        SessionConfiguration config;
        VirtualMachineDescriptor vm = (VirtualMachineDescriptor) processList.getSelectedValue();
        config = new SessionConfiguration(vm.displayName(), SessionType.AGENT);
        config.getParameters().put(SessionParameter.PROCESSID, vm.id());
        return config;
    }

    @Override
    public void setConnectionType(ConnectionType type) {
    }

}
