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

import com.thoughtworks.xstream.XStream;
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.common.FileChooserHelper;
import io.djigger.ui.common.FileMetadata;
import io.djigger.ui.common.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFrame extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private final JFrame frame;

    private final MainToolbarPane mainToolbar;

    private final SessionGroupPane groupPane;

    private final List<Session> sessions;

    private final ArgumentParser options;

    private boolean outOfMemoryPrevention = false;

    private final OutOfMemoryPreventionPane outOfMemoryPreventionPane;

    public MainFrame(final ArgumentParser options) {
        super(new BorderLayout());

        this.options = options;

        this.sessions = new ArrayList<Session>();

        if (System.getProperties().getProperty("heapMonitor", "false").equals("true")) {
            new HeapMonitor(this);
        }


        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(WindowsLookAndFeel));
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

        frame = new JFrame("djigger 1.10.1");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1300, 700));

        java.net.URL imgURL = getClass().getResource("logo_small.png");
        ImageIcon icon = new ImageIcon(imgURL);
        frame.setIconImage(icon.getImage());

        mainToolbar = new MainToolbarPane(this);

        groupPane = new SessionGroupPane(this);

        add(mainToolbar, BorderLayout.PAGE_END);

        //JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        //split.setLeftComponent(selectionPane);
        //split.setRightComponent(groupPane);

        outOfMemoryPreventionPane = new OutOfMemoryPreventionPane(this);
        outOfMemoryPreventionPane.setVisible(false);

        //add(outOfMemoryPreventionPane, BorderLayout.PAGE_START);

        add(groupPane, BorderLayout.CENTER);

        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        // center on screen
        frame.setLocationRelativeTo(null);

        if (options.hasOption("sessionFile")) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    File sessionFile = new File(options.getOption("sessionFile"));
                    importSessionFile(sessionFile);
                }
            });
        }
    }

    public synchronized void addSession(final Session session) {
        try {
            session.start();
            session.configure();
            sessions.add(session);
            groupPane.addSession(session);
            groupPane.selectSession(session);
            exportSessions(new File("djigger_lastsession.xml"));
            displayWelcomeDialog(session);
            session.setupInitialState();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayWelcomeDialog(final Session session) {
        if (session.getSessionType() == SessionType.STORE && Settings.getINSTANCE().getAsBoolean("browserpane.connectionconfirmation", true)) {
            boolean showThisAgain = JOptionPane.showOptionDialog(this, "Connection succeeded\nEnter your query in the 'Store filter' (optional) and press enter to retrieve your data.", "Connection succeeded", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"OK", "Don't show this again"}, "OK") == 0;
            Settings.getINSTANCE().put("browserpane.connectionconfirmation", Boolean.toString(showThisAgain));
        }
    }

    public boolean isOutOfMemoryPreventionActive() {
        return outOfMemoryPrevention;
    }

    public synchronized void preventOutOfMemory() {
        outOfMemoryPrevention = true;
        outOfMemoryPreventionPane.setVisible(true);
        Thread msgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(getFrame(),
                    "The JVM is close to the OutOfMemory. Data collection has been stopped to prevent the JVM from running out of memory. All incomming messages from Agents will be ignored. Consider saving your session has soon has possible.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        msgThread.start();
    }

    public synchronized void disableOutOfMemory(boolean neverMonitorAgain) {
        if (!neverMonitorAgain) {
            new HeapMonitor(this);
        }

        outOfMemoryPreventionPane.setVisible(false);
        outOfMemoryPrevention = false;
    }

    public void removeCurrentSession() {
        Session currentSession = groupPane.getCurrentSession();
        removeSession(currentSession);
    }

    private synchronized void removeSession(Session session) {
        session.close();
        sessions.remove(session);
        groupPane.removeSession(session);
    }

    public synchronized void exportSessions() {
        File file = FileChooserHelper.saveFile(FileMetadata.SESSIONLIST);
        exportSessions(file);
    }

    private synchronized void exportSessions(File file) {
        if (file != null) {
            XStream xstream = new XStream();
            List<SessionConfiguration> configs = new ArrayList<SessionConfiguration>();
            try {
                for (Session session : sessions) {
                    configs.add(session.getConfig());
                }
                xstream.toXML(configs, new FileWriter(file));
            } catch (IOException e) {
                logger.error("Error while saving session list", e);
            }
        }
    }

    public synchronized void importSessions() {
        File file = FileChooserHelper.loadFile(FileMetadata.SESSIONLIST);
        importSessionFile(file);
    }

    private void importSessionFile(File file) {
        XStream xstream = new XStream();
        if (file != null) {
            String configXml;
            try {
                configXml = new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException("Error while reading file " + file, e);
            }

            configXml = replacePlaceholders(configXml);

            @SuppressWarnings("unchecked")
            List<SessionConfiguration> configs = (List<SessionConfiguration>) xstream.fromXML(configXml);
            for (SessionConfiguration config : configs) {
                Session session = new Session(config, this);
                addSession(session);
            }
        }
    }

    private String replacePlaceholders(String configXml) {
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\$\\{(.+?)\\}").matcher(configXml);
        while (m.find()) {
            String key = m.group(1);
            String replacement = options.getOption(key);
            m.appendReplacement(sb, replacement);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void selectSession(Session session) {
        if (session != null) {
            groupPane.selectSession(session);
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public SessionGroupPane getGroupPane() {
        return groupPane;
    }

    public ArgumentParser getOptions() {
        return options;
    }

    public MainToolbarPane getMainToolbar() {
        return mainToolbar;
    }

    public static void main(String[] args) {
        ArgumentParser options = new ArgumentParser(args);
        new MainFrame(options);
    }
}
