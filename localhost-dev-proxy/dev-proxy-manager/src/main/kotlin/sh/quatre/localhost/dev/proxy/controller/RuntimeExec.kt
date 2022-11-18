package sh.quatre.localhost.dev.proxy.controller

import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger("exec")

fun String.runCommand(workingDir: File) {
    try {
        logger.info("$ `$this`")
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        val output = proc.inputStream.bufferedReader().readText()
        logger.info(output)
    } catch (e: IOException) {
        logger.error("error executing ${this}\n->${e.message}", e)
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
