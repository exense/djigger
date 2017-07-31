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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import io.djigger.ui.analyzer.Dashlet;

@SuppressWarnings("serial")
public class EnhancedTabbedPane extends JTabbedPane {

	private Runnable addTabAction;
		
    public EnhancedTabbedPane(boolean backgroundWithLogo) {
		super();

		createAddTab(backgroundWithLogo);
	}

	interface TabClickListener {
    	
    	public void tabClicked();
    }
    
	class AddTab extends JPanel implements TabClickListener {

    	public AddTab(boolean backgroundWithLogo) {
			super();

			if(backgroundWithLogo) {
				setLayout(new GridBagLayout());
				
				java.net.URL imgURL = CommandButton.class.getResource("logo.png");
				ImageIcon icon = new ImageIcon(imgURL);
				JLabel background = new JLabel(icon);
				add(background);
				setBackground(Color.WHITE);
			}
		}
		
		@Override
		public void tabClicked() {
			addTabAction.run();
		}
    }
    
    public Runnable getAddTabAction() {
		return addTabAction;
	}

	public void setAddTabAction(Runnable addTabAction) {
		this.addTabAction = addTabAction;
	}

	private void createAddTab(boolean backgroundWithLogo) {
		addTab(new AddTab(backgroundWithLogo), "+", false);
	}
	
    public void addTab(Component analyzer, String name, boolean closeButton) {
    	if(analyzer instanceof Dashlet) {
    		((Dashlet)analyzer).setTitle(name);
    	}
    	insertTab(name, null, analyzer, null, Math.max(0, getTabCount()-1));
    	setTabComponentAt(indexOfComponent(analyzer), getTitlePanelWithCloseButton(analyzer, name, closeButton));
    	setSelectedComponent(analyzer);
    }
    
	private JPanel getTitlePanelWithCloseButton(final Component component, String title, boolean addCloseButton) {
		JPanel panelanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelanel.setOpaque(false);
		JLabel titleLbl = new JLabel(title);
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		panelanel.add(titleLbl);
		if(addCloseButton) {
			CommandButton closeButton = new CommandButton("close.png","Close session", new Runnable() {
				@Override
				public void run() {
					int i = indexOfComponent(component);
					remove(component);
					if(component instanceof Closeable) {
						((Closeable)component).close();
					}
					setSelectedIndex(Math.max(0, i-1));
				}
			}, 10);
			panelanel.add(closeButton);
		}
		
		
		if(component instanceof TabClickListener) {
			panelanel.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent arg0) {}
				
				@Override
				public void mousePressed(MouseEvent arg0) {}
				
				@Override
				public void mouseExited(MouseEvent arg0) {}
				
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
						((TabClickListener)component).tabClicked();
				}
			});
		}
		return panelanel;
	}

    public Component getCurrentTab() {
        Component _component = getSelectedComponent();
        return _component;
    }
    
	public void selectTabByName(String name) {
		setSelectedComponent(getTabByName(name));
	}
	
	public Component getTabByName(String name) {
		for (Component component : getComponents()) {
			if(component instanceof Dashlet && name.equals(((Dashlet)component).getTitle())) {
				return component;
			}
		}
		throw new RuntimeException("Tab with name "+name+" not found");
	}
}
