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
