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
package io.djigger.aggregation.filter;

import io.djigger.ui.common.NodePresentationHelper;
import io.djigger.ui.model.NodeID;



public class NodeFilter implements Filter<NodeID>, ContextAwareFilter{

	private final String excludePattern;

	private final NodePresentationHelper presentationHelper;
	
	private static final String ENTRY_PREFIX = "entry:";
	private boolean isEntry = false;

	private boolean entryMatched=false;
	
	public NodeFilter(String excludePattern,
			NodePresentationHelper presentationHelper) {
		if(excludePattern.startsWith(ENTRY_PREFIX)) {
			excludePattern = excludePattern.substring(ENTRY_PREFIX.length());
			isEntry = true;
		}
		
		this.excludePattern = excludePattern;
		this.presentationHelper = presentationHelper;
	}

	@Override
	public boolean isValid(NodeID nodeID) {
		if(!isEntry) {
			if(presentationHelper.getFullname(nodeID).contains(excludePattern)) {
				return true;
			} else {
				return false;
			}
		} else {
			if(entryMatched) {
				return true;
			} else {
				if(presentationHelper.getFullname(nodeID).contains(excludePattern)) {
					entryMatched = true;
					return true;
				} else {
					return false;
				}				
			}
		}
	}

	@Override
	public void startIteration() {
		entryMatched = false;
	}

	@Override
	public void stopIteration() {
		// TODO Auto-generated method stub
		
	}

}
