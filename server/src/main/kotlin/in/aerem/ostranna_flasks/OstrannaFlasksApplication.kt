package `in`.aerem.ostranna_flasks

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import io.ktor.util.logging.*
import java.io.FileInputStream

data class ActionEntry(
    val professor: String = "",
    val department: String = "",
    val amount: Int = 0,
    val timestamp: Long = 0,
    val token: String? = null
)

class OstrannaFlasksApplication {
    private val comport = openComport()
    private val log: Logger

    constructor(log: Logger) {
        this.log = log

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream("ostranna-flasks-account-key.json")))
            .setDatabaseUrl("https://ostranna-flasks-default-rtdb.europe-west1.firebasedatabase.app/")
            .build()
        FirebaseApp.initializeApp(options)
    }

    fun subscribeToFirebaseDatabase() {
        val database = FirebaseDatabase.getInstance().reference;
        database.child("actions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    onNewActionsSnapshot(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                log.error("Error while listening for actions updates: ${error?.message}")
            }
        })
    }

    private fun openComport(): SerialPort {
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
                log.info("Received from COM port: ${String(event.receivedData).trim()}")
            }
        })
        return comport
    }

    private fun onNewActionsSnapshot(snapshot: DataSnapshot) {
        val totals = mutableMapOf<String, Int>().withDefault { 0 }
        // TODO: support daily limits
        for (children in snapshot.children) {
            val entry = children.getValue(ActionEntry::class.java)
            totals[entry.department] = totals.getValue(entry.department) + entry.amount
        }
        val command = "Set ${totals.getValue("Гриффиндор")}, ${totals.getValue("Слизерин")}, ${totals.getValue("Когтевран")}, ${totals.getValue("Пуффендуй")}\n"
        log.info("Writing to COM port: ${command.trim()}")
        comport.writeBytes(command.toByteArray(), command.length.toLong())
    }
}


