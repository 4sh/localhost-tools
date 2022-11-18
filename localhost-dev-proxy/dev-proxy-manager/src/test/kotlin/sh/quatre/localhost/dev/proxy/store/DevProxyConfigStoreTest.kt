package sh.quatre.localhost.dev.proxy.store

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import java.nio.file.Path

class DevProxyConfigStoreTest {
    @Test
    fun `should save and load servers state`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("conf")
        val store = DevProxyConfigStore(file)

        val startedAt = Clock.System.now()
        val servers = listOf(
            runningDevServer("my-web-server", 8080, startedAt),
            runningDevServer("my-other-server", 8081, startedAt)
        )

        store.save(servers)

        expectThat(file.toFile().exists()).isTrue()
        expectThat(file.toFile().readText()).isEqualTo(
            """
                my-web-server${"\t"}8080${"\t"}$startedAt
                my-other-server${"\t"}8081${"\t"}$startedAt
            """.trimIndent()
        )

        val loadedServers = store.load()

        expectThat(loadedServers).hasSize(servers.size).isEqualTo(servers)
    }

    private fun runningDevServer(name: String, port: Int, startedAt: Instant) =
        RunningDevServer(LocalDevServer(name), port, startedAt)
}
