package pwr.lab.a259380_projekt.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import pwr.lab.a259380_projekt.dao.NoteDao
import pwr.lab.a259380_projekt.entities.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
}