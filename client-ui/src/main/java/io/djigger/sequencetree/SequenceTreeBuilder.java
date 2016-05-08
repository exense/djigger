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

import java.util.Stack;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ql.Filter;

public class SequenceTreeBuilder {

	public SequenceTreeNode build(InstrumentationEventNode realTree, Filter<Stack<InstrumentationEvent>> pathTransformer, Filter<InstrumentationEvent> nodeFilter) {
		SequenceTreeNode root = new SequenceTreeNode();
		
		
		Stack<InstrumentationEvent> stack = new Stack<>();
		addChildren(root, stack, realTree, pathTransformer, nodeFilter);


		return root;
	}

	private boolean addChildren(SequenceTreeNode currentTargetNode, Stack<InstrumentationEvent> stack, InstrumentationEventNode currentNode, Filter<Stack<InstrumentationEvent>> pathTransformer, Filter<InstrumentationEvent> nodeFilter) {
		stack.push(currentNode.getEvent());
		boolean retain = false;
		if(currentNode.getChildren()!=null&&currentNode.getChildren().size()>0) {
			for(InstrumentationEventNode child:currentNode.getChildren()) {
				if(nodeFilter==null||nodeFilter.isValid(child.getEvent())) {
					SequenceTreeNode targetChild = new SequenceTreeNode();
					targetChild.setEvent(child.getEvent());
					if(addChildren(targetChild, stack, child, pathTransformer, nodeFilter)) {
						currentTargetNode.addChild(targetChild);
						retain = true;
					}
				} else {
					stack.push(child.getEvent());
					if(addChildren(currentTargetNode, stack, child, pathTransformer, nodeFilter)) {
						retain = true;
					}
					stack.pop();
				}
			}
		} else {
			// reached leaf
			retain = pathTransformer==null|| pathTransformer.isValid(stack);
		}
		stack.pop();
		return retain;
	}
}
