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
import javax.swing.JMenuItem;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ui.Session;
import io.djigger.ui.analyzer.TransactionAnalyzerFrame;
import io.djigger.ui.model.PseudoInstrumentationEvent;

@SuppressWarnings("serial")
public class TransactionMenu {

	TransactionMenuCallback callback;
	
	Session session;
	
	final JComponent target;
	
	public TransactionMenu(final JComponent target, final Session session, final TransactionMenuCallback callback) {
		this.callback = callback;
		this.session = session;
		this.target = target;
		populate();
	}
	
	public interface TransactionMenuCallback {
		
		public InstrumentationEvent getCurrentEvent();
	}

	private void populate() {
		
		target.add(new JMenuItem(new AbstractAction("Sequence tree") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationEvent event = callback.getCurrentEvent();
				if(event instanceof PseudoInstrumentationEvent) {
					session.getAnalyzerGroupPane().addSequenceTreePane((PseudoInstrumentationEvent)event);	
				} else {
					UUID transactionID = callback.getCurrentEvent().getTransactionID();
					session.getAnalyzerGroupPane().addSequenceTreePane(transactionID);					
				}
			}
		}));
		target.add(new JMenuItem(new AbstractAction("Sampling tree") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				InstrumentationEvent event = callback.getCurrentEvent();
				new TransactionAnalyzerFrame(session, event);
			}
		}));
		target.add(new JMenuItem(new AbstractAction("Event list") {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final UUID transactionID = callback.getCurrentEvent().getTransactionID();
				session.getAnalyzerGroupPane().addInstrumentationEventPaneForTransaction(transactionID);
			}
		}));
	}

}
