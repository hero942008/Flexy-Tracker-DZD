package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FlexyTransaction

@Database(entities = [FlexyTransaction::class], version = 1, exportSchema = false)
abstract class FlexyDatabase : RoomDatabase() {
    abstract fun flexyDao(): FlexyDao

    companion object {
        @Volatile
        private var INSTANCE: FlexyDatabase? = null

        fun getDatabase(context: Context): FlexyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlexyDatabase::class.java,
                    "flexy_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
