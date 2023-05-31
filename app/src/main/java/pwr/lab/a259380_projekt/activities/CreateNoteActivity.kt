package pwr.lab.a259380_projekt.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import pwr.lab.a259380_projekt.R
import pwr.lab.a259380_projekt.dao.NoteDao
import pwr.lab.a259380_projekt.database.DatabaseProvider
import pwr.lab.a259380_projekt.entities.Note
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle: EditText
    private lateinit var inputNoteText: EditText
    private lateinit var textDateTime: TextView

    private lateinit var noteDao: NoteDao

    private var viewOrUpdate = false
    private var noteUpdate: Note? = null

    @SuppressLint("InflateParams")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        noteDao = DatabaseProvider.getNotesDatabase(applicationContext).noteDao()

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDataTime)

        textDateTime.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"))

        viewOrUpdate = intent.getBooleanExtra("isViewOrUpdate", false)
        if (viewOrUpdate) {
            val extra = intent.getIntExtra("note_id", -1)
            if (extra != -1) {
                GlobalScope.launch(Dispatchers.IO) {
                    noteUpdate = noteDao.getNote(extra)
                    inputNoteTitle.setText(noteUpdate!!.title)
                    inputNoteText.setText(noteUpdate!!.noteText)
                    textDateTime.text = noteUpdate!!.dateTime
                }
            }
        }

        findViewById<ImageView>(R.id.imageDel).setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, null)
            val alertDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(0))
            dialogView.findViewById<TextView>(R.id.textDeleteNote).setOnClickListener {
                if (noteUpdate != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        noteDao.delete(noteUpdate!!)
                    }
                }
                alertDialog.dismiss()
                Toast.makeText(this, "Note delete", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }

            dialogView.findViewById<TextView>(R.id.textCancel).setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        findViewById<ImageView>(R.id.imageBack).setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        findViewById<ImageView>(R.id.imageMic).setOnClickListener {
            askSpeechInput()
        }
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveNote() {
        if (inputNoteTitle.text.toString().trim().isEmpty()) {
            return
        }
        val note = Note(title=inputNoteTitle.text.toString(),
            dateTime=textDateTime.text.toString(),
            noteText=inputNoteText.text.toString())

        GlobalScope.launch(Dispatchers.IO) {
            if (viewOrUpdate && noteUpdate != null) {
                if (noteUpdate!!.title != note.title ||
                        noteUpdate!!.noteText != note.noteText) {
                    noteUpdate!!.title = note.title
                    noteUpdate!!.noteText = note.noteText
                    noteUpdate!!.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"))
                    noteDao.update(noteUpdate!!)
                }
            } else {
                noteDao.insert(note)
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val speechResult = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            inputNoteText.append(speechResult?.get(0).toString())
        }
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say")
            resultLauncher.launch(i)
        }
    }
}