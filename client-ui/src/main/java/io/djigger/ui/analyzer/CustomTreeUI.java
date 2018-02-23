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

import io.djigger.ui.model.AnalysisNode;

import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;
import java.util.List;

public class CustomTreeUI extends MetalTreeUI {

    @Override
    protected void toggleExpandState(TreePath path) {
        if (!tree.isExpanded(path)) {
            AnalysisNode node = (AnalysisNode) path.getLastPathComponent();
            path = getExpandPath(node);

            int row = getRowForPath(tree, path);

            tree.expandPath(path);
            updateSize();
            if (row != -1) {
                if (tree.getScrollsOnExpand())
                    ensureRowsAreVisible(row, row + treeState.getVisibleChildCount(path));
                else
                    ensureRowsAreVisible(row, row);
            }
        } else {
            tree.collapsePath(path);
            updateSize();
        }
    }

    // This method should NEVER return a leaf as the tree.expandPath() only works with folder (nodes)
    private TreePath getExpandPath(AnalysisNode node) {
        List<AnalysisNode> children = node.getChildren();
        if (children.size() == 1 && !children.get(0).isLeaf()) {
            return getExpandPath(node.getChildren().get(0));
        } else if (children.size() > 1) {
            AnalysisNode mostImportantNode = null;
            for (AnalysisNode child : children) {
                if (mostImportantNode == null || mostImportantNode.getWeight() < child.getWeight()) {
                    mostImportantNode = child;
                }
            }
            if (mostImportantNode.getWeight() > node.getWeight() * 0.8 && !mostImportantNode.isLeaf()) {
                return getExpandPath(mostImportantNode);
            } else {
                return new TreePath(node.getTreePath().toArray());
            }
        } else {
            return new TreePath(node.getTreePath().toArray());
        }
    }

}
