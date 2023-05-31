package pwr.lab.a259380_projekt.dao

import androidx.room.*
import pwr.lab.a259380_projekt.entities.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Int): Note

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}