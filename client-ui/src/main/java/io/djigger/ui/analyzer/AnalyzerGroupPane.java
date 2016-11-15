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
package io.djigger.ui.analyzer;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bson.types.ObjectId;

import io.djigger.aggregation.AnalyzerService;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.sequencetree.SequenceTreeView;
import io.djigger.ui.Session;
import io.djigger.ui.Session.SessionType;
import io.djigger.ui.common.EnhancedTabbedPane;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.instrumentation.InstrumentationEventPane;
import io.djigger.ui.instrumentation.SubscriptionPane;


public class AnalyzerGroupPane extends EnhancedTabbedPane implements ChangeListener {

	private static final long serialVersionUID = -894031577206042607L;

	private final Session parent;
	
	protected final AnalyzerService analyzerService;
			
	private final NodePresentationHelper presentationHelper;
	
	private final List<AnalyzerPaneListener> listeners;

	public AnalyzerGroupPane(final Session parent, final NodePresentationHelper presentationHelper) {
		super(false);
		this.parent = parent;
		listeners = new ArrayList<AnalyzerPaneListener>();
        addChangeListener(this);
        analyzerService = new AnalyzerService();
        this.presentationHelper = presentationHelper;  
        
        final AnalyzerGroupPane me = this;
        setAddTabAction(new Runnable() {
			
			@Override
			public void run() {
				Point p1 = MouseInfo.getPointerInfo().getLocation();
	    		SwingUtilities.convertPointFromScreen(p1, me);
	    		
	    		AddTabPopUp popup = new AddTabPopUp(me);
	    		popup.show(me, p1.x, p1.y);
			}
		});
	}

    @SuppressWarnings("serial") class AddTabPopUp extends JPopupMenu {
    	    	
		public AddTabPopUp(final AnalyzerGroupPane groupPane){
        	        	
            add(new JMenuItem(new AbstractAction("Tree View") {
				@Override
				public void actionPerformed(ActionEvent e) {
			        TreeView normalAnalyzer = new TreeView(groupPane, TreeType.NORMAL);
			        addTab(normalAnalyzer, e.getActionCommand(), true);
			    	setVisible(false);
				}

			}));
            add(new JMenuItem(new AbstractAction("Reverse Tree View") {
				@Override
				public void actionPerformed(ActionEvent e) {
			        TreeView normalAnalyzer = new TreeView(groupPane, TreeType.REVERSE);
			        addTab(normalAnalyzer, e.getActionCommand(), true);
			    	setVisible(false);
				}

			}));
            add(new JMenuItem(new AbstractAction("Block View") {
				@Override
				public void actionPerformed(ActionEvent e) {
					BlockView normalAnalyzer = new BlockView(groupPane, TreeType.NORMAL);
			        addTab(normalAnalyzer, e.getActionCommand(), true);
			    	setVisible(false);
				}

			}));
            add(new JMenuItem(new AbstractAction("Reverse Block View") {
				@Override
				public void actionPerformed(ActionEvent e) {
					BlockView normalAnalyzer = new BlockView(groupPane, TreeType.REVERSE);
					addTab(normalAnalyzer, e.getActionCommand(), true);
			    	setVisible(false);
				}

			}));
            add(new JMenuItem(new AbstractAction("Instrumentation events") {
				@Override
				public void actionPerformed(ActionEvent e) {
					InstrumentationEventPane view = new InstrumentationEventPane(parent, groupPane);
					addTab(view, e.getActionCommand(), true);
			    	setVisible(false);
				}

			}));
            
            add(new JMenuItem(new AbstractAction("Subscriptions") {
				@Override
				public void actionPerformed(ActionEvent e) {
					addSubscriptionPane();
			    	setVisible(false);
				}

			}));    
        }
    }
    
    public void addDrilldownPane(ObjectId parentID) {
    	Iterator<InstrumentationEvent> it = parent.getStoreClient().getInstrumentationAccessor().getByParentId(parentID);
    	if(it.hasNext()) {
    		InstrumentationEvent event = it.next();
    		addSequenceTreePane(event.getTransactionID());
    	} else {
			JOptionPane.showMessageDialog(parent, "No child transaction found.", "Drilldown", JOptionPane.INFORMATION_MESSAGE);
    	}
    	
    }
    
    public void addSequenceTreePane(UUID trID) {
    	SequenceTreeView view = new SequenceTreeView(this, TreeType.NORMAL, trID);
		addTab(view, "Sequence tree "+trID.toString(), true);
    }
    
    public void addInstrumentationEventPaneForTransaction(UUID trID) {
    	InstrumentationEventPane view = new InstrumentationEventPane(parent, "trid="+trID, this);
		addTab(view, "Event list "+trID.toString(), true);
    }
    
    public void addSubscriptionPane() {
    	SubscriptionPane pane = new SubscriptionPane(parent);
    	addTab(pane, "Subscriptions", true);
    }
    
    public void setSamples(List<RealNodePathWrapper> pathSamples) {
    	analyzerService.load(pathSamples);
    	refresh();
    }
    
	public void refresh() {	
    	for(Component component:getComponents()) {
            if(component instanceof Dashlet) {
                ((Dashlet)component).refresh();
            }
        }
    }

    public void initialize() {
        TreeView normalAnalyzer = new TreeView(this, TreeType.NORMAL);
        addTab(normalAnalyzer, "Tree view", true);
        TreeView reverseAnalyzer = new TreeView(this, TreeType.REVERSE);
        addTab(reverseAnalyzer, "Reverse tree view", true);
        BlockView blockPane = new BlockView(this, TreeType.NORMAL);
        addTab(blockPane, "Block view", true);
        BlockView testPane = new BlockView(this, TreeType.REVERSE);
        addTab(testPane, "Reverse block view", true);
        if(parent.getSessionType()!=SessionType.JMX&&parent.getSessionType()!=SessionType.FILE) {
        	addInstrumentationEventPane();
			addSubscriptionPane();
        }
        setSelectedIndex(0);
    }

	private void addInstrumentationEventPane() {
		InstrumentationEventPane view = new InstrumentationEventPane(parent, this);
		addTab(view, "Instrumentation events", true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {		
		Component selection = getCurrentTab();
		if(selection!=null && selection instanceof AnalyzerPane) {
			((AnalyzerPane)selection).resetFocus();
		}
	}

	public void addListener(AnalyzerPaneListener listener) {
		listeners.add(listener);
	}

	public void fireSelection(Set<Long> selectedThreadIds) {
		for(AnalyzerPaneListener listener:listeners) {
			listener.onSelection(selectedThreadIds);
		}
	}

	public Session getMain() {
		return parent;
	}

	public NodePresentationHelper getPresentationHelper() {
		return presentationHelper;
	}

	public AnalyzerService getAnalyzerService() {
		return analyzerService;
	}
}
