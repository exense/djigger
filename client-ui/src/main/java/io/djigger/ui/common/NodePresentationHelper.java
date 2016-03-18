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

package io.djigger.ui.common;

import io.djigger.model.NodeID;
import io.djigger.model.RealNodePath;
import io.djigger.ui.instrumentation.InstrumentationStatistics;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.Node;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class NodePresentationHelper {
	
	private final InstrumentationStatisticsCache statisticsCache;

	public NodePresentationHelper(InstrumentationStatisticsCache statisticsCache) {
		super();
		this.statisticsCache = statisticsCache;
	}

	public String shortLabel(Node node, Node rootForCalculation) {
		String[] split = getFullname(node).split("\\.");
		if(split.length>=2) {
			return split[split.length-2] + "." + split[split.length-1] + "() " + getPercentage(node, rootForCalculation) ;
		} else {
			return toString(node);
		}
	}

	public String shortLabel(RealNodePath path) {
		String[] split = path.getLastNode().getFullname().split("\\.");
		if(split.length>=2) {
			return split[split.length-2] + "." + split[split.length-1] ;
		} else {
			return path.getLastNode().getFullname();
		}
	}

	public String longLabel(Node node, Node rootForCalcultation) {
		return getFullname(node) + "()  " + getPercentage(node, rootForCalcultation) ;
	}

	public String toString(Node node) {
		return getFullname(node) + "()  " + getPercentage(node, node.getRoot()) ;
	}

	public String getFullname(Node node) {
		return getFullname(node.getId());
	}

	public String getFullname(NodeID nodeID) {
		if(nodeID!=null) {
			return nodeID.getClassName() + "." + nodeID.getMethodName();
		} else {
			return "Root";
		}
	}

	private String getPercentage(Node node, Node rootForCalculation) {
		InstrumentationStatistics statisctics = statisticsCache.getInstrumentationStatistics(node.getPath());

		Node root = rootForCalculation;
		Node thisNode = node;

		BigDecimal percentage;
		if(thisNode.getWeight()>0) {
			percentage = new BigDecimal(thisNode.getWeight()/(1.0*root.getWeight())*100);
		} else {
			percentage = new BigDecimal(0);
		}
		percentage = percentage.setScale(0,RoundingMode.HALF_EVEN);

		String info;
		if(statisctics!=null) {
			info = percentage + "% [" + thisNode.getWeight() + "]"+ "  { " + statisctics.getRealCount() + " - " + statisctics.getAverageResponseTime() + "ms}";
		} else {
			info = percentage + "% [" + thisNode.getWeight() + "]";
		}
		return info;
	}

	public boolean hasInstrumentationStatistics(Node node) {
		return statisticsCache.getInstrumentationStatistics(node.getPath())!=null;
	}
}
