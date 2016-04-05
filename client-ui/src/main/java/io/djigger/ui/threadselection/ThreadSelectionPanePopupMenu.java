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
