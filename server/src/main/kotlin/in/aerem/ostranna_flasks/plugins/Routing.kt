package `in`.aerem.ostranna_flasks.plugins

import `in`.aerem.ostranna_flasks.OstrannaFlasksApplication
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*
import java.text.SimpleDateFormat
import java.util.Calendar

fun Application.configureRouting(app: OstrannaFlasksApplication) {
    val formatter = SimpleDateFormat("dd.MM hh:mm")
    routing {
        get("/") {
            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title {
                        +"История начислений"
                    }
                }
                unsafe {
                    +"""<style>
                        table {
                        font-family: arial, sans-serif;
                        border-collapse: collapse;
                        width: 100%;
                    }

                    td, th {
                        border: 1px solid #dddddd;
                        text-align: left;
                        padding: 8px;
                    }

                    tr:nth-child(even) {
                        background-color: #dddddd;
                    }</style>"""
                }
                body {
                    table {
                        for (action in app.getActions()) {
                            tr {
                                td {
                                    val cal = Calendar.getInstance().also { it.timeInMillis = action.timestamp }
                                    formatter.calendar = cal
                                    + formatter.format(cal.time)
                                }
                                td {
                                    + action.professor
                                }
                                td {
                                    + action.department
                                }
                                td {
                                    + action.amount.toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
