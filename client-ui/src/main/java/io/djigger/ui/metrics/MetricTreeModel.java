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
package io.djigger.ui.metrics;

import io.djigger.model.TaggedMetric;
import io.djigger.monitoring.java.model.Metric;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

public class MetricTreeModel implements TreeModel {

    private MetricNode root = new MetricNode();

    public MetricTreeModel() {
        super();
        root.name = "Root";
    }

    public MetricTreeModel(List<TaggedMetric> tMetrics) {
        this();
        load(tMetrics);
        this.sort(root);
    }

    private void sort(MetricNode node) {
        Collections.sort(node.children);
        for (MetricNode child: node.children) {
            sort(child);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    public static class MetricNode implements Comparable {

        String name;

        List<MetricNode> children = new ArrayList<>();

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
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
            MetricNode other = (MetricNode) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        @Override
        public int compareTo(Object o) {
            MetricNode compareTo = (MetricNode) o;
            return this.name.compareTo(compareTo.name);
        }
    }

    private MetricNode addMissingChildNode(MetricNode parent, String name) {
        MetricNode child = findChildByName(parent.children, name);
        if (child == null) {
            child = new MetricNode();
            child.name = name;
            parent.children.add(child);
        }
        return child;
    }

    public void load(List<TaggedMetric> tMetrics) {
        root.children.clear();
        for (TaggedMetric tm : tMetrics) {
            MetricNode rootOrTag = root;
            Map<String, String> tags = tm.getTags();
            if (tags != null && tags.size()>0) {
                rootOrTag = addMissingChildNode(root, tags.toString());
            }
            Metric m = tm.getMetric();
            MetricNode child = addMissingChildNode(rootOrTag, m.getName());

            if (m.getValue() instanceof HashMap) {
                loadJson(child, (HashMap<String, Object>) m.getValue());
            } else {
                // This is a leaf. Nothing else to do here.
            }

        }
    }

    public MetricNode findChildByName(List<MetricNode> children, String name) {
        for (MetricNode node : children) {
            if (node.name.equals(name)) {
                return node;
            }
        }
        return null;
    }

    public void loadJson(MetricNode parent, HashMap<String, Object> o) {
        for (String key : o.keySet()) {
            MetricNode child = findChildByName(parent.children, key);
            if (child == null) {
                child = new MetricNode();
                child.name = key;
                parent.children.add(child);
            }
            Object value = o.get(key);
            if (value instanceof HashMap) {
                loadJson(child, (HashMap<String, Object>) value);
            } else if (value instanceof ArrayList) {
                // todo
            } else {
                // this is a leaf. Nothing to do here
            }
        }
    }

    @Override
    public Object getChild(Object _parent, int index) {
        if (_parent instanceof MetricNode) {
            MetricNode m = (MetricNode) _parent;
            if (m.children != null) {
                return m.children.get(index);
            } else {
                throw new RuntimeException("The node " + m + " has no children");
            }
        } else {
            throw new RuntimeException("Unsupported node " + _parent);
        }
    }

    @Override
    public int getChildCount(Object _parent) {
        if (_parent instanceof MetricNode) {
            MetricNode m = (MetricNode) _parent;
            if (m.children != null) {
                return m.children.size();
            } else {
                throw new RuntimeException("The node " + m + " has no children");
            }
        } else {
            throw new RuntimeException("Unsupported node " + _parent);
        }
    }

    @Override
    public boolean isLeaf(Object _node) {
        return getChildCount(_node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object _parent, Object _child) {
        for (int i = 0; i < getChildCount(_parent); i++) {
            if (getChild(_parent, i) == _child) {
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
