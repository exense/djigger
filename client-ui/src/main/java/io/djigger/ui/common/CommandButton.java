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

import java.awt.AlphaComposite;
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

public class CommandButton extends JToggleButton {

	public CommandButton(Action action) {
		super();
		init(null, action);
	}
	
	boolean toggle = false;
	
	public CommandButton(String resourceName, String toolTip, final Runnable cmd, int size) {
		super();
		
		ImageIcon icon = buildIcon(resourceName, toolTip, size);

		AbstractAction action = new AbstractAction("", icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmd.run();
			}
		};
		
        init(toolTip, action);
	}
	
	public CommandButton(String resourceName, String toolTip, final Command cmd, int size) {
		super();
		
		toggle = true;
		
		ImageIcon icon = buildIcon(resourceName, toolTip, size);
		
		final CommandButton me = this;
		AbstractAction action = new AbstractAction("", icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmd.execute(me.getModel().isSelected());
			}
		};
		
        init(toolTip, action);
	}

	private ImageIcon buildIcon(String resourceName, String toolTip, int size) {
		java.net.URL imgURL = getClass().getResource(resourceName);
		ImageIcon icon = new ImageIcon(imgURL, toolTip);
		icon.setImage(icon.getImage().getScaledInstance(size, size, Image.SCALE_FAST));
		return icon;
	}
	
	public CommandButton(String resourceName, String toolTip, final Runnable cmd) {
		this(resourceName, toolTip, cmd, 20);
	}

	private void init(String toolTip, Action action) {
		//Make the button looks the same for all Laf's
        //setUI(new BasicToggleButtonUI());
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
	
	float alpha = 1f;
	
	protected float getAlpha() {
		return alpha;
	}

	protected void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public void paintComponent(java.awt.Graphics g)
	  {
	    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	    if(toggle) {
			setBorderPainted(isSelected());
		}
	    super.paintComponent(g2);
	  }
	
    private final MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            setAlpha(0.7f);
            repaint();
        }

        public void mouseExited(MouseEvent e) {
        	setAlpha(1f);
            repaint();
        }

		@Override
		public void mousePressed(MouseEvent e) {
			setAlpha(0.5f);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setAlpha(0.7f);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
		}
    };
	
    public static interface Command {
    	
    	public void execute(boolean selected);
    }
	
}
