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
}
