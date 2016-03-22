/*******************************************************************************
 * (C) Copyright  2016 Dorian Cransac and others.
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
 *    - Dorian Cransac (dcransac)
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
		try {
			brjvm_1.createJmxConnectorServer(9878);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		long runFor = 60000L; // Run per default for 1 minute
		long sleepFor = 1000L; // Check if time limit reached every second
		long begin = System.currentTimeMillis();

		while(System.currentTimeMillis() - begin < runFor)
			try {
				Thread.sleep(sleepFor);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		System.out.println("Basic jvm run complete.");
	}

	private void createJmxConnectorServer(int portNr) throws IOException {
		LocateRegistry.createRegistry(portNr);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:"+portNr+"/jmxrmi");
		JMXConnectorServer svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
		svr.start();
	}

}
