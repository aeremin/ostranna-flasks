package `in`.aerem.ostranna_flasks.ui.main

import `in`.aerem.ostranna_flasks.R
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_main.*


data class Professor(
    val name: String = "",
    val limit: Int = 0,
)

data class ActionEntry(
    val professor: String,
    val department: String,
    val amount: Int,
    val timestamp: Long,
    val token: String?
)

class MainFragment : Fragment() {
    companion object {
        private const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel
    private val database = Firebase.database
    private var token: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result
                database.getReference("devices/$token").setValue("Connected!")
            }
        })

        populateDepartments()
        subscribeToProfessorsChanges()
        setUpButtons()
    }

    private fun subscribeToProfessorsChanges() {
        database.getReference("professors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val professors = dataSnapshot.getValue<Map<String, Professor>>()
                    val professorNames = arrayListOf<String>()
                    if (professors != null) {
                        for (professor in professors) {
                            Log.i(
                                TAG,
                                "Professor ${professor.value.name} (id: ${professor.key}) has limit of ${professor.value.limit}"
                            )
                            professorNames.add(professor.value.name)
                        }
                    }
                    professors_dropdown.adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.bigger_spinner, professorNames
                    )
                } catch (e: DatabaseException) {
                    // This is fine, probably DB is temporarily "broken", i.e. has an incomplete
                    // document
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun populateDepartments() {
        department_dropdown.adapter = ArrayAdapter(
            requireContext(),
            R.layout.bigger_spinner,
            arrayListOf("Гриффиндор", "Слизерин", "Когтевран", "Пуффендуй")
        )
    }

    private fun setUpButtons() {
        button_add.setOnClickListener { givePoints(
            professors_dropdown.selectedItem as String,
            department_dropdown.selectedItem as String,
            Integer.valueOf(points_amount.text.toString())) }

        button_substract.setOnClickListener { givePoints(
            professors_dropdown.selectedItem as String,
            department_dropdown.selectedItem as String,
            -Integer.valueOf(points_amount.text.toString())) }
    }

    private fun givePoints(professor: String, department: String, amount: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтвердите начисление")
            .setMessage("$professor начисляет факультету $department $amount баллов. Подтверждаете?")
            .setPositiveButton("Ок"
            ) { _, _ ->
                database.getReference("actions").push().setValue(
                    ActionEntry(
                        professor,
                        department,
                        amount,
                        System.currentTimeMillis(),
                        token
                    )
                )
            }
            .setNegativeButton("Отмена", null).show()

    }
}