package sh.quatre.localhost.dev.proxy.controller

import kotlinx.datetime.Clock
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer
import sh.quatre.localhost.dev.proxy.gen.EnvoyConfGenerator
import java.nio.file.Path
import java.nio.file.Paths

class EnvoyProxyController(private val dir: Path = Paths.get("/tmp/localhost-dev-proxy")) {
    private val confDir = dir.resolve("envoy")
    private val confFilePath = confDir.resolve("envoy.yaml")

    private var initialized = false
    private var started = false

    fun updateConf(conf: String) {
        confDir.toFile().mkdirs()
        confFilePath.toFile().writeText(conf)

        if (started) {
            refresh()
        }
    }

    fun refresh() {
        "docker-compose --project-name localhost-dev-proxy restart".runCommand(dir.toFile())
    }

    fun start() {
        init()
        "docker-compose --project-name localhost-dev-proxy up --detach".runCommand(dir.toFile())
    }

    private fun init() {
        if (!initialized) {
            "docker-compose.yaml".copyResourceTo(dir)
            confDir.toFile().mkdirs()
            initialized = true
        }
    }

    fun stop() {
        "docker-compose --project-name localhost-dev-proxy down".runCommand(dir.toFile())
    }
}

fun main() {
    val controller = EnvoyProxyController()

    val conf = EnvoyConfGenerator().generateToString(
        listOf(
            RunningDevServer(LocalDevServer("my-web-server"), 8000, Clock.System.now()),
            RunningDevServer(LocalDevServer("my-http2-server"), 8082, Clock.System.now())
        )
    )

    controller.updateConf(conf)

    controller.start()
}
