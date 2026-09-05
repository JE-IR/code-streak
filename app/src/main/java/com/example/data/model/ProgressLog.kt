//Defines the database schemas, primary keys, foreign key relations, and timestamp columns.
package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_logs",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ProgressLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val userId: Long,
    val logType: String, // "SHAKE_GESTURE", "POMODORO_LAP", "PROBLEM_SOLVED", "MILESTONE"
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val quoteSnippet: String = ""
)
