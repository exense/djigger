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
package io.djigger.collector.accessors;

import io.djigger.collector.accessors.ThreadInfoAccessor;
import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;
import io.djigger.monitoring.java.model.ThreadInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.bson.Document;

public class ThreadInfoAccessorTest {
	

	public void test() throws Exception {
		
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
		
		
		Date from = f.parse("20140902 09:00:00");
		Date to = f.parse("20140902 09:00:10");

		
		System.out.println("Mongo");
		for(int i=0;i<10;i++) {		
			ThreadInfoAccessorImpl b = new ThreadInfoAccessorImpl();
			b.start("c600883", "djigger_test");
			query(b, from, to);
		}
		

	}
	
	private void query(ThreadInfoAccessor a, Date from, Date to) throws Exception {		
		long t1 = System.currentTimeMillis();
		Iterator<ThreadInfo> it = a.query(new Document("env","S01"), from, to).iterator();
		int c = 0;
		while(it.hasNext()) {
			it.next();
			c++;
		}
		System.out.println(c + "," + (System.currentTimeMillis()-t1));
	}
}
