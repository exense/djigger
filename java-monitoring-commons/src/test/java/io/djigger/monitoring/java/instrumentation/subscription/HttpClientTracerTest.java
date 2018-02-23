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

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientTracerTest {

    public static void main(String[] args) throws Exception {
        HttpClientTracerTest test = new HttpClientTracerTest();
        while (true) {
            test.call();
            Thread.sleep(3000);
        }
    }

    private void call() throws Exception {

        System.out.println("Http Test :");
        System.out.println("callGet:" + parseResponse(callGet()));
        System.out.println("callPost:" + parseResponse(callPost()));
        System.out.println("callDelete:" + parseResponse(callDelete()));
        System.out.println("callPut:" + parseResponse(callPut()));
    }

    private String parseResponse(CloseableHttpResponse response) throws Exception {

        String responseString = EntityUtils.toString(response.getEntity());
        return responseString;

    }


    private CloseableHttpResponse callGet() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpGet get = new HttpGet("http://localhost:12298");
            response = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private CloseableHttpResponse callPost() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost post = new HttpPost("http://localhost:12298");
            response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private CloseableHttpResponse callDelete() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpDelete del = new HttpDelete("http://localhost:12298");
            response = client.execute(del);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private CloseableHttpResponse callPut() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPut put = new HttpPut("http://localhost:12298");
            response = client.execute(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
