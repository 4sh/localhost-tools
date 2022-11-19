package sh.quatre.localhost.dev.proxy.store

import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import sh.quatre.localhost.dev.proxy.DevServerProtocol
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.nio.file.Path

class DevProxyConfigStore(val source: Path) {
    fun save(servers: List<RunningDevServer>) {
        servers
            .map { server ->
                "${server.server.name}\t${server.server.protocol}\t${server.port}\t${server.startedAt}"
            }
            .joinToString("\n")
            .also {
                source.parent.toFile().mkdirs()
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
            .takeIf { it.size == 4 }
            ?.let {
                RunningDevServer(
                    server = LocalDevServer(name = it[0], protocol = DevServerProtocol.valueOf(it[1])),
                    port = it[2].toInt(),
                    startedAt = Instant.parse(it[3])
                )
            }

    companion object {
        private val logger = LogManager.getLogger(DevProxyConfigStore::class.java)
    }
}
