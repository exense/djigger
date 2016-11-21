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
package io.djigger.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.djigger.aggregation.Thread;
import io.djigger.aggregation.Thread.RealNodePathWrapper;
import io.djigger.monitoring.java.instrumentation.InstrumentationEvent;
import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.monitoring.java.model.ThreadInfo;
import io.djigger.samplig.PseudoInstrumentationEventsGenerator;
import io.djigger.samplig.PseudoInstrumentationEventsGenerator.Listener;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.PseudoInstrumentationEvent;
import io.djigger.ui.model.RealNodePath;

public class InstrumentationEventsApproximatorTest {

	@Test
	public void test() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m2"},{"m1","m3","m4"},{"m1","m3","m4"},{"m1","m2"},{"m1","m3"}});
				
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(6, events.size());
	}
	
	@Test
	public void test1() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m3"},{"m1","m2"},{"m1","m3"}});
		
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(5, events.size());
	}
	
	@Test
	public void test2() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m2"},{"m1","m2"}});
		
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(2, events.size());
	}
	
	@Test
	public void test4() {	
		List<Thread> threads = threads(thread(new Object[][]{{"m1","m2"}},1),thread(new Object[][]{{"m1","m2"}},2));
				
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(4, events.size());
	}
	
	@Test
	public void test5() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m2","m3","m4"},{"m1","m2"},{"m1","m2","m3"}});
				
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(5, events.size());
	}
	
	@Test
	public void test6() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2","m3"},{"m1","m4","m5"},{"m1","m2","m3"}});
				
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
				System.out.println(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(7, events.size());
	}
	
	@Test
	public void test7() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2","m3","m4"},{"m1","m2"},{"m1","m4","m3"}});
				
		final List<InstrumentationEvent> events = new ArrayList<>();
		PseudoInstrumentationEventsGenerator a = new PseudoInstrumentationEventsGenerator(new Listener() {
			
			@Override
			public void onPseudoInstrumentationEvent(PseudoInstrumentationEvent event) {
				events.add(event);
			}
		});
		a.generateApproximatedEvents(threads);
		Assert.assertEquals(6, events.size());
	}
	
	
	private List<Thread> threads(Object[][] o) {
		List<Thread> threads = new ArrayList<>();
		threads.add(thread(o, 0));
		return threads;
	}
	
	private List<Thread> threads(Thread... o) {
		List<Thread> threads = new ArrayList<>();
		threads.addAll(Arrays.asList(o));
		return threads;
	}

	private Thread thread(Object[][] o, long id) {
		List<RealNodePathWrapper> paths = new ArrayList<>();
		long t = 0;
		for (Object[] objects : o) {
			ArrayList<NodeID> path = new ArrayList<>();
			for (int i=0;i<objects.length;i++) {
				
				NodeID nodeID = NodeID.getInstance(objects[i].toString());
				nodeID.setAttachment(new StackTraceElement("Class1", objects[i].toString(), "", 0));
				
				path.add(nodeID);
			}
			paths.add(new RealNodePathWrapper(RealNodePath.getInstance(path), new ThreadInfo(new StackTraceElement[objects.length], id, t++)));
		}
		return new Thread(id, paths);
	}
}
