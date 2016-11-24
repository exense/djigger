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
package io.djigger.ui.menus;

import java.awt.event.ActionEvent;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ui.Session;
import io.djigger.ui.instrumentation.InstrumentationEventDetailsPane;
import io.djigger.ui.menus.TransactionMenu.TransactionMenuCallback;

@SuppressWarnings("serial")
public class InstrumentationEventMenu {

	InstrumentationEventMenuCallback callback;
	
	Session session;
	
	final JComponent target;
	
	public InstrumentationEventMenu(final JComponent target, final Session session, final InstrumentationEventMenuCallback callback) {
		super();
		this.callback = callback;
		this.session = session;
		this.target = target;
		populate();
	}
	
	public interface InstrumentationEventMenuCallback {
		
		public InstrumentationEvent getCurrentEvent();
	}

	private void populate() {
		target.add(new JMenuItem(new AbstractAction("Event details") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final InstrumentationEvent event = callback.getCurrentEvent();
				new InstrumentationEventDetailsPane(session.getMain(), event);		
			}
		}));
		
		
		JMenu transactionMenu = new JMenu("Analyze Transaction in");
		new TransactionMenu(transactionMenu, session, new TransactionMenuCallback() {			
			@Override
			public InstrumentationEvent getCurrentEvent() {
				final InstrumentationEvent event = callback.getCurrentEvent();
				return event;
			}
		});
		target.add(transactionMenu);
	}

}
