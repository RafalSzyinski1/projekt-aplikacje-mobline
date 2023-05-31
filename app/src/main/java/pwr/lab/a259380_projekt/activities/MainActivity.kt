package pwr.lab.a259380_projekt.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pwr.lab.a259380_projekt.R
import pwr.lab.a259380_projekt.adapters.NotesAdapter
import pwr.lab.a259380_projekt.dao.NoteDao
import pwr.lab.a259380_projekt.database.DatabaseProvider
import pwr.lab.a259380_projekt.entities.Note
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var noteDao: NoteDao
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // For test comment this
        showBiometricPrompt()
        // findViewById<ConstraintLayout>(R.id.activityMain).visibility = View.VISIBLE

        noteDao = DatabaseProvider.getNotesDatabase(applicationContext).noteDao()
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        findViewById<ImageView>(R.id.imageAddNoteMain).setOnClickListener {
            val intent = Intent(this@MainActivity, CreateNoteActivity::class.java)
            intent.putExtra("isViewOrUpdate", false)
            startActivity(intent)
        }

        findViewById<EditText>(R.id.inputSearch).addTextChangedListener (
            object: TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    notesAdapter.cancelTimer()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    notesAdapter.searchNotes(p0.toString())
                }
            })
        }


    override fun onStart() {
        super.onStart()
        loadNotes()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadNotes() {
        GlobalScope.launch(Dispatchers.IO) {
            val notes = noteDao.getAllNotes()

            runOnUiThread {
                notesAdapter = NotesAdapter(notes, object : NotesAdapter.NotesListener {
                    override fun onClick(note: Note, position: Int) {
                        val intent = Intent(this@MainActivity, CreateNoteActivity::class.java)
                        intent.putExtra("note_id", note.id)
                        intent.putExtra("isViewOrUpdate", true)
                        startActivity(intent)
                    }
                })
                notesRecyclerView.adapter = notesAdapter
            }
        }
    }

    private fun showBiometricPrompt() {

        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticateWithBiometrics()
            }
            else -> {
                Log.w("BIOMETRIC", "Cannot use biometric on device. Access deny")
                setContentView(R.layout.activity_access_deny)
            }
        }
    }

    private fun authenticateWithBiometrics() {
        val biometricPrompt = createBiometricPrompt()
        val promptInfo = createPromptInfo()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        return BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.w("BIOMETRIC", "Error while authenticate. Access deny")
                setContentView(R.layout.activity_access_deny)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                findViewById<ConstraintLayout>(R.id.activityMain).visibility = View.VISIBLE
            }

            override fun onAuthenticationFailed() {
                Log.w("BIOMETRIC", "Cannot verify biometric on device")
            }
        })
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Please authenticate using your biometrics")
            .setNegativeButtonText("Cancel")
            .build()
    }
}
