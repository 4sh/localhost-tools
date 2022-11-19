package sh.quatre.localhost.dev.proxy.controller

import kotlinx.datetime.Clock
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import sh.quatre.localhost.dev.proxy.gen.EnvoyConfGenerator
import sh.quatre.localhost.dev.proxy.gen.ServersIndexGenerator
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

class EnvoyProxyController(private val dir: Path = Paths.get("/etc/localhost-server-manager")) {
    private val envoyConfDir = dir
    private val confFilePath = envoyConfDir.resolve("envoy.yaml")
    private val indexFilePath = envoyConfDir.resolve("default.html")

    private val started get() = process != null
    private var process: Process? = null

    private val confGen = EnvoyConfGenerator()
    private val indexGen = ServersIndexGenerator()

    fun updateServers(servers: List<RunningDevServer>) {
        envoyConfDir.toFile().mkdirs()
        confFilePath.toFile().writeText(confGen.generateToString(servers))
        indexFilePath.toFile().writeText(indexGen.generateToString(servers))

        if (started) {
            refresh()
        }
    }

    fun refresh() {
        stop()
        start()
    }

    fun start() {
        process = "envoy -c ${confFilePath.absolutePathString()}".runDaemon()
    }

    fun stop() {
        process?.destroy()
        process?.waitFor(10, TimeUnit.SECONDS)
        process = null
    }
}

fun main() {
    val controller = EnvoyProxyController()

    val servers =
        listOf(
            RunningDevServer(LocalDevServer("my-web-server"), 8000, Clock.System.now()),
            RunningDevServer(LocalDevServer("my-http2-server"), 8082, Clock.System.now())
        )

    controller.updateServers(servers)

    controller.start()
}
