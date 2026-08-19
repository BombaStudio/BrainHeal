package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.CalendarEventDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.NoteDao
import com.example.data.dao.TaskDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.CalendarEventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.NoteEntity
import com.example.data.model.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        ExpenseEntity::class,
        CalendarEventEntity::class,
        NoteEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun noteDao(): NoteDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "odak_flow_adhd_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
