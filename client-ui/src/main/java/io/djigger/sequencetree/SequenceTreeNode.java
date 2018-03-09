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

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ui.model.NodeID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class SequenceTreeNode implements Comparable<SequenceTreeNode> {

    private final NodeID id;

    private SequenceTreeNode parent;

    private final List<SequenceTreeNode> children = new ArrayList<SequenceTreeNode>();

    private InstrumentationEvent event;

    public SequenceTreeNode(SequenceTreeNode parent, NodeID id) {
        super();
        this.id = id;
        this.parent = parent;
    }

    public SequenceTreeNode() {
        this.id = null;
        this.parent = null;
    }

    public InstrumentationEvent getEvent() {
        return event;
    }

    public void setEvent(InstrumentationEvent event) {
        this.event = event;
    }

    public void sort() {
        for (SequenceTreeNode child : getChildren()) {
            child.sort();
        }
        Collections.sort(children);
    }

    public SequenceTreeNode getChildByID(NodeID id) {
        for (SequenceTreeNode child : children) {
            if (id.equals(child.getId())) {
                return child;
            }
        }
        return null;
    }

    public List<SequenceTreeNode> getChildren() {
        return children;
    }

    public boolean addChildren(Collection<? extends SequenceTreeNode> c) {
        return children.addAll(c);
    }

    public boolean addChild(SequenceTreeNode e) {
        return children.add(e);
    }

    public SequenceTreeNode getParent() {
        return parent;
    }

    public void setParent(SequenceTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(SequenceTreeNode o) {
        return event.getId().compareTo(o.event.getId());
    }

    public NodeID getId() {
        return id;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public SequenceTreeNode getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }

    public List<SequenceTreeNode> getTreePath() {
        ArrayList<SequenceTreeNode> path = new ArrayList<SequenceTreeNode>();
        SequenceTreeNode currentNode = this;
        while (currentNode != null) {
            path.add(0, currentNode);
            currentNode = currentNode.parent;
        }
        return path;
    }

    @Override
    public String toString() {
        if (id != null) {
            return id.toString();
        } else {
            return super.toString();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SequenceTreeNode other = (SequenceTreeNode) obj;
        if (event == null) {
            if (other.event != null)
                return false;
        } else if (!event.equals(other.event))
            return false;
        return true;
    }

}
