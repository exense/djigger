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
package io.djigger.monitoring.java.sampling;


public class Sampler extends Thread {

	private volatile int interval = 1000;

	private volatile boolean run = false;
	
	private volatile boolean destroyed = false;
	
	private final Runnable runnable;

	public Sampler(Runnable runnable) {
		super();
		this.runnable = runnable;
	}

	@Override
	public void run() {
		long lastDump = System.currentTimeMillis();
		long now; 
		while(!destroyed) {
			try {
				if(run) {
					now = System.currentTimeMillis();
					Thread.sleep(Math.max(interval-(now-lastDump),0));
					//System.out.println("Sampler: sleeping "+Integer.toString((int) (interval-(now-lastDump))));
					lastDump = System.currentTimeMillis();
					
					runnable.run();
				} else {
					synchronized(this) {
						wait();
					}
				}
			} catch (Exception e) {
				//TODO logger
				e.printStackTrace();
			}
		}
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setRun(boolean run) {
		this.run = run;
		synchronized (this) {
			notify();
		}
	}
	
	public void destroy() {
		destroyed = true;
	}	
}
