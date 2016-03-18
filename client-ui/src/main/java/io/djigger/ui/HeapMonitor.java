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
package io.djigger.ui;

import java.lang.ref.SoftReference;

public class HeapMonitor {

	public final MainFrame main;
	
	private static SoftReference<Object> sofRef;
	
	public HeapMonitor(MainFrame main) {
		this.main = main;
		sofRef = new SoftReference<Object>(new OutOfMemoryAlert());		
	}

	private class OutOfMemoryAlert {
		
		@Override
		protected void finalize() throws Throwable {
			try {
				System.out.println("Reaching out of memory!");
				main.preventOutOfMemory();				
			} catch (Throwable t) {
				throw t;
			} finally {
				System.out.println("Calling finalize of Super Class");
				super.finalize();
			}
		}
	}
}
