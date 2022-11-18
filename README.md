# localhost-tools

This repo provides some tools to help developers work with their localhost.

The first tool is a localhost server manager: it let you register your locally running servers, and the server manager
will assign a free port to them and make them accessible by name, always on the same 9999 port.

## Usage

You can download it from the releases, and then launch it to run a small server on your localhost.

In your apps, you can register against the server with a simple HTTP call:
```
POST http://localhost:9990/local-servers/your-server-name
```
This will return the assigned port in the body, and then you can start your server on this port, and it will be 
accessible at http://your-server-name.localtest.me:9999/

If you run a java server, you can use the very lightweight `dev-proxy-lib` library to register your server.

Example in kotlin with htt4k:
```kotlin
fun main() {
    val handler = { request: Request -> Response(OK).body("Hello, I'm ${request.header("host")}!") }

    val port = LocalDevServer.registerAndGetPort("my-http4k-server")
    logger.info("starting server on port $port")
    handler.asServer(Undertow(port)).start()
}
```

## Pre requisites

- docker
- a jvm

## Status

This tool is still in very early stage. It has been developed over a night in the "Nuit de la R&D" event.

The release is not packaged yet, and there is no error management - so it works when it works, otherwise...

Therefore contributions are welcome!

## Development

The main logic can be found in the `localhost-dev-proxy/dev-proxy-manager` module.

It is developed in Kotlin, and we plan to release the server as a binary with graalvm compilation (not done yet).

It relies on Envoy for the reverse proxy. Envoy is managed with docker-compose, and configured and reloaded by the server.