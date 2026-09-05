//The core Room Database instance. Configures SQLite entities (User, Session, ProgressLog) and
// exposes the DAOs.

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ProgressLog
import com.example.data.model.Session
import com.example.data.model.User

@Database(
    entities = [User::class, Session::class, ProgressLog::class],
    version = 2,
    exportSchema = false
)
abstract class CodeStreakDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun progressLogDao(): ProgressLogDao

    companion object {
        @Volatile
        private var INSTANCE: CodeStreakDatabase? = null

        fun getDatabase(context: Context): CodeStreakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CodeStreakDatabase::class.java,
                    "codestreak_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

