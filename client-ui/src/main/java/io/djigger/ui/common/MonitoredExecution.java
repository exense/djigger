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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoredExecution extends JDialog {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitoredExecution.class);
	
	private final JProgressBar dpb;
	
	private JLabel statusLabel;
	
	private JButton interruptButton;
	
	private long duration;
	
	private MonitoredExecution execution;
	
	private boolean isInterrupted;
	
	public MonitoredExecution(Frame owner, String title, final MonitoredExecutionRunnable runnable) {		
		this(owner, title, runnable, false);
	} 
	
	public MonitoredExecution(Frame owner, String title, final MonitoredExecutionRunnable runnable, boolean interruptable) {		
		super(owner, "Progress", true);
		this.execution = this;
		setLayout(new GridLayout());
		
		setResizable(false);
		
		JPanel panel = new JPanel(new BorderLayout(5,5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dpb = new JProgressBar();
		dpb.setIndeterminate(true);
		
		statusLabel = new JLabel(title);
		panel.add(BorderLayout.NORTH, statusLabel);
		panel.add(BorderLayout.CENTER, dpb);
		
		isInterrupted = false;
		if(interruptable) {
			interruptButton = new JButton("Interrupt");
			panel.add(BorderLayout.SOUTH, interruptButton);
			
			interruptButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					isInterrupted = true;
				}
			});
		}
		
		add(panel);
		
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(300, 75);
		setLocationRelativeTo(owner);
				
		addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent event) {
                final Thread task = new Thread(new ProgressRunnable(runnable));
                task.start();
            }
        });
		
		pack();
	} 
	
	public void setMaxValue(long value) {
		dpb.setIndeterminate(false);
		dpb.setMaximum((int) value);
	}
	
	public void setValue(long value) {
		dpb.setValue((int) value);
	}
	
	public void setText(String text) {
		statusLabel.setText(text);
	}
	
    public boolean isInterrupted() {
		return isInterrupted;
	}

	public long getDuration() {
		return duration;
	}

	public void run() {
        super.setVisible(true);
    }
	
	private class ProgressRunnable implements Runnable {
		private final MonitoredExecutionRunnable runnable;

		private ProgressRunnable(MonitoredExecutionRunnable runnable) {
			super();
			this.runnable = runnable;
		}

		@Override
		public void run() {
			final long start = System.currentTimeMillis();
			try {
				runnable.run(execution);
			} catch (Exception e) {
				logger.error("Error while running monitored execution",e);
			}finally {
				duration = (int) (start - System.currentTimeMillis());
            	if(duration<1000) {
            		try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
            	}
				SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setVisible(false);
                    }
                });
			}
		}
		
		
	}
}

