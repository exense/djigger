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
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class EnhancedTabbedPane extends JTabbedPane {

	private Runnable addTabAction;
	
	private boolean insertingTab = false;
	
    public EnhancedTabbedPane() {
		super();
		
		addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!insertingTab) {
					Component p = (Component) e.getSource();
	
					Component selection = getCurrentTab();
					if(p.isShowing()) {
						if(selection instanceof TabSelectionListener) {
							((TabSelectionListener)selection).tabSelected(selection, p);
						}
					}
				}
			}
		});

		createAddTab();
	}

	interface TabSelectionListener {
    	
    	public void tabSelected(Component c, Component p);
    }
    
	class AddTab extends JPanel implements TabSelectionListener {

    	@Override
    	public void tabSelected(Component c, Component p) {
    		addTabAction.run();
    	}
    }
    
    public Runnable getAddTabAction() {
		return addTabAction;
	}

	public void setAddTabAction(Runnable addTabAction) {
		this.addTabAction = addTabAction;
	}

	private void createAddTab() {
		addTab(new AddTab(), "+", false);
	}
	
    public void addTab(Component analyzer, String name, boolean closeButton) {
//    	if(getTabCount()>0)
//    		setSelectedIndex(0);
    	insertingTab = true;
    	
    	try {
	    	insertTab(name, null, analyzer, null, Math.max(0, getTabCount()-1));
	    	if(closeButton) {
	    		setTabComponentAt(indexOfComponent(analyzer), getTitlePanelWithCloseButton(analyzer, name));
	    	}
	    	setSelectedComponent(analyzer);
    	} finally {
    		insertingTab = false;
    	}
    }
    
	private JPanel getTitlePanelWithCloseButton(final Component component, String title) {
		JPanel panelanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelanel.setOpaque(false);
		JLabel titleLbl = new JLabel(title);
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		panelanel.add(titleLbl);
		CommandButton closeButton = new CommandButton("close.png","Close session", new Runnable() {
			@Override
			public void run() {
				insertingTab = true;
				try {
					int i = indexOfComponent(component);
					remove(component);
					if(component instanceof Closeable) {
						((Closeable)component).close();
					}
					setSelectedIndex(Math.max(0, i-1));
				} finally {
					insertingTab = false;
				}
			}
		}, 10);
		panelanel.add(closeButton);
		return panelanel;
	}

    public Component getCurrentTab() {
        Component _component = getSelectedComponent();
        return _component;
    }
}
