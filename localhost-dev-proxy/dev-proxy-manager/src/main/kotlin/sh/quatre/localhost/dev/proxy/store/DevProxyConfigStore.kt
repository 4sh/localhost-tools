package sh.quatre.localhost.dev.proxy.store

import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.nio.file.Path

class DevProxyConfigStore(val source: Path) {
    fun save(servers: List<RunningDevServer>) {
        servers
            .map { server ->
                "${server.server.name}\t${server.port}\t${server.startedAt}"
            }
            .joinToString("\n")
            .also {
                source.toFile().writeText(it)
                logger.info("update conf at $source - ${servers.size} servers")
            }
    }

    fun load(): List<RunningDevServer> =
        source
            .toFile()
            .takeIf { it.exists() }
            ?.readText()
            ?.lines()
            ?.map { line -> line.parseAsRunningDevServer() }
            ?.filterNotNull()
            ?: listOf()

    fun String.parseAsRunningDevServer() =
        split('\t')
            .takeIf { it.size == 3 }
            ?.let { RunningDevServer(LocalDevServer(it[0]), it[1].toInt(), Instant.parse(it[2])) }

    companion object {
        private val logger = LogManager.getLogger(DevProxyConfigStore::class.java)
    }
}
