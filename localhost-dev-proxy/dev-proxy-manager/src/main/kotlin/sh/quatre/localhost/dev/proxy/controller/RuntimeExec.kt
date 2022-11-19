package sh.quatre.localhost.dev.proxy.controller

import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.file.Path

private val logger = LogManager.getLogger("exec")

fun String.runDaemon(): Process? {
    return try {
        logger.info("$ `$this`")
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .inheritIO()
            .start()

        proc!!
    } catch (e: IOException) {
        logger.error("error executing ${this}\n->${e.message}", e)
        null
    }
}

fun String.copyResourceTo(dir: Path) {
    val res = this
    dir.resolve(res).toFile().outputStream().use { out ->
        Thread.currentThread().contextClassLoader.getResourceAsStream("$res")?.use { input ->
            logger.info("copy $res to $dir")
            input.copyTo(out)
        } ?: also { logger.warn("resource $res not found") }
    }
}
