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
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.FileMetadata;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileChooserParameters implements ConnectionParameterFrame {

    public static class JStack extends FileChooserParameters {
        public JStack() {
            super(FileMetadata.JSTACK);
        }
    }

    public static class SavedSession extends FileChooserParameters {
        public SavedSession() {
            super(FileMetadata.SESSION);
        }
    }

    private JFileChooser chooser;

    private JPanel panel;

    private ConnectionType type;

    public FileChooserParameters() {
        this (new FileFilter[0]);
    }

    public FileChooserParameters(FileMetadata filters) {
        this(filters.preferredFilters);
    }

    public FileChooserParameters(FileFilter[] filters) {
        super();

        LookAndFeel saved = UIManager.getLookAndFeel();
        if (System.getProperty("os.name").toLowerCase().trim().contains("mac")) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }

        chooser = FileChooserHelper.newChooser(null, filters);

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
        if (file != null) {
            FileChooserHelper.setLastUsedDirectory(file);
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
    public void setReloadListener(ReloadListener listener) {
    }

}
