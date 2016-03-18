/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
 *******************************************************************************/

package io.djigger.ui.analyzer;

import io.djigger.ui.Session.SessionType;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;



public class TreePopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1103264298324210815L;

	@SuppressWarnings("serial")
	public TreePopupMenu(final AnalyzerPane analyzer) {
/*		anItem = new JMenuItem("Merged calls");
		anItem.addActionListener(this);
		analyzer.mergedCalls();
		add(anItem); */
		add(new JMenuItem(new AbstractAction("Set filter on this method") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.setFilterOnCurrentSelection();
			}
		}));
		add(new JMenuItem(new AbstractAction("Skip this method") {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzer.skipCurrentSelection();
			}
		}));
		if(analyzer.getMain().getSessionType()==SessionType.AGENT) {
			add(new JMenuItem(new AbstractAction("Instrument this node") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					analyzer.instrumentCurrentNode();
				}
			}));
			add(new JMenuItem(new AbstractAction("Instrument all nodes in this method") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					analyzer.instrumentCurrentMethod();
				}
			}));
		}
	}
}
