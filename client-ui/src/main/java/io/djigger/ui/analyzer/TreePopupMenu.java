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
package io.djigger.ui.analyzer;

import io.djigger.ui.Session.SessionType;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;



public class TreePopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1103264298324210815L;

	@SuppressWarnings("serial")
	public TreePopupMenu(final AnalyzerPane analyzer) {
		if(analyzer instanceof TreeView) {
			final TreeView tree = (TreeView)analyzer;
			add(new JMenuItem(new AbstractAction("Expand all") {
				@Override
				public void actionPerformed(ActionEvent e) {
					tree.expandChildrenOfCurrentSelection();
				}
			}));
			add(new JMenuItem(new AbstractAction("Expand first branch") {
				@Override
				public void actionPerformed(ActionEvent e) {
					tree.expandFirstChildrenOfCurrentSelection();
				}
			}));
			add(new JMenuItem(new AbstractAction("Collapse all") {
				@Override
				public void actionPerformed(ActionEvent e) {
					tree.collapseChildrenOfCurrentSelection();
				}
			}));
			add(new JSeparator());
		}
		
		add(new JMenuItem(new AbstractAction("Filter branches in this node") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.appendCurrentSelectionToBranchFilter(false);
			}
		}));
		add(new JMenuItem(new AbstractAction("Exclude branches in this node") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.appendCurrentSelectionToBranchFilter(true);
			}
		}));
		add(new JSeparator());
		add(new JMenuItem(new AbstractAction("Filter this node") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.appendCurrentSelectionToNodeFilter(false);
			}
		}));
		add(new JMenuItem(new AbstractAction("Skip this node") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.appendCurrentSelectionToNodeFilter(true);
			}
		}));
		if(analyzer.getMain().getSessionType()==SessionType.AGENT) {
			add(new JSeparator()); 
			add(new JMenuItem(new AbstractAction("Instrument this node (only this path)") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					analyzer.instrumentCurrentNode();
				}
			}));
			add(new JMenuItem(new AbstractAction("Instrument this node (all paths)") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					analyzer.instrumentCurrentMethod();
				}
			}));
		}
	}
}
