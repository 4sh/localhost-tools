package sh.quatre.localhost.dev.proxy

import kotlinx.datetime.Instant

class LocalDevServer(val name: String)

class RunningDevServer(val server: LocalDevServer, val port: Int, val startedAt: Instant)
