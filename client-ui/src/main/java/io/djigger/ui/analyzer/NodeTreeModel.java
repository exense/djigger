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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class NodeTreeModel implements TreeModel {

    private final AnalysisNode root;

    public NodeTreeModel(AnalysisNode root) {
        super();
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object _parent, int index) {
        AnalysisNode parent = (AnalysisNode) _parent;
        return parent.getChildren().get(index);
    }

    @Override
    public int getChildCount(Object _parent) {
        AnalysisNode parent = (AnalysisNode) _parent;
        return parent.getChildren().size();
    }

    @Override
    public boolean isLeaf(Object _node) {
        AnalysisNode node = (AnalysisNode) _node;
        return node.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object _parent, Object _child) {
        AnalysisNode parent = (AnalysisNode) _parent;
        AnalysisNode child = (AnalysisNode) _child;
        for (int i = 0; i < parent.getChildren().size(); i++) {
            if (parent.getChildren().get(i).equals(child)) {
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
