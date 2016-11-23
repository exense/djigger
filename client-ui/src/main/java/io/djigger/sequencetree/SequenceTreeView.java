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
package io.djigger.sequencetree;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.UUID;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import io.djigger.ui.analyzer.AnalyzerGroupPane;
import io.djigger.ui.analyzer.TreeType;
import io.djigger.ui.model.PseudoInstrumentationEvent;


@SuppressWarnings("serial")
public class SequenceTreeView extends SequenceTreePane implements TreeSelectionListener {

	private SequenceTreeNode currentNode;

	private JTree tree;

	public SequenceTreeView(final AnalyzerGroupPane parent, TreeType treeType, UUID transactionID) {
		super(parent, treeType, transactionID);
		initView(parent);
	}
	
	public SequenceTreeView(final AnalyzerGroupPane parent, TreeType treeType, PseudoInstrumentationEvent pseudoEvent) {
		super(parent, treeType, pseudoEvent);
		initView(parent);
	}

	private void initView(final AnalyzerGroupPane parent) {
		tree = new JTree(new SequenceTreeModel(workNode));
		//tree.setUI(new SeqT());
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {	
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean sel, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				if(value instanceof SequenceTreeNode) {
					SequenceTreeNode node = (SequenceTreeNode) value;
					value = parent.getPresentationHelper().toString(node);
				}
				return super.getTreeCellRendererComponent(tree, value,
						sel, expanded, leaf, row, hasFocus);
			}
		});
		
		final SequenceTreePopupMenu popup = new SequenceTreePopupMenu(this);
		
//		final TreePopupMenu popup = new TreePopupMenu(this);
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
	}

	public void refreshDisplay() {
		Enumeration<TreePath> expandedDesc = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
		TreePath selectionPath = tree.getSelectionPath();
		
		SequenceTreeModel model = new SequenceTreeModel(workNode);
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
			currentNode = (SequenceTreeNode) newSelectionPath.getLastPathComponent();
			//parent.fireSelection(currentNode);
		}
	}
	
	public void eventDetails() {
		getSelectedNode().getEvent();
	}
	
	public void expandAll() {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public void expandChildrenOfCurrentSelection() {
		expandChildren(getSelectedNode());
	}
	
	private void expandChildren(SequenceTreeNode node) {
		for(SequenceTreeNode child:node.getChildren()) {
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
	
	private void expandFirstChildren(SequenceTreeNode node) {
		for(SequenceTreeNode child:node.getChildren()) {
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
	
	private void collapseChildren(SequenceTreeNode node) {
		for(SequenceTreeNode child:node.getChildren()) {
			collapseChildren(child);
			tree.collapsePath(new TreePath(node.getTreePath().toArray()));
		}
	}

	@Override
	protected SequenceTreeNode getSelectedNode() {
		return currentNode;
	}	
}
