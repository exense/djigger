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
package io.djigger.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.djigger.monitoring.java.model.StackTraceElement;
import io.djigger.ui.model.NodeID;
import io.djigger.ui.model.RealNode;
import io.djigger.ui.model.RealNodePath;

public class RealNodeBuilderTest {

	@Test
	public void test() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m3"},{"m1","m2"},{"m1","m3"}});
				
		RealNodeBuilder a = new RealNodeBuilder(); 	
		RealNode result = a.buildRealNodeTree(threads);
		
		assertMinCount(result, new String[]{"m1"}, 1);
		assertMinCount(result, new String[]{"m1","m2"}, 2);
		assertMinCount(result, new String[]{"m1","m3"}, 2);

	}
	
	@Test
	public void test2() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m3","m4"},{"m1","m2"},{"m1","m3","m4"}});
				
		RealNodeBuilder a = new RealNodeBuilder(); 	
		RealNode result = a.buildRealNodeTree(threads);
		
		assertMinCount(result, new String[]{"m1"}, 1);
		assertMinCount(result, new String[]{"m1","m2"}, 2);
		assertMinCount(result, new String[]{"m1","m3"}, 2);
		assertMinCount(result, new String[]{"m1","m3","m4"}, 2);
	}
	
	@Test
	public void test3() {	
		List<Thread> threads = threads(new Object[][]{{"m1","m2"},{"m1","m2"},{"m1","m2"}});
				
		RealNodeBuilder a = new RealNodeBuilder(); 	
		RealNode result = a.buildRealNodeTree(threads);
		
		assertMinCount(result, new String[]{"m1"}, 1);
		assertMinCount(result, new String[]{"m1","m2"}, 1);
	}
	
	@Test
	public void test4() {	
		List<Thread> threads = threads(thread(new Object[][]{{"m1","m2"}},1),thread(new Object[][]{{"m1","m2"}},2));
				
		RealNodeBuilder a = new RealNodeBuilder(); 	
		RealNode result = a.buildRealNodeTree(threads);
		
		assertMinCount(result, new String[]{"m1"}, 2);
		assertMinCount(result, new String[]{"m1","m2"}, 2);
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
		List<RealNodePath> paths = new ArrayList<>();
		for (Object[] objects : o) {
			StackTraceElement[] s = new StackTraceElement[objects.length];
			for (int i=0;i<objects.length;i++) {
				s[objects.length-i-1]=new StackTraceElement("c1", (String) objects[i],null, 0);
			}
			paths.add(RealNodePath.fromStackTrace(s,false));
		}
		return new Thread(id, paths);
	}
	
	private void assertMinCount(RealNode root, String[] path,int expectedMinCount) {
		RealNode currentNode = root;
		for (String string : path) {
			currentNode = currentNode.getChild(NodeID.getInstance("c1", string));
		}
		org.junit.Assert.assertSame(expectedMinCount, currentNode.getMinCallCount());
	}
}
