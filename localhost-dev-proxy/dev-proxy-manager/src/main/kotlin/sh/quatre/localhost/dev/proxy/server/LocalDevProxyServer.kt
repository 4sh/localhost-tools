package sh.quatre.localhost.dev.proxy.server

import org.apache.logging.log4j.LogManager
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.controller.EnvoyProxyController
import sh.quatre.localhost.dev.proxy.gen.ServersIndexGenerator
import sh.quatre.localhost.dev.proxy.store.DevProxyConfigStore
import java.nio.file.Paths

class LocalDevProxyServer {
    val controller = EnvoyProxyController()
    val store = DevProxyConfigStore(Paths.get("/etc/localhost-server-manager/conf"))
    val index = ServersIndexGenerator()
    val manager = LocalDevServersManager(store.load())

    fun updateServers() {
        store.save(manager.servers)
        controller.updateServers(manager.servers)
    }

    fun startAndAwait(port: Int) {
        logger.info("starting local dev proxy server on port $port")
        if (!manager.servers.isEmpty()) {
            logger.info("loaded servers: ${manager.servers}")
        }

        controller.updateServers(manager.servers)
        controller.start()

        LocalDevProxyHttpServer().start(
            port,
            object : ServerCallbacks {
                override fun onServerStarting(name: String): Int {
                    logger.info("server starting: $name")
                    return (
                        manager.servers.find { it.server.name == name }
                            ?: manager.register(LocalDevServer(name)).also {
                                logger.info("new server starting: $it")
                                updateServers()
                            }
                        ).port
                }

                override fun onServerStopped(name: String) {
                    logger.info("server stopped: $name")
                    manager.stopped(name)
                    updateServers()
                }

                override fun serveServersList() = index.generateToString(manager.servers)
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
