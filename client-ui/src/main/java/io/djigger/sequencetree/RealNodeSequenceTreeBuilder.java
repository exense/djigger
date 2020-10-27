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
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealNodeSequenceTreeBuilder {

    public InstrumentationEventNode buildRealNodeTree(List<InstrumentationEvent> events) {

        Map<String, InstrumentationEventNode> nodeIndex = new HashMap<>();

        for (InstrumentationEvent event : events) {
            nodeIndex.put(event.getId(), new InstrumentationEventNode(event));
        }

        InstrumentationEventNode lastNode = null;
        for (InstrumentationEventNode node : nodeIndex.values()) {
            InstrumentationEvent event = node.getEvent();
            InstrumentationEventNode parent = nodeIndex.get(event.getParentID());
            if (parent != null && parent != node) {
                node.setParent(parent);
                parent.add(node);
                lastNode = node;
            }
        }

        InstrumentationEventNode rootNode = lastNode;
        while (rootNode.getParent() != null) {
            rootNode = rootNode.getParent();
        }

        InstrumentationEventNode root = new InstrumentationEventNode(null);
        root.add(rootNode);
        rootNode.setParent(rootNode);

        return root;
    }
}
