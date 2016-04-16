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
import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class BasicJMXJVM {

	public static void main(String ... args){

		System.out.println("Starting basic jvm.");

		BasicJMXJVM brjvm_1 = new BasicJMXJVM();

		// Programmatically open JMX channel
		JMXConnectorServer srv = null;
		try {
			srv = brjvm_1.createJmxConnectorServer(1098);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		long runFor = 600000L; // Run for X milliseconds
		long sleepFor = 1000L; // Check if time limit reached every X millisecond

		// 50 % - CPU
		brjvm_1.iWaste50pcOfTheTimeButIUseCPU(runFor, sleepFor);
		
		// 30 % - WAIT
		brjvm_1.iSleep30pcOfTheTime(runFor, sleepFor);
		
		// 20 % - WAIT
		brjvm_1.iSleep20pcOfTheTime(runFor, sleepFor);
		
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
	


	private void iSleep30pcOfTheTime(long howLong, long checkInterval){
		iSleep( (long) (howLong * 0.3), checkInterval);
	}
	
	private void iSleep20pcOfTheTime(long howLong, long checkInterval){
		iSleep( (long) (howLong * 0.2), checkInterval);
	}
	
	private void iWaste50pcOfTheTimeButIUseCPU(long howLong, long checkInterval){
		
		for(int i = 0; i < 5; i++)
			iSleepUsingCPU(howLong/20, checkInterval);
		
		// boiler plate for code line function check
		for(int i = 0; i < 5; i++)
			iSleepUsingCPU(howLong/20, checkInterval);
	}

	private void iSleepUsingCPU(long howLong, long checkInterval) {

		long begin = System.currentTimeMillis();

		Map<String,String> aCertainHashMap = new TreeMap<String,String>();
		Map<String,String> anotherHashMap = new TreeMap<String,String>();

		String result = "";

		while(System.currentTimeMillis() - begin < howLong){
			int i = 0;
			
			int loopNumber = 100000; // if too high, then risk of surpassing the check interval period
			
			for (; i < loopNumber; i++){
				aCertainHashMap.put("key"+i, "value4");
				if (i !=0){
					aCertainHashMap.remove("key" + (i-1));
					anotherHashMap.putAll(aCertainHashMap);
				}

				aCertainHashMap.remove("key" + i);

			}
		}
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
	
}
