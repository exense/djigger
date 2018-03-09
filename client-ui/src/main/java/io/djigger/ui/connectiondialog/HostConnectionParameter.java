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

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HostConnectionParameter implements CaretListener, ConnectionParameterFrame {

    private boolean defaultName;

    private final JPanel hostParametersPane;

    private final JTextField name;

    private final JTextField host;

    private final JTextField port;

    private final JTextField username;

    private final JPasswordField password;

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
        password = new JPasswordField(20);
        hostParametersPane.add(password);


        JPopupMenu passwordMenu = new JPopupMenu();
        password.setComponentPopupMenu(passwordMenu);
        password.setEchoChar('*');

        passwordMenu.add(new JMenuItem(new AbstractAction("Show/hide") {
            @Override
            public void actionPerformed(ActionEvent e) {
                char echo = password.getEchoChar();
                echo = (echo == 0) ? '*' : 0;
                password.setEchoChar(echo);
            }
        }));

        host.requestFocusInWindow();


    }


    @Override
    public void caretUpdate(CaretEvent e) {
        if (defaultName) {
            name.setText(host.getText() + ":" + port.getText());
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


    @Override
    public void setReloadListener(ReloadListener listener) {
    }

}
