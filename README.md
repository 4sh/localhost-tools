# localhost-tools

This repo provides some tools to help developers work with their localhost.

The first tool is a localhost server manager: it let you register your locally running servers, and the server manager
will assign a free port to them and make them accessible by name, always on the same 9999 port.

## Usage

We provide a docker image, so you only need docker to run it, like this:
```shell
docker run -p 9990:9990 -p 9999:9999 europe-docker.pkg.dev/quatreapp/localhost-tools/localhost-server-manager:1.0.0
```
We recommend that you start it at startup.

Then, in your apps, you can register against the server with a simple HTTP call:
```
POST http://localhost:9990/local-servers/your-server-name
```
This will return the assigned port in the body, and then you can start your server on this port, and it will be 
accessible at http://your-server-name.localtest.me:9999/

If you run a java server, you can use the very lightweight `dev-proxy-lib` library to register your server 
(pure java, non dependency).

Example in kotlin with htt4k:
```kotlin
fun main() {
    val handler = { request: Request -> Response(OK).body("Hello, I'm ${request.header("host")}!") }

    val port = LocalDevServer.registerAndGetPort("my-http4k-server")
    logger.info("starting server on port $port")
    handler.asServer(Undertow(port)).start()
}
```

You can have a look at the code of this lib, it's very simple:
[source](localhost-dev-proxy/dev-proxy-lib/src/main/java/sh/quatre/localhost/dev/proxy/api/LocalDevServer.java)

## Status

This tool is still in very early stage. It has been developed over a night in the "Nuit de la R&D" event.

There is no error management, and ports assignment is very basic - so it works when it works, otherwise...

Therefore contributions are welcome!

## Development

The main logic can be found in the `localhost-dev-proxy/dev-proxy-manager` module.

It is developed in Kotlin, but the library provided is pure java, and you can easily use it in any language 
supporting to send an HTTP POST.

It relies on Envoy for the reverse proxy. Envoy is packaged in the same docker image, and managed as a simple process.