package sh.quatre.localhost.dev.proxy.gen

import sh.quatre.localhost.dev.proxy.RunningDevServer

class ServersIndexGenerator {
    fun generateToString(servers: List<RunningDevServer>) = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Localhost Dev Proxy Servers Listing</title>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css">
          </head>
          <body>
          <section class="section">
            <div class="container">
              <h1 class="title">
                Welcome to Localhost Dev Proxy, powered by Envoy.
              </h1>
              <nav class="panel">
                <p class="panel-heading">
                  Local Servers
                </p>
                ${servers.map { it.asLink() }.joinToString("")}
              </nav>
            </div>
          </section>
          <footer class="footer">
            <div class="content has-text-centered">
              <p>
                <strong>Dev Proxy</strong> powered by <a href="https://www.envoyproxy.io/">Envoy</a>, made with ❤ by <a href="https://github/com/xhanin">Xavier Hanin</a>. 
              </p>
            </div>
          </footer>
          </body>
        </html>
    """.trimIndent()

    fun RunningDevServer.asLink() =
        "<a class='panel-block' href='http://${server.name}.localtest.me:9999/'>${server.name}</a>"
}
