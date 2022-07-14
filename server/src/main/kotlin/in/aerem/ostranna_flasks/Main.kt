package `in`.aerem.ostranna_flasks

import `in`.aerem.ostranna_flasks.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    if (args.size != 1) throw Exception("Please provide exactly one command-line argument - com-port device")

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val app = OstrannaFlasksApplication(log, args[0])
        app.subscribeToFirebaseDatabase()
        configureRouting(app)
    }.start(wait = true)
}
