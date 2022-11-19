package sh.quatre.localhost.dev.proxy.example

import org.apache.logging.log4j.LogManager
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.Undertow
import org.http4k.server.asServer
import sh.quatre.localhost.dev.proxy.api.LocalDevServer

private val logger = LogManager.getLogger("example")

fun main() {
    val handler = { request: Request -> Response(OK).body("Hello, I'm ${request.header("host")}!") }

    // get port from PORT environment variable if defined (for production), or get it from local dev server manager
    val port = System.getenv("PORT")?.toIntOrNull() ?: LocalDevServer.registerAndGetPort("my-http4k-server")
    logger.info("starting server on port $port")
    handler.asServer(Undertow(port)).start()
}
