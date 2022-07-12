package `in`.aerem.ostranna_flasks

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import `in`.aerem.ostranna_flasks.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.FileInputStream


fun main() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("ostranna-flasks-account-key.json")))
        .setDatabaseUrl("https://ostranna-flasks-default-rtdb.europe-west1.firebasedatabase.app/")
        .build()
    FirebaseApp.initializeApp(options)
    val database = FirebaseDatabase.getInstance().reference;
        database.child("actions").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot == null) return
                println(snapshot.childrenCount)
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
