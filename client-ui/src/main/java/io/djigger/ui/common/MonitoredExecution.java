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
	
	public void setIndeterminate() {
		dpb.setIndeterminate(true);
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

