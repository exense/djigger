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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.djigger.aggregation.Aggregator;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.common.EnhancedTabbedPane;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.Node;


public class AnalyzerGroupPane extends EnhancedTabbedPane implements ChangeListener {

	private static final long serialVersionUID = -894031577206042607L;

	private final Session parent;

	private final Aggregator aggregator;
	
	private final NodePresentationHelper presentationHelper;

	//private AnalyzerPane currentSelection;

	private final List<AnalyzerPaneListener> listeners;

	public AnalyzerGroupPane(final Session parent, final NodePresentationHelper presentationHelper) {
		super(false);
		this.parent = parent;
		listeners = new ArrayList<AnalyzerPaneListener>();
        addChangeListener(this);
        aggregator = new Aggregator(parent.getStore());
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
        }
    }
    
    public void setStoreFilter(StoreFilter storeFilter) {
    	aggregator.setStoreFilter(storeFilter);
    }

    public void refresh() {
    	aggregator.reload();
    	for(Component component:getComponents()) {
            if(component instanceof AnalyzerPane) {
                ((AnalyzerPane)component).refresh();
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
        setSelectedIndex(0);
    }

	@Override
	public void stateChanged(ChangeEvent e) {		
		Component selection = getCurrentTab();
		/*
		// This feature seems to disturb more than it helps
		if(selection!=null && currentSelection!=null) {
			String oldExclude = currentSelection.getNodeFilter();
			String oldFilter = currentSelection.getStacktraceFilter();
			String newExclude = selection.getNodeFilter();
			String newFilter = selection.getStacktraceFilter();

			if((oldExclude!=null &&
					!oldExclude.equals(newExclude)) ||
				(oldFilter!=null &&
						!oldFilter.equals(newFilter))) {
				if(JOptionPane.showConfirmDialog(parent.getFrame(),
						"Do you wish to reuse the filters of the previous view?") == JOptionPane.YES_OPTION) {
					selection.setStacktraceFilter(oldFilter);
					selection.setNodeFilter(oldExclude);
					selection.refresh();
				}
			}
		}
		currentSelection = selection; */
		if(selection!=null && selection instanceof AnalyzerPane) {
			((AnalyzerPane)selection).resetFocus();
		}
	}

	public void addListener(AnalyzerPaneListener listener) {
		listeners.add(listener);
	}

	protected void fireSelection(Node node) {
		for(AnalyzerPaneListener listener:listeners) {
			listener.onSelection(node);
		}
	}

	public Session getMain() {
		return parent;
	}

	public Aggregator getAggregator() {
		return aggregator;
	}

	public NodePresentationHelper getPresentationHelper() {
		return presentationHelper;
	}
	
	public void showLineNumbers(boolean show) {
		aggregator.setIncludeLineNumbers(show);
		refresh();
	}


}
