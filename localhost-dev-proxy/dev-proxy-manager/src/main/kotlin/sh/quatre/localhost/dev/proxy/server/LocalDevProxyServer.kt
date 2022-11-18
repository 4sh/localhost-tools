package sh.quatre.localhost.dev.proxy.server

import org.apache.logging.log4j.LogManager

class LocalDevProxyServer {
    fun startAndAwait(port: Int) {
        logger.info("starting local dev proxy server on port $port")
    }

    companion object {
        private val logger = LogManager.getLogger(LocalDevProxyServer::class.java)
    }
}

fun main() {
    val port = System.getenv("DEV_PROXY_CONTROL_PORT")?.toIntOrNull() ?: 9990

    LocalDevProxyServer().startAndAwait(port)
}
