package `in`.aerem.ostranna_flasks

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import io.ktor.util.logging.*
import java.io.FileInputStream
import `in`.aerem.ostranna_flasks.ComPort.*

data class ActionEntry(
    val professor: String = "",
    val department: String = "",
    val amount: Int = 0,
    val timestamp: Long = 0,
    val token: String? = null
)

class OstrannaFlasksApplication {
    private val comport: ComPort
    private val log: Logger

    constructor(log: Logger, comPortName: String) {
        this.log = log
        this.comport = openComport(log, comPortName)

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

    private fun onNewActionsSnapshot(snapshot: DataSnapshot) {
        val totals = mutableMapOf<String, Int>().withDefault { 0 }
        // TODO: support daily limits
        for (children in snapshot.children) {
            val entry = children.getValue(ActionEntry::class.java)
            totals[entry.department] = totals.getValue(entry.department) + entry.amount
        }
        val command = "Set ${totals.getValue("Гриффиндор")}, ${totals.getValue("Слизерин")}, ${totals.getValue("Когтевран")}, ${totals.getValue("Пуффендуй")}\n"
        log.info("Writing to COM port: ${command.trim()}")
        try {
          comport.write(command)
        } catch (e: Exception) {
          log.error("Failed to write to com port: ${e.message}")
        }
    }
}


