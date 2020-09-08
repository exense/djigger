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
package io.djigger.ui;

import io.djigger.ui.Session.SessionType;

import java.util.HashMap;

public class SessionConfiguration {

    private final String name;

    private final SessionType type;

    private final HashMap<SessionParameter, String> parameters = new HashMap<SessionParameter, String>();

    public SessionConfiguration(String name, SessionType type) {
        super();
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SessionType getType() {
        return type;
    }

    public HashMap<SessionParameter, String> getParameters() {
        return parameters;
    }

    public enum SessionParameter {
        PROCESSID,

        HOSTNAME,

        PORT,

        USERNAME,

        PASSWORD,

        DB,

        FILE,

        QUERY,

        TIMEINTERVAL_START,

        TIMEINTERVAL_END,

        CALCULATE_PSEUDO_EVENTS,

        INITIAL_PANE_SELECTION,

        EVENT_LIST_QUERY;
    }

}
