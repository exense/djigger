package io.djigger.it.support;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Target workload for {@code HttpClientTracerIT}: repeatedly issues HTTP GETs (via Apache HttpClient) to
 * the URL given as the first argument, so djigger's {@code HttpClientTracer} has outgoing requests to
 * instrument. The tracer injects a {@code "djigger"} correlation header into each request, which the
 * receiving server in the test verifies. Runs until the launching test destroys the JVM.
 *
 * <p>Adapted from the former {@code djigger-demo} {@code HttpClientTracerTest}. Needs only Apache
 * HttpClient (+ its transitive deps) on its classpath - no djigger jars (see the module README).
 */
public class HttpWorkload {

    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "http://localhost:8080/";
        System.out.println("HttpWorkload started -> " + url);
        while (!Thread.currentThread().isInterrupted()) {
            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(new HttpGet(url))) {
                EntityUtils.consume(response.getEntity());
            } catch (Exception e) {
                // the server may momentarily be unavailable - keep trying
            }
            Thread.sleep(300);
        }
    }
}
