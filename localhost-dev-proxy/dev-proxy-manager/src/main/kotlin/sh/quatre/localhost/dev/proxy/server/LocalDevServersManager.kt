package sh.quatre.localhost.dev.proxy.server

import kotlinx.datetime.Clock
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.net.URL
import kotlin.random.Random
import kotlin.random.nextInt

class LocalDevServersManager(load: List<RunningDevServer>) {
    val servers: List<RunningDevServer> get() = _servers
    private val _servers = mutableListOf<RunningDevServer>()

    init {
        _servers.addAll(load)
    }

    fun register(server: LocalDevServer) = findAvailablePort().let { port ->
        RunningDevServer(server, port, Clock.System.now())
    }.also {
        register(it)
    }

    fun register(server: RunningDevServer) {
        servers.find { it.port == server.port }?.also {
            throw IllegalStateException("server port conflicts with $it")
        }
        servers.find { it.server.name == server.server.name }?.also {
            throw IllegalStateException("server name conflicts with $it")
        }
        _servers.add(server)
    }

    fun stopped(name: String) {
        _servers.removeIf { it.server.name == name }
    }

    fun findAvailablePort(): Int {
        var port: Int? = null
        while (port == null) {
            port = Random.nextInt(10000..30000).takeIf { isPortAvailable(it) }
        }
        return port
    }

    private fun isPortAvailable(port: Int) =
        servers.none { it.port == port } && !"http://localhost:$port/".canHttpConnect()

    private fun String.canHttpConnect(): Boolean = try {
        URL(this).openConnection().apply { connectTimeout = 200 }.connect()
        true
    } catch (e: Exception) {
        false
    }
}
