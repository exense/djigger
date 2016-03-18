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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class NodeTreeModel implements TreeModel {
	
	private final Node root;

	public NodeTreeModel(Node root) {
		super();
		this.root = root;
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object _parent, int index) {
		Node parent = (Node) _parent;
		return parent.getChildren().get(index);
	}

	@Override
	public int getChildCount(Object _parent) {
		Node parent = (Node) _parent;
		return parent.getChildren().size();
	}

	@Override
	public boolean isLeaf(Object _node) {
		Node node = (Node) _node;
		return node.isLeaf();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		
	}

	@Override
	public int getIndexOfChild(Object _parent, Object _child) {
		Node parent = (Node) _parent;
		Node child = (Node) _child;
		for(int i=0;i<parent.getChildren().size();i++) {
			if(parent.getChildren().get(i).equals(child)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

}
