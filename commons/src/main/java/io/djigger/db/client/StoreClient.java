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
package io.djigger.db.client;

import io.djigger.collector.accessors.stackref.ThreadInfoAccessorImpl;

public class StoreClient {

	ThreadInfoAccessorImpl threadInfoAccessor;
	
	public void connect(String host) throws Exception {
		threadInfoAccessor = new ThreadInfoAccessorImpl();
		// TODO Change!
		threadInfoAccessor.start(host, "djigger");
	}

	public ThreadInfoAccessorImpl getThreadInfoAccessor() {
		return threadInfoAccessor;
	}
}
