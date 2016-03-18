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

import io.djigger.ui.model.Node;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;


@SuppressWarnings("serial")
public class TreeView extends AnalyzerPane implements TreeSelectionListener {

	private Node currentNode;

	private final JTree tree;

	public TreeView(final AnalyzerGroupPane parent, TreeType treeType) {
		super(parent, treeType);
		
		tree = new JTree(new NodeTreeModel(workNode));
		tree.setUI(new CustomTreeUI());
		tree.setComponentPopupMenu(new TreePopupMenu(this));
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {	
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean sel, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				if(value instanceof Node) {
					Node node = (Node) value;
					value = parent.getPresentationHelper().toString(node);
				}
				return super.getTreeCellRendererComponent(tree, value,
						sel, expanded, leaf, row, hasFocus);
			}
		});
		
		contentPanel.setLayout(new GridLayout(0,1));
		contentPanel.add(new JScrollPane(tree));
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
			currentNode = (Node) newSelectionPath.getLastPathComponent();
			parent.fireSelection(currentNode);
		}
	}

	@Override
	public Node getSelectedNode() {
		return currentNode;
	}
}
