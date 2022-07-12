package `in`.aerem.ostranna_flasks.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        // TODO: add a route to get an html page with a list of actions
        
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
