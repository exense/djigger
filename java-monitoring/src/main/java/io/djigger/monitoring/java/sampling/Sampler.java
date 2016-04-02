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
