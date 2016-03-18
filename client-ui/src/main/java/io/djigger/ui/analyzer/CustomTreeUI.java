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

import java.util.List;

import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;

public class CustomTreeUI extends MetalTreeUI {

	@Override
	protected void toggleExpandState(TreePath path) {
		if(!tree.isExpanded(path)) {
			Node node = (Node) path.getLastPathComponent();
			path = getExpandPath(node);

		    int row = getRowForPath(tree, path);

		    tree.expandPath(path);
		    updateSize();
		    if(row != -1) {
			if(tree.getScrollsOnExpand())
			    ensureRowsAreVisible(row, row + treeState.getVisibleChildCount(path));
			else
			    ensureRowsAreVisible(row, row);
		    }
		}
		else {
		    tree.collapsePath(path);
		    updateSize();
		}
	}
	
	private TreePath getExpandPath(Node node) {
		List<Node> children = node.getChildren();
		if(children.size()==1 && !children.get(0).isLeaf()) {
			return getExpandPath(node.getChildren().get(0));
		} else if (children.size() > 1){
			Node mostImportantNode = null;
			for(Node child:children) {
				if(mostImportantNode == null || mostImportantNode.getWeight()<child.getWeight()) {
					mostImportantNode = child;
				}
			}
			if(mostImportantNode.getWeight()>node.getWeight()*0.8) {
				return getExpandPath(mostImportantNode);
			} else {
				return new TreePath(node.getTreePath().toArray());
			}
		} else {
			return new TreePath(node.getTreePath().toArray());
		}
	}

}
