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

package io.djigger.ui.threadselection;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;



public class ThreadSelectionPanePopupMenu extends JPopupMenu implements ActionListener {

	private static final long serialVersionUID = 1103264298324210815L;

	JMenuItem anItem;

	private ThreadSelectionPane pane;

	public ThreadSelectionPanePopupMenu(ThreadSelectionPane pane) {
		this.pane = pane;
		anItem = new JMenuItem("Select all");
		anItem.addActionListener(this);
		add(anItem);
		anItem = new JMenuItem("Unselect all");
		anItem.addActionListener(this);
		add(anItem);
		anItem = new JMenuItem("Select this only");
		anItem.addActionListener(this);
		add(anItem);
		anItem = new JMenuItem("Zoom out");
		anItem.addActionListener(this);
		add(anItem);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Select all")) {
			pane.selectAll();
		} else if(e.getActionCommand().equals("Unselect all")) {
			pane.unselectAll();
		} else if(e.getActionCommand().equals("Select this only")) {
			pane.selectThisOnly();
		} else if(e.getActionCommand().equals("Zoom out")) {
			pane.zoomOut();
		}

	}

	@Override
	public void print(Graphics g) {
		anItem.setEnabled(pane.isMouseOverBlock());
		super.print(g);
	}

}
