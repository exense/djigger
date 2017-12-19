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

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import io.djigger.monitoring.java.model.Metric;

public class MetricTreeModel implements TreeModel {
		
	private MetricNode root = new MetricNode();
	
	public MetricTreeModel() {
		super();
		root.name = "Root";
	}
	
	public MetricTreeModel(List<Metric<?>> metrics) {
		this();
		load(metrics);
	}

	@Override
	public Object getRoot() {
		return root;
	}
	
	public static class MetricNode {
		
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
		
	}
	
	public void load(List<Metric<?>> metrics) {
		root.children.clear();
		for(Metric<?> m:metrics) {
			MetricNode child = findChildByName(root.children, m.getName());
			if(child==null) {
				child = new MetricNode();
				child.name = m.getName();
				root.children.add(child);
			}
			
			if(m.getValue() instanceof JsonObject) {
				loadJson(child, (JsonObject)m.getValue());
			} else {
				// This is a leaf. Nothing else to do here.
			}
			
		}
	}

	public MetricNode findChildByName(List<MetricNode> children, String name) {
		for(MetricNode node:children) {
			if(node.name.equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	public void loadJson(MetricNode parent, JsonObject o) {
		for(String key:o.keySet()) {
			MetricNode child = findChildByName(parent.children, key);
			if(child==null) {
				child = new MetricNode();
				child.name = key;
				parent.children.add(child);
			}
			JsonValue value = o.get(key);
			if(value instanceof JsonObject) {
				loadJson(child, (JsonObject)value);
			} else if(value instanceof JsonArray) {
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
			if(m.children != null) {
				return m.children.get(index);
			} else {
				throw new RuntimeException("The node "+m+" has no children");
			}
		} else {
			throw new RuntimeException("Unsupported node "+_parent);
		}
	}

	@Override
	public int getChildCount(Object _parent) {
		if (_parent instanceof MetricNode) {
			MetricNode m = (MetricNode) _parent;
			if(m.children != null) {
				return m.children.size();
			} else {
				throw new RuntimeException("The node "+m+" has no children");
			}
		} else {
			throw new RuntimeException("Unsupported node "+_parent);
		}
	}

	@Override
	public boolean isLeaf(Object _node) {
		return getChildCount(_node)==0;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		
	}

	@Override
	public int getIndexOfChild(Object _parent, Object _child) {
		for(int i=0;i<getChildCount(_parent);i++) {
			if(getChild(_parent, i)==_child) {
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
