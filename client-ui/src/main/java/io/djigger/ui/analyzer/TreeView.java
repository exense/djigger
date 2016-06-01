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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.RealNodeAggregation;


@SuppressWarnings("serial")
public class TreeView extends AnalyzerPane implements TreeSelectionListener {

	private AnalysisNode currentNode;

	private final JTree tree;

	public TreeView(final AnalyzerGroupPane parent, TreeType treeType) {
		super(parent, treeType);
		
		tree = new JTree(new NodeTreeModel(workNode));
		tree.setUI(new CustomTreeUI());
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {	
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean sel, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				if(value instanceof AnalysisNode) {
					AnalysisNode node = (AnalysisNode) value;
					value = parent.getPresentationHelper().toString(node);
				}
				return super.getTreeCellRendererComponent(tree, value,
						sel, expanded, leaf, row, hasFocus);
			}
		});
		
		final TreePopupMenu popup = new TreePopupMenu(this);
		// using the mouselistener instead of tree.setComponentPopupMenu() to also select items on right click
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(),e.getY());
					tree.setSelectionPath(selPath);
					if (selRow > -1) {
						tree.setSelectionRow(selRow);
					}
					popup.show(tree, e.getX(), e.getY());
				}
			}
		};
		tree.addMouseListener(ml);
		
		contentPanel.setLayout(new GridLayout(0,1));
		contentPanel.add(new JScrollPane(tree));
		
		
		// OSX support for RIGHT and LEFT KeyStrokes
		InputMap currentIm = tree.getInputMap();
		
		if(currentIm != null){
			String keyMapping = (String)currentIm.get(KeyStroke.getKeyStroke("pressed RIGHT"));
			if(keyMapping == null || keyMapping != "selectChild")
				currentIm.put(KeyStroke.getKeyStroke("pressed RIGHT"), "selectChild");
			
			keyMapping = (String)currentIm.get(KeyStroke.getKeyStroke("pressed LEFT"));
			if(keyMapping == null || keyMapping != "selectParent")
				currentIm.put(KeyStroke.getKeyStroke("pressed LEFT"), "selectParent");
		}

	}

	public void refreshDisplay() {
		Enumeration<TreePath> expandedDesc = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
		TreePath selectionPath = tree.getSelectionPath();
		
		NodeTreeModel model = new NodeTreeModel(workNode);
		tree.setModel(model);
		
		if(expandedDesc!=null) {
			while(expandedDesc.hasMoreElements()) {
				TreePath path = expandedDesc.nextElement();
				tree.expandPath(path);
			}
		}
		if(selectionPath!=null) {
			tree.setSelectionPath(selectionPath);
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath newSelectionPath = e.getNewLeadSelectionPath();
		if(newSelectionPath!=null) {
			currentNode = (AnalysisNode) newSelectionPath.getLastPathComponent();
			parent.fireSelection(getSelectedThreadsIds());
		}
	}
	
	private Set<Long> getSelectedThreadsIds() {
		HashSet<Long> result = new HashSet<Long>();

		AnalysisNode selectedNode = getSelectedNode();
		if(selectedNode!=null) {
			for(RealNodeAggregation aggregation:selectedNode.getAggregations()) {
				for(io.djigger.aggregation.Thread thread:aggregation.getAggregation().getSamples()) {
					result.add(thread.getId());
				}
			}
		}
		
		return result;
	}

	@Override
	public AnalysisNode getSelectedNode() {
		return currentNode;
	}
	
	public void expandAll() {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public void expandChildrenOfCurrentSelection() {
		expandChildren(getSelectedNode());
	}
	
	private void expandChildren(AnalysisNode node) {
		for(AnalysisNode child:node.getChildren()) {
			if(child.isLeaf()) {
				tree.expandPath(new TreePath(node.getTreePath().toArray()));
			} else {
				expandChildren(child);
			}
		}
	}
	
	public void expandFirstChildrenOfCurrentSelection() {
		expandFirstChildren(getSelectedNode());
	}
	
	private void expandFirstChildren(AnalysisNode node) {
		for(AnalysisNode child:node.getChildren()) {
			if(child.isLeaf()) {
				tree.expandPath(new TreePath(node.getTreePath().toArray()));
			} else {
				expandFirstChildren(child);
			}
			break;
		}
	}
	
	public void collapseChildrenOfCurrentSelection() {
		collapseChildren(getSelectedNode());
	}
	
	private void collapseChildren(AnalysisNode node) {
		for(AnalysisNode child:node.getChildren()) {
			collapseChildren(child);
			tree.collapsePath(new TreePath(node.getTreePath().toArray()));
		}
	}	
}
