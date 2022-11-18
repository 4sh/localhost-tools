package sh.quatre.localhost.dev.proxy.server

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

class LocalDevProxyHttpServer {
    fun start(port: Int, callbacks: ServerCallbacks) {
        val app = routes(
            "local-servers/{name}" bind Method.POST to {
                it.path("name")?.let(callbacks::onServerStarting)?.let { port -> Response(OK).body(port.toString()) }
                    ?: Response(NOT_FOUND)
            },
            "local-servers/{name}" bind Method.DELETE to {
                it.path("name")?.let(callbacks::onServerStopped)?.let { Response(OK).body("gone") }
                    ?: Response(NOT_FOUND)
            }
        )
        app.asServer(Undertow(port)).start()
    }
}

interface ServerCallbacks {
    fun onServerStarting(name: String): Int
    fun onServerStopped(name: String)
}