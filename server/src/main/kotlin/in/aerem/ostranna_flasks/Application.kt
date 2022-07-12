package `in`.aerem.ostranna_flasks

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import `in`.aerem.ostranna_flasks.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.FileInputStream

data class ActionEntry(
    val professor: String = "",
    val department: String = "",
    val amount: Int = 0,
    val timestamp: Long = 0,
    val token: String? = null
)

fun openComport(): SerialPort {
    val comport = SerialPort.getCommPort("COM4")
    comport.baudRate = 115200
    comport.numStopBits = SerialPort.ONE_STOP_BIT
    comport.openPort()
    comport.addDataListener(object: SerialPortDataListener {
        override fun getListeningEvents(): Int {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED
        }

        override fun serialEvent(event: SerialPortEvent?) {
            if (event == null) return
            println(String(event.receivedData))
        }
    })
    return comport
}

fun main() {
    val comport = openComport()

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("ostranna-flasks-account-key.json")))
        .setDatabaseUrl("https://ostranna-flasks-default-rtdb.europe-west1.firebasedatabase.app/")
        .build()
    FirebaseApp.initializeApp(options)
    val database = FirebaseDatabase.getInstance().reference;
        database.child("actions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot == null) return
                val totals = mutableMapOf<String, Int>().withDefault { 0 }
                for (children in snapshot.children) {
                    val entry = children.getValue(ActionEntry::class.java)
                    totals[entry.department] = totals.getValue(entry.department) + entry.amount
                }
                val command = "Set ${totals.getValue("Гриффиндор")}, ${totals.getValue("Слизерин")}, ${totals.getValue("Когтевран")}, ${totals.getValue("Пуффендуй")}\n"
                println(command)
                comport.writeBytes(command.toByteArray(), command.length.toLong())
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
