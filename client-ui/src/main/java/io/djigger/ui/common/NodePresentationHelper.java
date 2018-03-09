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
package io.djigger.ui.common;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.sequencetree.SequenceTreeNode;
import io.djigger.ui.instrumentation.InstrumentationStatistics;
import io.djigger.ui.instrumentation.InstrumentationStatisticsCache;
import io.djigger.ui.model.AnalysisNode;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNodePath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;


public class NodePresentationHelper {

    private final InstrumentationStatisticsCache statisticsCache;

    private DecimalFormat format1 = new DecimalFormat("#");

    private DecimalFormat format2 = new DecimalFormat("#.####");

    public NodePresentationHelper(InstrumentationStatisticsCache statisticsCache) {
        super();
        this.statisticsCache = statisticsCache;
        this.format1.setRoundingMode(RoundingMode.CEILING);
        this.format2.setRoundingMode(RoundingMode.CEILING);
    }

    public String shortLabel(AnalysisNode node, AnalysisNode rootForCalculation) {
        String[] split = getFullname(node).split("\\.");
        if (split.length >= 2) {
            return split[split.length - 2] + "." + split[split.length - 1] + "() " + getPercentage(node, rootForCalculation);
        } else {
            return toString(node);
        }
    }

    public String shortLabel(RealNodePath path) {
        String[] split = path.getLastNode().toString().split("\\.");
        if (split.length >= 2) {
            return split[split.length - 2] + "." + split[split.length - 1];
        } else {
            return path.getLastNode().toString();
        }
    }

    public String longLabel(AnalysisNode node, AnalysisNode rootForCalcultation) {
        return getFullname(node) + "()  " + getPercentage(node, rootForCalcultation);
    }

    public String toString(AnalysisNode node) {
        return getFullname(node) + "()  " + getPercentage(node, node.getRoot());
    }

    public String toString(SequenceTreeNode node) {
        return node.getEvent() != null ? getFullname(node.getEvent()) + "() - " + (node.getEvent().getDuration() / 1000000) + "ms" : "";
    }

    public String getFullname(AnalysisNode node) {
        return getFullname(node.getId());
    }

    public String getFullname(NodeID nodeID) {
        if (nodeID != null) {
            return nodeID.toString();
        } else {
            return "Root";
        }
    }

    public String getFullname(InstrumentationEvent event) {
        return event != null ? event.getClassname() + "." + event.getMethodname() : "";
    }


    private String getPercentage(AnalysisNode node, AnalysisNode rootForCalculation) {
        InstrumentationStatistics statisctics = statisticsCache.getInstrumentationStatistics(node.getRealNodePath());

        AnalysisNode root = rootForCalculation;
        AnalysisNode thisNode = node;

        BigDecimal percentage;
        if (thisNode.getWeight() > 0) {
            percentage = new BigDecimal(thisNode.getWeight() / (1.0 * root.getWeight()) * 100);
        } else {
            percentage = new BigDecimal(0);
        }
        percentage = percentage.setScale(0, RoundingMode.HALF_EVEN);

        StringBuilder builder = new StringBuilder();
        builder.append(percentage).append("% [").append(thisNode.getWeight()).append("] ");

        if (statisctics != null) {
            builder.append("  { " + statisctics.getRealCount() + " - " + format1.format(statisctics.getAverageResponseTime()) + "ms}");
        }

        return builder.toString();
    }

    public boolean hasInstrumentationStatistics(AnalysisNode node) {
        return statisticsCache.getInstrumentationStatistics(node.getRealNodePath()) != null;
    }
}
