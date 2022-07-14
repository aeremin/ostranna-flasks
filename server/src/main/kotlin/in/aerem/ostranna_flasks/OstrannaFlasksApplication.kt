package `in`.aerem.ostranna_flasks

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import io.ktor.util.logging.*
import java.io.FileInputStream
import `in`.aerem.ostranna_flasks.ComPort.*
import java.util.*

data class ActionEntry(
    val professor: String = "",
    val department: String = "",
    val amount: Int = 0,
    val timestamp: Long = 0,
    val token: String? = null
)

data class Professor(
    val name: String = "",
    val limit: Int = 0,
)


class OstrannaFlasksApplication {
    private val comport: ComPort
    private val log: Logger
    private val database: DatabaseReference
    private val dailyLimits = mutableMapOf<String, Int>().withDefault { 0 }
    private var subscribedToActions = false
    private var actions = listOf<ActionEntry>()

    constructor(log: Logger, comPortName: String) {
        this.log = log
        this.comport = openComport(log, comPortName)

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream("ostranna-flasks-account-key.json")))
            .setDatabaseUrl("https://ostranna-flasks-default-rtdb.europe-west1.firebasedatabase.app/")
            .build()
        FirebaseApp.initializeApp(options)

        database = FirebaseDatabase.getInstance().reference;
    }

    fun subscribeToFirebaseDatabase() {
        subscribeToProfessors()
    }

    fun getActions(): List<ActionEntry> {
        return actions
    }

    private fun subscribeToActionsIfNeeded() {
        if (subscribedToActions) return
        subscribedToActions = true
        database.child("actions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    log.info("Received new actions data from the server")
                    onNewActionsSnapshot(snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                log.error("Error while listening for actions updates: ${error?.message}")
            }
        })
    }

    private fun subscribeToProfessors() {
        database.child("professors").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    log.info("Received new professors data from the server")
                    onNewProfessorsSnapshot(snapshot)
                    subscribeToActionsIfNeeded()
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                log.error("Error while listening for professors updates: ${error?.message}")
            }
        })
    }

    private fun sameDate(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun onNewActionsSnapshot(snapshot: DataSnapshot) {
        try {
            val totals = mutableMapOf<String, Int>().withDefault { 0 }
            val totalAddedPerProfessor = mutableMapOf<String, Int>().withDefault { 0 }
            val totalRemovedPerProfessor = mutableMapOf<String, Int>().withDefault { 0 }
            actions = snapshot.children.toList().map { it.getValue(ActionEntry::class.java) }.sortedBy { it.timestamp }
            val today = Calendar.getInstance()
            val exceededLimitTokens = mutableSetOf<String>()

            for (action in actions) {
                val t = Calendar.getInstance().also { it.timeInMillis = action.timestamp }
                totals[action.department] = totals.getValue(action.department) + action.amount
                if (sameDate(today, t)) {
                    totalAddedPerProfessor[action.professor] =
                        totalAddedPerProfessor.getValue(action.professor) + action.amount
                    totalRemovedPerProfessor[action.professor] =
                        totalRemovedPerProfessor.getValue(action.professor) - action.amount

                    if (totalAddedPerProfessor[action.professor]!! > dailyLimits.getValue(action.professor)) {
                        totals[action.department] =
                            totals.getValue(action.department) - (totalAddedPerProfessor[action.professor]!! - dailyLimits.getValue(action.professor))
                        totalAddedPerProfessor[action.professor] = dailyLimits.getValue(action.professor)
                        if (action.token != null) {
                            exceededLimitTokens.add(action.token)
                        }
                    }

                    if (totalRemovedPerProfessor[action.professor]!! > dailyLimits.getValue(action.professor)) {
                        totals[action.department] =
                            totals.getValue(action.department) + (totalRemovedPerProfessor[action.professor]!! - dailyLimits.getValue(action.professor))
                        totalRemovedPerProfessor[action.professor] = dailyLimits.getValue(action.professor)
                        if (action.token != null) {
                            exceededLimitTokens.add(action.token)
                        }
                    }
                }
            }
            val command = "Set ${totals.getValue("Гриффиндор")}, ${totals.getValue("Слизерин")}, ${totals.getValue("Когтевран")}, ${totals.getValue("Пуффендуй")}\n"
            log.info("Writing to COM port: ${command.trim()}")
            try {
                comport.write(command)
            } catch (e: Exception) {
                log.error("Failed to write to com port: ${e}")
            }

        } catch (e: Exception) {
            log.error(e.message)
        }
    }

    private fun onNewProfessorsSnapshot(snapshot: DataSnapshot) {
        dailyLimits.clear()
        for (child in snapshot.children) {
            try {
                val professor = child.getValue(Professor::class.java)
                dailyLimits[professor.name] = professor.limit
            } catch (e: Exception) {
                log.error("Failed to parse professor data: ${e.message}")
            }
        }
    }
}


