//Defines the database schemas, primary keys, foreign key relations, and timestamp columns.
package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val category: String, // e.g. "Algorithms / LeetCode", "Android / Kotlin", "Web Dev", "System Design", "Open Source"
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val status: String = "COMPLETED", // "IN_PROGRESS", "PAUSED", "COMPLETED", "CANCELLED"
    val notes: String = "",
    val shakeCount: Int = 0,
    val quoteSnippet: String = ""
)
