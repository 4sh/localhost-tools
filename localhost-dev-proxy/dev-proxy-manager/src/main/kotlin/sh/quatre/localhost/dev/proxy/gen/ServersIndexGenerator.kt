package sh.quatre.localhost.dev.proxy.gen

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Undertow
import org.http4k.server.asServer
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

class ServersIndexGenerator {
    fun generateToString(servers: List<RunningDevServer>) = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Localhost Dev Server Manager</title>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
          </head>
          <body>
          <section class="section">
            <div class="container">
              <h1 class="title">
                Welcome to Localhost Dev Server Manager, powered by Envoy.
              </h1>
              <nav class="panel is-primary">
                <p class="panel-heading">
                  Local Servers
                </p>
                <div class="panel-block">
                <table class="table is-fullwidth">
                <thead>
                <tr><th>server</th><th>port</th><th>started at</th></tr>
                </thead>
                <tbody>
                ${servers.map { it.asRow() }.joinToString("")}
                </tbody>
                </table>
                </div>
              </nav>
            </div>
          </section>
          <footer class="footer">
            <div class="content has-text-centered">
              <p>
                <strong>Dev Proxy</strong> powered by <a href="https://www.envoyproxy.io/">Envoy</a>, made with ❤ by <a href="https://github.com/4sh">4SH</a>. 
              </p>
            </div>
          </footer>
          </body>
        </html>
    """.trimIndent()

    fun RunningDevServer.asRow() =
        """<tr>
            |<td><a href='http://${server.name}.localtest.me:9999/'>${server.name}</a></td>
            |<td>$port</td><td>${startedAt.formatAsTime()}</td></tr>
        """.trimMargin()

    fun Instant.formatAsTime() = DateTimeFormatter.ofPattern("HH:mm:ss")
        .format(LocalDateTime.ofInstant(toJavaInstant(), ZoneId.systemDefault()))
}

fun main() {
    val index = ServersIndexGenerator().generateToString(
        listOf(
            RunningDevServer(LocalDevServer("my-server"), 10000, Clock.System.now() - 5.minutes),
            RunningDevServer(LocalDevServer("other-server"), 10001, Clock.System.now() - 15.minutes)
        )
    )
    val handler = { request: Request ->
        Response(Status.OK)
            .header("Content-Type", "text/html; charset=utf-8")
            .body(index)
    }

    val port = 8000
    handler.asServer(Undertow(port)).start()
}
