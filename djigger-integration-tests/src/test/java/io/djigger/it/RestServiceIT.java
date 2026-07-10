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
 * Verifies that the collector's embedded web service (Jetty 9.4 + Jersey 2.x) starts and serves REST
 * requests. This is an end-to-end check of the web stack after the Jetty/Jersey dependency upgrades.
 *
 * <p>Only the service server is started (no database), on an ephemeral port; the {@code /services/status}
 * endpoint returns the (here empty) list of collector connections as JSON.
 */
@Tag("integration")
public class RestServiceIT {

    @Test
    public void serviceServerServesStatusEndpoint() throws Exception {
        Server collector = new Server();
        try {
            collector.startServiceServer(0, "localhost");
            int port = collector.getServicePort();
            assertTrue(port > 0, "service server should expose its ephemeral port");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/rest/services/status"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), () -> "unexpected status, body=" + response.body());
            String body = response.body().trim();
            assertTrue(body.startsWith("[") && body.endsWith("]"),
                    () -> "expected a JSON array from /services/status but got: " + body);
        } finally {
            collector.stop();
        }
    }
}
