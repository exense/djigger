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


/*******************************************************************************
 * BasicRunningJVM : simple runnable program used to mock a target JVM and test collector features (among others) 
/*******************************************************************************/

package io.djigger.collector.test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class BasicJMXJVM {

	public static void main(String ... args){

		System.out.println("Starting basic jvm.");

		// Programmatically open JMX channel
		BasicJMXJVM brjvm_1 = new BasicJMXJVM();
		
		JMXConnectorServer srv = null;
		try {
			srv = brjvm_1.createJmxConnectorServer(9878);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		long runFor = 60000L; // Run per default for 1 minute
		long sleepFor = 1000L; // Check if time limit reached every second

		// 50%
		brjvm_1.iSleep50pcOfTheTime(runFor, sleepFor);
		
		// 2x 25%
		brjvm_1.iSleep25pcOfTheTime(runFor, sleepFor);
		brjvm_1.andMeTwoiSleep25pcOfTheTimeToo(runFor, sleepFor);
		
		System.out.println("Basic jvm run complete.");
		
		try {
			srv.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JMXConnectorServer  createJmxConnectorServer(int portNr) throws IOException {
		LocateRegistry.createRegistry(portNr);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:"+portNr+"/jmxrmi");
		JMXConnectorServer svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		svr.start();
		
		return svr;
	}
	
	private void iSleep(long howLong, long checkInterval){

		long begin = System.currentTimeMillis();

		while(System.currentTimeMillis() - begin < howLong)
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void iSleep50pcOfTheTime(long howLong, long checkInterval){
		iSleep(howLong/2, checkInterval);
	}
	
	private void iSleep25pcOfTheTime(long howLong, long checkInterval){
		iSleep(howLong/4, checkInterval);
	}
	
	private void andMeTwoiSleep25pcOfTheTimeToo(long howLong, long checkInterval){
		iSleep(howLong/4, checkInterval);
	}
	
}
