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
