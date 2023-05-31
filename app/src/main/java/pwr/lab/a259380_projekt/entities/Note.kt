package pwr.lab.a259380_projekt.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name="date_time") var dateTime: String,
    @ColumnInfo(name="note_text") var noteText: String,
        )