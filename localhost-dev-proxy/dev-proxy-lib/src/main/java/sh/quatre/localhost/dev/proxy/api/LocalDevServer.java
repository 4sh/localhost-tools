package sh.quatre.localhost.dev.proxy.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * API of LocalDevServer localhost tool.
 * <p>
 * Use it in your app server to register your server against the localhost server manager, which will assign a free port
 * to your app server.
 * <p>
 * It will also automatically unregister it on jvm close (or you can call `serverStopped` manually).
 * <p>
 * The server will then be able to be reached at http://[your-server-name].localtest.me:9999/
 * <p>
 * And it will show up in the list of servers at <a href="http://localhost:9999/">http://localhost:9999/</a>
 * <p>
 * Make sure a localhost server manager is running before launching your server.
 * <p>
 * Example of use with http4k:
 * <code>
 * fun main() {
 * val handler = { request: Request -> Response(OK).body("Hello, I'm ${request.header("host")}!") }
 * <p>
 * val port = LocalDevServer.registerAndGetPort("my-http4k-server")
 * logger.info("starting server on port $port")
 * handler.asServer(Undertow(port)).start()
 * }
 * </code>
 */
public class LocalDevServer {
    private static final int localDevProxyControllerPort = 9990;

    public static int registerAndGetPort(String name) {
        Runtime.getRuntime().addShutdownHook(new ServerStop(name));
        return Integer.parseInt(httpForServer("POST", name));
    }

    public static void serverStopped(String name) {
        httpForServer("DELETE", name);
    }

    private static URI localDevProxyControllerUriFor(String name) {
        try {
            return new URI("http://127.0.0.1:" + localDevProxyControllerPort + "/local-servers/" + name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String httpForServer(String method, String name) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(localDevProxyControllerUriFor(name))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ServerStop extends Thread {
        private final String name;

        ServerStop(String name) {
            this.name = name;
        }

        public void run() {
            serverStopped(name);
        }
    }
}
