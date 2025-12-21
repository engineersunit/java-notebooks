import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * JEP 517 demo: Opt in to HTTP/3 with the standard HttpClient.
 * Requires JDK 26+ (HttpClient.Version.HTTP_3).
 *
 * Usage:
 *   javac Http3Demo.java
 *   java Http3Demo https://example.com/
 * If the server supports HTTP/3 and the network allows QUIC/UDP, the request
 * will use HTTP/3; otherwise it will transparently downgrade to HTTP/2/1.1.
 */
public class Http3Demo {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "https://www.cloudflare.com/";

        HttpClient client = HttpClient.newBuilder()
                .version(tryHttp3Version()) // prefer HTTP/3 if supported; fallback otherwise
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .version(tryHttp3Version()) // prefer HTTP/3 for this request if supported
                .GET()
                .build();

        long startNanos = System.nanoTime();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        System.out.println("URL:      " + url);
        System.out.println("Status:   " + response.statusCode());
        System.out.println("Protocol: " + response.version()); // HTTP_3, HTTP_2, or HTTP_1_1
        System.out.println("Time:     " + elapsedMs + " ms");

        // Helpful header to observe discovery/downgrade behavior
        response.headers().firstValue("Alt-Svc").ifPresent(h -> System.out.println("Alt-Svc:  " + h));

        System.out.println("Body length: " + response.body().length());
    }

    private static HttpClient.Version tryHttp3Version() {
        try {
            return HttpClient.Version.valueOf("HTTP_3");
        } catch (IllegalArgumentException e) {
            try {
                return HttpClient.Version.valueOf("HTTP_2");
            } catch (IllegalArgumentException e2) {
                return HttpClient.Version.HTTP_1_1;
            }
        }
    }
}
