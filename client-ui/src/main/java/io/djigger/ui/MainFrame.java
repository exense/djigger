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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import io.djigger.ui.common.FileChooserHelper;

public class MainFrame extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private final JFrame frame;
        
    private final MainToolbarPane mainToolbar;
    
    private final SessionSelectionPane selectionPane;
    
    private final SessionGroupPane groupPane;
    
    private final List<Session> sessions;
    
    private final ArgumentParser options;
    
    private boolean outOfMemoryPrevention = false;
    
    private final OutOfMemoryPreventionPane outOfMemoryPreventionPane;
    
	public MainFrame(ArgumentParser options) {
		super(new BorderLayout());
		
		this.options = options;
		
		this.sessions = new ArrayList<Session>();
		
		if(System.getProperties().getProperty("heapMonitor","false").equals("true")) {
			new HeapMonitor(this);
		}
		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(WindowsLookAndFeel));
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		
       	frame = new JFrame("dJigger 1.4.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1300,700));
        
        java.net.URL imgURL = getClass().getResource("logo_small.png");
		ImageIcon icon = new ImageIcon(imgURL);
        frame.setIconImage(icon.getImage());
        
        mainToolbar = new MainToolbarPane(this);
        
        selectionPane = new SessionSelectionPane(this);
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
	}
	
	public synchronized void addSession(final Session session) {
		try {
			session.start();
			sessions.add(session);
			selectionPane.addSession(session);
			groupPane.addSession(session);
			groupPane.selectSession(session);
			exportSessions(new File("djigger_lastsession.xml"));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
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
		
		for(Session session:sessions) {
			session.getStore().clearBuffers();
		}
	}
	
	public synchronized void disableOutOfMemory(boolean neverMonitorAgain) {
		if(!neverMonitorAgain) {
			new HeapMonitor(this);
		}
		
		outOfMemoryPreventionPane.setVisible(false);
		outOfMemoryPrevention = false;
	}
	
	public void removeCurrentSession() {
		Session currentSession = groupPane.getCurrentSession();
		removeSession(currentSession);
	}
	
	public synchronized void removeSession(Session session) {
		session.close();
		sessions.remove(session);
		selectionPane.removeSession(session);
		groupPane.removeSession(session);
	}
	
	public synchronized void exportSessions() {
		File file = FileChooserHelper.selectFile("Export session list", "Save");
		exportSessions(file);
	}
	
	public synchronized void exportSessions(File file) {
		if(file!=null) {
			XStream xstream = new XStream();
			List<SessionConfiguration> configs = new ArrayList<SessionConfiguration>();
			try {
				for(Session session:sessions) {
					configs.add(session.getConfig());
				}
				xstream.toXML(configs, new FileWriter(file));			
			} catch (IOException e) {
				logger.error("Error while exporting sessions",e);
			}
		}
	}
	
	public synchronized void importSessions() {
		XStream xstream = new XStream();
		File file = FileChooserHelper.selectFile("Import session list", "Open");
		if(file!=null) {
        	@SuppressWarnings("unchecked")
			List<SessionConfiguration> configs = (List<SessionConfiguration>) xstream.fromXML(file);
    		for(SessionConfiguration config:configs) {
    			Session session = new Session(config, this);
    			addSession(session);
    		}
        }
	}

	public void selectSession(Session session) {
		if(session!=null) {
			selectionPane.selectSession(session);
			groupPane.selectSession(session);
		}
	}
	
	public void handleSessionEvent(Session session, SessionEvent event) {
		selectionPane.refresh();
	}

	public JFrame getFrame() {
		return frame;
	}

	public SessionSelectionPane getSelectionPane() {
		return selectionPane;
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

	public static void main(String[] args)  {
		ArgumentParser options = new ArgumentParser(args);
    	new MainFrame(options);
    }
}
