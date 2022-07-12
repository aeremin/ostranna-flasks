package `in`.aerem.ostranna_flasks

import `in`.aerem.ostranna_flasks.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        val app = OstrannaFlasksApplication(log)
        app.subscribeToFirebaseDatabase()
    }.start(wait = true)
}
