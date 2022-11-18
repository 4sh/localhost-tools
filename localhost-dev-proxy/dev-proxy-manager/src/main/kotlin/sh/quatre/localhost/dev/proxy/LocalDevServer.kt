package sh.quatre.localhost.dev.proxy

import kotlinx.datetime.Instant
import sh.quatre.localhost.dev.proxy.DevServerProtocol.HTTP

data class LocalDevServer(val name: String, val protocol: DevServerProtocol = HTTP)

enum class DevServerProtocol {
    HTTP, HTTP_2
}
data class RunningDevServer(val server: LocalDevServer, val port: Int, val startedAt: Instant)
