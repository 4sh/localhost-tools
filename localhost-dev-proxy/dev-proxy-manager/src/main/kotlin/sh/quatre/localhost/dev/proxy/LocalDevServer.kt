package sh.quatre.localhost.dev.proxy

import kotlinx.datetime.Instant

data class LocalDevServer(val name: String)

data class RunningDevServer(val server: LocalDevServer, val port: Int, val startedAt: Instant)
