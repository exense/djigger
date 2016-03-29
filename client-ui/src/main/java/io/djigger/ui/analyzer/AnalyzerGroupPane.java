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

package io.djigger.ui.analyzer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.djigger.aggregation.Aggregator;
import io.djigger.store.filter.StoreFilter;
import io.djigger.ui.Session;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.Node;


public class AnalyzerGroupPane extends JTabbedPane implements ChangeListener {

	private static final long serialVersionUID = -894031577206042607L;

	private final Session parent;

	private final Aggregator aggregator;
	
	private final NodePresentationHelper presentationHelper;

	//private AnalyzerPane currentSelection;

	private final List<AnalyzerPaneListener> listeners;

	public AnalyzerGroupPane(final Session parent, final NodePresentationHelper presentationHelper) {
		super();
		this.parent = parent;
		listeners = new ArrayList<AnalyzerPaneListener>();
        addChangeListener(this);
        aggregator = new Aggregator(parent.getStore());
        this.presentationHelper = presentationHelper;  
	}

    public void addTab(Component analyzer, String name) {
        add(name, analyzer);
    }

    public AnalyzerPane getCurrentTab() {
        Component _component = getSelectedComponent();
        if(_component instanceof AnalyzerPane) {
            return (AnalyzerPane) _component;
        }
        return null;
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
        addTab(normalAnalyzer, "Tree view");
        TreeView reverseAnalyzer = new TreeView(this, TreeType.REVERSE);
        addTab(reverseAnalyzer, "Reverse tree view");
        BlockView blockPane = new BlockView(this, TreeType.NORMAL);
        addTab(blockPane, "Block view");
        BlockView testPane = new BlockView(this, TreeType.REVERSE);
        addTab(testPane, "Reverse block view");
        setSelectedIndex(0);
    }

	@Override
	public void stateChanged(ChangeEvent e) {
		AnalyzerPane selection = getCurrentTab();
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
		selection.resetFocus();
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
