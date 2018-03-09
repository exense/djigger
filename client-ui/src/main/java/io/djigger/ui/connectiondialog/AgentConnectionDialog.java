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

import io.djigger.ui.MainFrame;
import io.djigger.ui.SessionConfiguration;
import io.djigger.ui.connectiondialog.ConnectionParameterFrame.ReloadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AgentConnectionDialog implements ActionListener, ReloadListener {

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

        dialog = new JDialog(main.getFrame(), "New session", true);
        dialog.setFocusable(true);
        dialog.setLayout(new BorderLayout());

        JPanel connectionTypePane = new JPanel();

        JPanel buttonGroupPane = new JPanel(new GridLayout(1, 0, 10, 10));
        sessionTypeGroup = new ButtonGroup();

        for (ConnectionType type : ConnectionType.values()) {
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
        button.setSelected(connectionType == type);
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
            currentFrame.setReloadListener(this);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        agentPane.add(currentFrame.getPanel());
        reload();
    }

    @Override
    public void reload() {
        agentPane.revalidate();
        agentPane.repaint();
        dialog.pack();
    }

}
