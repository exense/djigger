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

import java.util.LinkedList;
import java.util.List;

public class InstrumentationEventNode {

    private InstrumentationEvent event;

    private InstrumentationEventNode parent;

    private LinkedList<InstrumentationEventNode> children = new LinkedList<>();

    public InstrumentationEventNode(InstrumentationEvent event) {
        super();
        this.event = event;
    }

    public boolean add(InstrumentationEventNode e) {
        return children.add(e);
    }

    public List<InstrumentationEventNode> getChildren() {
        return children;
    }

    public InstrumentationEventNode getParent() {
        return parent;
    }

    public void setParent(InstrumentationEventNode parent) {
        this.parent = parent;
    }

    public InstrumentationEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "InstrumentationEventNode [event=" + event + "]";
    }


}
