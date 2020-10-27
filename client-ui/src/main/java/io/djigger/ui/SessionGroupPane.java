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

import io.djigger.ui.common.EnhancedTabbedPane;
import io.djigger.ui.storebrowser.StoreBrowserPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

@SuppressWarnings("serial")
public class SessionGroupPane extends EnhancedTabbedPane implements ChangeListener {

    private final MainFrame main;

    public SessionGroupPane(final MainFrame main) {
        super(true);
        this.main = main;
        addChangeListener(this);

        setAddTabAction(new Runnable() {

            @Override
            public void run() {
                main.getMainToolbar().addSession();
            }
        });
    }

    public void addSession(Session session) {
        addTab(session, session.getSessionName(), true);
    }

    public void duplicateSession(Session session) {
        SessionConfiguration sessionConfiguration = session.cloneConfiguration();
        Session duplicatedSession = new Session(sessionConfiguration, session.getMain());
        main.addSession(duplicatedSession);
        //trigger the search directly for store session
        if (session.getSessionType() == Session.SessionType.STORE) {
            duplicatedSession.getStoreBrowserPane().search();
        }
    }


    public void selectSession(Session session) {
        if (getCurrentTab() != session) {
            setSelectedComponent(session);
        }
    }

    public void removeSession(Session session) {
        session.close();
        remove(session);
    }

    public Session getCurrentSession() {
        Component _component = getSelectedComponent();
        if (_component instanceof Session) {
            return (Session) _component;
        }
        return null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        main.selectSession(getCurrentSession());
    }

}
