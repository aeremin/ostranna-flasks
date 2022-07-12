package `in`.aerem.ostranna_flasks.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("MessagingService", "Got a messaging token: $token")
    }
}