package `in`.aerem.ostranna_flasks

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import `in`.aerem.ostranna_flasks.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
