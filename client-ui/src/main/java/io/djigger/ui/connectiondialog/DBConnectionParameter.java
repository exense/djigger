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

public class DBConnectionParameter extends HostConnectionParameter {


    private final JTextField db;

    public DBConnectionParameter() {
        super();
        port.setText("27017");
        hostParametersPane.add(new JLabel("DB"));
        db = new JTextField(20);
        db.setText("djigger");
        hostParametersPane.add(db);
    }

    public String getDb() {
        return db.getText();
    }

    @Override
    public SessionConfiguration getSessionConfiguration() {
        SessionConfiguration config = super.getSessionConfiguration();
        config.getParameters().put(SessionParameter.DB, getDb());
        return config;
    }
}
