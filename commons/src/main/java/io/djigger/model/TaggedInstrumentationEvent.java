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
package io.djigger.model;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;

import java.util.Map;

public class TaggedInstrumentationEvent extends AbstractOrganizableObject {

    private Map<String, String> tags;

    private InstrumentationEvent event;

    public TaggedInstrumentationEvent() { super();}

    public TaggedInstrumentationEvent(Map<String, String> tags, InstrumentationEvent event) {
        super();
        this.tags = tags;
        this.event = event;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public InstrumentationEvent getEvent() {
        return event;
    }
}
