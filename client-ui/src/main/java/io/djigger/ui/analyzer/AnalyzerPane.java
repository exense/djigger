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

import io.djigger.aggregation.Aggregation;
import io.djigger.aggregation.Aggregator;
import io.djigger.aggregation.DefaultPathTransformer;
import io.djigger.aggregation.PathTransformer;
import io.djigger.aggregation.RevertTreePathTransformer;
import io.djigger.aggregation.filter.BranchFilterFactory;
import io.djigger.aggregation.filter.Filter;
import io.djigger.aggregation.filter.FilterFactory;
import io.djigger.aggregation.filter.NodeFilterFactory;
import io.djigger.aggregation.filter.ParsingException;
import io.djigger.model.NodeID;
import io.djigger.model.RealNodePath;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import io.djigger.monitoring.java.instrumentation.subscription.SimpleInstrumentationSubscription;
import io.djigger.ui.Session;
import io.djigger.ui.common.EnhancedTextField;
import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.instrumentation.InstrumentationPaneListener;
import io.djigger.ui.model.Node;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;


public abstract class AnalyzerPane extends JPanel implements ActionListener, InstrumentationPaneListener {

	private static final long serialVersionUID = -8452799701138552693L;

	protected final Session main;

	protected final AnalyzerGroupPane parent;

	private Filter<NodeID> nodeFilter;

	protected Node workNode;

	private EnhancedTextField filterTextField;

	private EnhancedTextField excludeTextField;

	protected final TreeType treeType;

	protected JPanel contentPanel;

	private final String STACKTRACE_FILTER = "Stacktrace filter (and, or, not operators allowed)";
	private final String NODE_FILTER = "Node filter (and, or, not operators allowed)";

	protected AnalyzerPane(AnalyzerGroupPane parent, TreeType treeType) {
		super(new BorderLayout());

		this.parent = parent;
		this.main = parent.getMain();
		this.treeType = treeType;

		JPanel filterPanel = new JPanel(new GridLayout(0,1));

		filterTextField = new EnhancedTextField(STACKTRACE_FILTER);
		filterTextField.setToolTipText(STACKTRACE_FILTER);
		filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		filterTextField.addActionListener(this);
		filterPanel.add(filterTextField);

		excludeTextField = new EnhancedTextField(NODE_FILTER);
		excludeTextField.setToolTipText(NODE_FILTER);
		excludeTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		excludeTextField.addActionListener(this);
		filterPanel.add(excludeTextField);

		contentPanel = new JPanel();

		add(filterPanel, BorderLayout.PAGE_START);
		add(contentPanel, BorderLayout.CENTER);

		transform();

		main.getInstrumentationPane().addListener(this);
	}

	public void refresh() {
		transform();
		refreshDisplay();
	}

	public void mergedCalls() {
		//Node currentNode = getSelectedNode();
		//Node root = new Node();
		//TODO: root.setOverallTotalCount(rootNode.updateTotalCount());
		//filteredRootNode.extractMerge(currentNode.getMethodName(), root);
		//TreeView mergedCallAnalyzer = new TreeView(parent, TreeType.MERGE, root);
		//parent.addTab(mergedCallAnalyzer);
	}

	public void setFilterOnCurrentSelection() {
		filterTextField.setText(getPresentationHelper().getFullname(getSelectedNode()));
		refresh();
	}

	public void skipCurrentSelection() {
		String newSkipText = excludeTextField.getText();
		if(newSkipText!=null && newSkipText.length()>0 && !newSkipText.endsWith(",")) {
			newSkipText += ",";
		}
		newSkipText += getPresentationHelper().getFullname(getSelectedNode());
		excludeTextField.setText(newSkipText);
		refresh();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		refresh();
	}

	public String getStacktraceFilter() {
		return filterTextField.getText();
	}

	public void setStacktraceFilter(String filter) {
		filterTextField.setText(filter);
	}

	public String getNodeFilter() {
		return excludeTextField.getText();
	}

	public void setNodeFilter(String filter) {
		excludeTextField.setText(filter);
	}

	private Filter<RealNodePath> parseBranchFilter() {
		Filter<RealNodePath> complexFilter = null;
		String filter = getStacktraceFilter();
		if(filter!=null) {
			BranchFilterFactory atomicFactory = new BranchFilterFactory(getPresentationHelper());
			FilterFactory<RealNodePath> factory = new FilterFactory<RealNodePath>(atomicFactory);
			try {
				complexFilter = factory.getCompositeFilter(filter);
			} catch (ParsingException e) {
				JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return complexFilter;
	}

	private Filter<NodeID> parseNodeFilter() {
		String excludeFilter = getNodeFilter();
		if(excludeFilter!=null) {
			NodeFilterFactory atomicFactory = new NodeFilterFactory(getPresentationHelper());
			FilterFactory<NodeID> factory = new FilterFactory<NodeID>(atomicFactory);
			try {
				nodeFilter = factory.getCompositeFilter(excludeFilter);
			} catch (ParsingException e) {
				JOptionPane.showMessageDialog(this,	e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				nodeFilter = null;
			}
		} else {
			nodeFilter = null;
		}
		return nodeFilter;
	}

	private void transform() {
		Aggregator aggregator = parent.getAggregator();

		Filter<RealNodePath> branchFilter = parseBranchFilter();
		List<Aggregation> aggregations = aggregator.query(branchFilter);

		Filter<NodeID> nodeFilter = parseNodeFilter();
		PathTransformer pathTransformer;
		if(treeType == TreeType.REVERSE) {
			pathTransformer = new RevertTreePathTransformer(nodeFilter);
		} else {
			pathTransformer = new DefaultPathTransformer(nodeFilter);
		}

		Node aggregationTreeNode = Node.buildNode(aggregations, pathTransformer, nodeFilter);

		workNode = aggregationTreeNode;
	}

	public void instrumentCurrentMethod() {
		NodeID nodeID = getSelectedNode().getId();
		
		main.addSubscription(new SimpleInstrumentationSubscription(false, nodeID.getClassName(), nodeID.getMethodName()));
	}	
	
	public void instrumentCurrentNode() {
		if(nodeFilter==null) {
//			main.getFacade().instrument(new NodeSubscription(getSelectedNode().getId(), false));
			// TODO main.addSubscription(new RealNodePathSubscription(getSelectedNode().getPath()., false));
		} else {
			JOptionPane.showMessageDialog(this,
				    "Instrumentation impossible when packages are skipped. Please remove the exclusion criteria and try again.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}

	public abstract void refreshDisplay();

	protected abstract Node getSelectedNode();

	public Session getMain() {
		return main;
	}

	public void onSelection(Set<InstrumentSubscription> subscriptions) {
		repaint();
	}

	public NodePresentationHelper getPresentationHelper() {
		return parent.getPresentationHelper();
	}

	public void resetFocus() {
		requestFocusInWindow();
	}
}
