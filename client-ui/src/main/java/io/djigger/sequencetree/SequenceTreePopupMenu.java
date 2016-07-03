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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.ui.menus.InstrumentationEventMenu;
import io.djigger.ui.menus.InstrumentationEventMenu.InstrumentationEventMenuCallback;



public class SequenceTreePopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1103264298324210815L;

	
	JMenuItem drillDownItem;
	
	SequenceTreeView pane;
	
	@SuppressWarnings("serial")
	public SequenceTreePopupMenu(final SequenceTreeView pane) {
		this.pane = pane;
		
		drillDownItem = new JMenuItem(new AbstractAction("Drill-down") {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.drillDown();
			}
		});
		
		add(drillDownItem);
		
		new InstrumentationEventMenu(this, pane.session, new InstrumentationEventMenuCallback() {
			@Override
			public InstrumentationEvent getCurrentEvent() {
				SequenceTreeNode node = pane.getSelectedNode();
				return node!=null?node.getEvent():null;
			}
		});
		
		add(new JSeparator()); 
		add(new JMenuItem(new AbstractAction("Expand all") {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.expandChildrenOfCurrentSelection();
			}
		}));
		add(new JMenuItem(new AbstractAction("Expand first branch") {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.expandFirstChildrenOfCurrentSelection();
			}
		}));
		add(new JMenuItem(new AbstractAction("Collapse all") {
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.collapseChildrenOfCurrentSelection();
			}
		}));
	}

	@Override
	public void show(Component invoker, int x, int y) {
		SequenceTreeNode node = pane.getSelectedNode();
		
		if(node!=null && node.isLeaf()) {
			drillDownItem.setVisible(true);
		} else {
			drillDownItem.setVisible(false);
		}
		super.show(invoker, x, y);
	}
}
