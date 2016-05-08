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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

public class RealNodeSequenceTreeBuilder {

	public InstrumentationEventNode buildRealNodeTree(List<InstrumentationEvent> events) {		
			
		Map<Long, InstrumentationEventNode> nodeIndex = new HashMap<>();
		
		for(InstrumentationEvent event:events) {
			nodeIndex.put(event.getLocalID(), new InstrumentationEventNode(event));
		}
		
		for(InstrumentationEventNode event:nodeIndex.values()) {
			InstrumentationEventNode parent = nodeIndex.get(event.getEvent().getLocalParentID());
			if(parent!=event) {
				parent.add(event);
			}
		}
		
		return nodeIndex.get(new Long(0));
	}

}
