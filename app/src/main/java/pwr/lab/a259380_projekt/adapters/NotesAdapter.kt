package pwr.lab.a259380_projekt.adapters

import android.annotation.SuppressLint
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pwr.lab.a259380_projekt.R
import pwr.lab.a259380_projekt.entities.Note
import java.util.*
import java.util.logging.Handler

class NotesAdapter(
    private var notes: List<Note>,
    private val listener: NotesListener
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textTitle)
        val dateTimeView: TextView = itemView.findViewById(R.id.textDataTime)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedNote = notes[position]
                    listener.onClick(clickedNote, position)
                }

            }
        }
    }

    private val notesSource = notes
    private var timer: Timer? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.dateTimeView.text = note.dateTime
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun searchNotes(searchKeyword: String) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                notes = if (searchKeyword.trim().isEmpty()) {
                    notesSource
                } else {
                    val temp = ArrayList<Note>()
                    for (note in notesSource) {
                        if (note.title.lowercase().contains(searchKeyword.lowercase())
                            || note.noteText.lowercase().contains(searchKeyword.lowercase())) {
                            temp.add(note)
                        }
                    }
                    temp
                }

                android.os.Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }
            }
        }, 500)
    }

    fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
        }
    }

    interface NotesListener {
        fun onClick(note: Note, position: Int)
    }
}