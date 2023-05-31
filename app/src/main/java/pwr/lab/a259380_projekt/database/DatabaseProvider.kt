package pwr.lab.a259380_projekt.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var notesDatabase: NotesDatabase? = null

    fun getNotesDatabase(context: Context): NotesDatabase {
        if (notesDatabase == null) {
            synchronized(DatabaseProvider::class) {
                notesDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).build()
            }
        }
        return notesDatabase!!
    }
}