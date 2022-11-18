package sh.quatre.localhost.dev.proxy.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
