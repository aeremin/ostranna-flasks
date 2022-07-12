package `in`.aerem.ostranna_flasks

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FlasksApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}