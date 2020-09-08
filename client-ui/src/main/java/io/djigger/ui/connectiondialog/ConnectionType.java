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
package io.djigger.ui.connectiondialog;

import io.djigger.ui.Session.SessionType;

public enum ConnectionType {

    STORE("Collector DB", SessionType.STORE, DBConnectionParameter.class),

    JMX("JMX", SessionType.JMX, HostConnectionParameter.class),

    AGENT("Agent", SessionType.AGENT, HostConnectionParameter.class),

    ATTACH("Attach", SessionType.AGENT, AttachConnectionParameters.class),

    FILE("jstack", SessionType.FILE, FileChooserParameters.JStack.class),

    SESSION("Saved session", SessionType.AGENT_CAPTURE, FileChooserParameters.SavedSession.class);

    private String description;

    private SessionType sessionType;

    private Class<? extends ConnectionParameterFrame> parameterDialogClass;

    ConnectionType(String description, SessionType sessionType,
                           Class<? extends ConnectionParameterFrame> parameterDialogClass) {
        this.description = description;
        this.sessionType = sessionType;
        this.parameterDialogClass = parameterDialogClass;
    }

    public String getDescription() {
        return description;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public Class<? extends ConnectionParameterFrame> getParameterDialogClass() {
        return parameterDialogClass;
    }
}
