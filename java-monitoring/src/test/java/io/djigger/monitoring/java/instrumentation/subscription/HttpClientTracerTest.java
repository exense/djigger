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
package io.djigger.monitoring.java.instrumentation.subscription;

import java.io.IOException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientTracerTest {

	public static void main(String[] args) throws Exception {
		HttpClientTracerTest test = new HttpClientTracerTest();
		while(true) {
			test.call();
			Thread.sleep(1000);
		}
	}

	private void call() throws Exception {
		callGet();
		callPost();
		callDelete();
	}

	private void callGet() throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpGet get = new HttpGet("http://localhost:12298");
			client.execute(get);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void callPost() throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost get = new HttpPost("http://localhost:12298");
			client.execute(get);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void callDelete() throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpDelete get = new HttpDelete("http://localhost:12298");
			client.execute(get);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
