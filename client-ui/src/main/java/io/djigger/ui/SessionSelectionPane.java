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

import io.djigger.ui.Session.SessionType;
import io.djigger.ui.common.CommandButton;
import io.djigger.ui.connectiondialog.AgentConnectionDialog;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

public class SessionSelectionPane extends JPanel {

    private final JTree sessionTree;

    private final DefaultTreeModel treeModel;

    private final DefaultMutableTreeNode root;

    private final MainFrame main;

    public SessionSelectionPane(final MainFrame main) {
        super(new BorderLayout());

        this.main = main;

        root = new DefaultMutableTreeNode();
//		root.add(new DefaultMutableTreeNode(SessionType.AGENT));
//		root.add(new DefaultMutableTreeNode(SessionType.JMX));
//		root.add(new DefaultMutableTreeNode(SessionType.AGENT_CAPTURE));
//		root.add(new DefaultMutableTreeNode(SessionType.FILE));
//		root.add(new DefaultMutableTreeNode(SessionType.STORE));

        treeModel = new DefaultTreeModel(root);
        this.sessionTree = new JTree(treeModel);
        sessionTree.setCellRenderer(new SessionSelectionTreeRenderer());
        sessionTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Session session = getCurrentSelection();
                if (session != null) {
                    main.getGroupPane().selectSession(session);
                }
            }
        });

        sessionTree.setToggleClickCount(0);

        sessionTree.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    addSession();
                    e.consume();
                }
            }
        });

        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(new AbstractAction("Add session") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSession();
            }
        }));
//		menu.add(new JMenuItem(new AbstractAction("Rename") {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				addSession();
//			}
//		}));
        menu.add(new JMenuItem(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSession();
            }
        }));
        sessionTree.setComponentPopupMenu(menu);

        add(new JScrollPane(sessionTree), BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        commandPanel.add(new CommandButton("add.png", "Add session of selected type", new Runnable() {
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

        add(commandPanel, BorderLayout.SOUTH);

    }

    private void renameSession() {
        Session session = getCurrentSelection();
        if (session != null) {

        } else {
            JOptionPane.showMessageDialog(main.getFrame(), "Please select the session to be renamed.");
        }
    }

    private void addSession() {
        AgentConnectionDialog dialog = new AgentConnectionDialog(main);
        if (dialog.showAndWait()) {
            SessionConfiguration config = dialog.getSessionConfiguration();
            Session session = new Session(config, main);
            main.addSession(session);
        }
//
//		if(type == null || type == SessionType.AGENT || type == SessionType.JMX || type == SessionType.STORE) {
//		} else if (type == SessionType.FILE) {
//	        File file = FileChooserHelper.selectFile("Thread dumps file", "Open");
//			if(file!=null) {
//	        	SessionConfiguration config = new SessionConfiguration(file.getName(), SessionType.FILE);
//				config.getParameters().put(SessionParameter.FILE, file.getAbsolutePath());
//				Session session = new Session(config, main);
//				main.addSession(session);
//	        }
//		} else if (type == SessionType.AGENT_CAPTURE) {
//	        File file = FileChooserHelper.selectFile("Restore session", "Open");
//			if(file!=null) {
//	        	SessionConfiguration config = new SessionConfiguration(file.getName(), SessionType.AGENT_CAPTURE);
//				config.getParameters().put(SessionParameter.FILE, file.getAbsolutePath());
//				Session session = new Session(config, main);
//				main.addSession(session);
//	        }
//		}
    }

    public void refresh() {
        sessionTree.repaint();
    }

    private void removeSession() {
        Session session = getCurrentSelection();
        if (session != null) {
            main.removeSession(getCurrentSelection());
        } else {
            JOptionPane.showMessageDialog(main.getFrame(), "Please select the session to be removed.");
        }
    }

    private Session getCurrentSelection() {
        if (sessionTree.getSelectionPath() != null) {
            Object o = ((DefaultMutableTreeNode) sessionTree.getSelectionPath().getLastPathComponent()).getUserObject();
            if (o instanceof Session) {
                return (Session) o;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    public void addSession(Session session) {
        root.add(new DefaultMutableTreeNode(session));
        //treeModel.insertNodeInto(new DefaultMutableTreeNode(session), root, root.getChildCount());
    }

    public void selectSession(Session session) {
        DefaultMutableTreeNode node = searchSession(session);
        if (node != null) {
            if (sessionTree.getSelectionPath() == null || sessionTree.getSelectionPath().getLastPathComponent() != session) {
                sessionTree.setSelectionPath(new TreePath(node.getPath()));
            }
        }
    }

    public void removeSession(Session session) {
        DefaultMutableTreeNode node = searchSession(session);
        if (node != null) {
            treeModel.removeNodeFromParent(node);
        }
    }

    private DefaultMutableTreeNode searchSession(Session session) {
        for (Enumeration<DefaultMutableTreeNode> i = root.children(); i.hasMoreElements(); ) {
            DefaultMutableTreeNode child = i.nextElement();
            if (child.getUserObject().equals(session.getSessionType())) {
                for (Enumeration<DefaultMutableTreeNode> j = child.children(); j.hasMoreElements(); ) {
                    DefaultMutableTreeNode subchild = j.nextElement();
                    if (subchild.getUserObject().equals(session)) {
                        return subchild;
                    }
                }
            }
        }
        return null;
    }

    private class SessionSelectionTreeRenderer extends DefaultTreeCellRenderer {


        private SessionSelectionTreeRenderer() {
            super();

//			java.net.URL imgURL = getClass().getResource("");
//			ImageIcon icon = new ImageIcon(imgURL, toolTip);
//			icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_FAST));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof SessionType) {
                setText(((SessionType) node.getUserObject()).getDescription());
                setIcon(getDefaultClosedIcon());
            } else if (node.getUserObject() instanceof Session) {
                Session session = (Session) node.getUserObject();
                if (session.getSessionType() == SessionType.AGENT || session.getSessionType() == SessionType.JMX) {
                    if (session.isActive()) {
                        java.net.URL imgURL = getClass().getResource("connect.png");
                        ImageIcon icon = new ImageIcon(imgURL, "Connected");
                        setIcon(icon);
                    } else {
                        java.net.URL imgURL = getClass().getResource("disconnect.png");
                        ImageIcon icon = new ImageIcon(imgURL, "Disonnected");
                        setIcon(icon);
                        ;
                    }
                } else {
                    java.net.URL imgURL = getClass().getResource("text.png");
                    ImageIcon icon = new ImageIcon(imgURL, "/home/jerome/Downloads/text.png");
                    setIcon(icon);
                    ;
                }
            }

            return this;
        }

    }

}
