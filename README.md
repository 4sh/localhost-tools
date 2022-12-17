# localhost-tools

This repo provides some tools to help developers work with their localhost.

The first tool is a localhost server manager: it let you register your locally running servers, and the server manager
will assign a free port to them and make them accessible by name, always on the same 9999 port.

## Usage

We provide a docker image, so you only need docker to run it, like this:
```shell
docker run -p 9990:9990 -p 9999:9999 xhanin/localhost-tools:1.0.0
```
We recommend that you start it at startup.

Then, in your apps, you can register against the server with a simple HTTP call:
```
POST http://localhost:9990/local-servers/my-server-name
```
This will return the assigned port in the body, and then you can start your server on this port, and it will be 
accessible at http://my-server-name.localtest.me:9999/

You will also be able to see it on the list of servers of the management page:
<img width="757" alt="image" src="https://user-images.githubusercontent.com/553139/208233965-1b2214de-be18-4efe-aab3-6b80a7be808b.png">


To unregister a server, use DELETE http method:
```
DELETE http://localhost:9990/local-servers/my-server-name
```

### Example usage with curl

To declare a server and obtain a free port in a shell with curl:
```
$ PORT=$(curl -s -X POST http://localhost:9990/local-servers/my-server-name)
$ echo $PORT
19081
$ ./launch-my-server.sh --port=$PORT
```
_the last line assume you have a script `./launch-my-server.sh` to launch your server, accepting `--port=<port>` to set the port on which the server listens

To unregister the server:
```
curl -s -X DELETE http://localhost:9990/local-servers/my-server-name
```

### Example usage for a java server

If you run a java server, you can use the very lightweight `dev-proxy-lib` library to register your server 
(pure java, no dependency).

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

_the library is not published on maven central yet, so currently you need to copy the source_

### Ports used

The server manager uses 2 ports :
- `9990` - used for the management server, which serves the list of running servers, and accepts http calls to register and unregister servers
- `9999` - used for the reverse proxy which routes the http traffic it receives to your server running on localhost

When you register localhost servers, the management server will search for a random free port in the range `[10000-30000]`. It will only assign the port to your server, listening on the port is up to your own server.

## Status

This tool is ready to use but still in very early stage. It has been developed over a night in the "Nuit de la R&D" event.

There is almost no error management, and limited features - so it works when it works, otherwise...

Therefore contributions are welcome! Check the issues ;)

## Development

The main logic can be found in the `localhost-dev-proxy/dev-proxy-manager` module.

It is developed in Kotlin, but the library provided is pure java, and you can easily use it in any language 
supporting to send an HTTP POST.

It relies on Envoy for the reverse proxy. Envoy is packaged in the same docker image, and managed as a simple process.
