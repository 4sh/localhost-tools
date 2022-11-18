package sh.quatre.localhost.dev.proxy.store

import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.io.File

class DevProxyConfigStore(val source: File) {
    fun save(servers: List<RunningDevServer>) = Unit
    fun load(): List<RunningDevServer> = listOf()
}