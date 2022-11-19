package sh.quatre.localhost.dev.proxy.server

import kotlinx.datetime.Clock
import org.apache.logging.log4j.LogManager
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import sh.quatre.localhost.dev.proxy.controller.EnvoyProxyController
import sh.quatre.localhost.dev.proxy.store.DevProxyConfigStore
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class LocalDevProxyServer {
    val controller = EnvoyProxyController()
    val store = DevProxyConfigStore(Paths.get("/etc/localhost-server-manager/conf"))

    fun updateServers(servers: List<RunningDevServer>) {
        store.save(servers)
        controller.updateServers(servers)
    }

    fun startAndAwait(port: Int) {
        logger.info("starting local dev proxy server on port $port")
        val servers = store.load().toMutableList()
        val ports = AtomicInteger(servers.map { it.port }.maxOrNull() ?: 10000)
        if (!servers.isEmpty()) {
            logger.info("loaded servers: $servers")
        }

        controller.updateServers(servers)
        controller.start()

        LocalDevProxyHttpServer().start(
            port,
            object : ServerCallbacks {
                override fun onServerStarting(name: String): Int {
                    logger.info("server starting: $name")
                    return (
                        servers.find { it.server.name == name }
                            ?: RunningDevServer(
                                server = LocalDevServer(name),
                                port = ports.incrementAndGet(),
                                startedAt = Clock.System.now()
                            ).also {
                                logger.info("new server starting: $it")
                                servers.add(it)
                                updateServers(servers)
                            }
                        ).port
                }

                override fun onServerStopped(name: String) {
                    logger.info("server stopped: $name")
                    servers.removeIf { it.server.name == name }
                    updateServers(servers)
                }
            }
        )
    }

    companion object {
        private val logger = LogManager.getLogger(LocalDevProxyServer::class.java)
    }
}

fun main() {
    val port = System.getenv("DEV_PROXY_CONTROL_PORT")?.toIntOrNull() ?: 9990

    LocalDevProxyServer().startAndAwait(port)
}
