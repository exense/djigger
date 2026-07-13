package io.djigger.it;

import io.djigger.collector.server.Server;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the collector's embedded web server serves its static UI content (the {@code /djigger}
 * web context, backed by classpath resources). This complements {@link RestServiceIT} (which covers the
 * {@code /rest} JAX-RS endpoint) and exists mainly to give the upcoming Jetty 12 migration an automated
 * oracle: static-resource serving changes in Jetty 12 ({@code Resource}/{@code ResourceFactory},
 * {@code setBaseResource}) and must keep returning the UI page afterwards.
 *
 * <p>Only the service server is started (no database), on an ephemeral port.
 */
@Tag("integration")
public class WebStaticContentIT {

    @Test
    public void serviceServerServesStaticWebContent() throws Exception {
        Server collector = new Server();
        try {
            collector.startServiceServer(0, "localhost");
            int port = collector.getServicePort();
            assertTrue(port > 0, "service server should expose its ephemeral port");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/djigger/index.html"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), () -> "unexpected status, body=" + response.body());
            assertTrue(response.body().toLowerCase().contains("<html"),
                    () -> "expected an HTML page from the static webapp but got: " + response.body());
        } finally {
            collector.stop();
        }
    }
}
