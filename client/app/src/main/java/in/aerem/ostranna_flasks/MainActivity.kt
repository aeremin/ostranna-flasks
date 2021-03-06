package `in`.aerem.ostranna_flasks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import `in`.aerem.ostranna_flasks.ui.main.MainFragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment())
                    .commitNow()
        }
    }
}