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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class BasicJMXJVM {
	
    public static long runFor = 6000000L; // Run for X milliseconds
    public static long sleepFor = 1000L; // Check if time limit reached every X millisecond

    public static void main(String... args) {

        System.out.println("Starting basic jvm.");

        BasicJMXJVM brjvm_1 = new BasicJMXJVM();


        Runnable r1 = new Runnable() {
			
			@Override
			public void run() {
		        // 50 % - CPU
		        iWasteCPU(runFor, sleepFor);
			}
		};

		Runnable r2 = new Runnable() {
			
			@Override
			public void run() {
		        // 50 % - CPU
		        iAlsoWasteTime(runFor, sleepFor);
			}
		};
		
		Runnable r3 = new Runnable() {
			
			@Override
			public void run() {
		        // 50 % - CPU
		        iWasteTime(runFor, sleepFor);
			}
		};
		
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		

        System.out.println("Submitting the tasks for execution...");
        executorService.submit(r1);
        executorService.submit(r2);
        executorService.submit(r3);

        try {
			executorService.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        executorService.shutdown();

    }

    private JMXConnectorServer createJmxConnectorServer(int portNr) throws IOException {
        LocateRegistry.createRegistry(portNr);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + portNr + "/jmxrmi");
        JMXConnectorServer svr = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        svr.start();

        return svr;
    }


    public static void iAlsoWasteTime(long howLong, long checkInterval) {
        iSleep((long) (howLong * 0.3), checkInterval);
    }

    public static void iWasteTime(long howLong, long checkInterval) {
        iSleep((long) (howLong * 0.2), checkInterval);
    }

    public static void iWasteCPU(long howLong, long checkInterval) {

        for (int i = 0; i < 5; i++)
            iSleepUsingCPU(howLong / 20, checkInterval);

        // boiler plate for code line function check
        for (int i = 0; i < 5; i++)
            iSleepUsingCPU(howLong / 20, checkInterval);
    }

    public static void iSleepUsingCPU(long howLong, long checkInterval) {

        long begin = System.currentTimeMillis();

        Map<String, String> aCertainHashMap = new TreeMap<String, String>();
        Map<String, String> anotherHashMap = new TreeMap<String, String>();

        String result = "";

        while (System.currentTimeMillis() - begin < howLong) {
            int i = 0;

            int loopNumber = 100000; // if too high, then risk of surpassing the check interval period

            for (; i < loopNumber; i++) {
                aCertainHashMap.put("key" + i, "value4");
                if (i != 0) {
                    aCertainHashMap.remove("key" + (i - 1));
                    anotherHashMap.putAll(aCertainHashMap);
                }

                aCertainHashMap.remove("key" + i);

            }
        }
    }

    public static void iSleep(long howLong, long checkInterval) {

        long begin = System.currentTimeMillis();

        while (System.currentTimeMillis() - begin < howLong)
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

}
