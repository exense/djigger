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
package io.djigger.ui.common;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class CommandButton extends JToggleButton {

	public CommandButton(Action action) {
		super();
		init(null, action);
	}
	
	public CommandButton(String resourceName, String toolTip, final Runnable cmd, int size) {
		super();
		
		java.net.URL imgURL = getClass().getResource(resourceName);
		ImageIcon icon = new ImageIcon(imgURL, toolTip);
		icon.setImage(icon.getImage().getScaledInstance(size, size, Image.SCALE_FAST));
		AbstractAction action = new AbstractAction("", icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmd.run();
			}
		};
		
        init(toolTip, action);
	}
	
	public CommandButton(String resourceName, String toolTip, final Runnable cmd) {
		this(resourceName, toolTip, cmd, 20);
	}

	private void init(String toolTip, Action action) {
		//Make the button looks the same for all Laf's
        setUI(new BasicButtonUI());
        //Make it transparent
        setContentAreaFilled(false);
        //No need to be focusable
        setFocusable(false);
        setBorder(BorderFactory.createEtchedBorder());
        setBorderPainted(false);
        //Making nice rollover effect
        //we use the same listener for all buttons
        addMouseListener(buttonMouseListener);
        setRolloverEnabled(true);
		
		setMargin(new Insets(0,0,0,0));
		setAction(action);
		if(toolTip!=null) {
			setToolTipText(toolTip);
		}
	}
	
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
	

	
}
