package com.example.satfinderpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.satfinderpro.data.dao.AlignmentDao
import com.example.satfinderpro.data.dao.UserDao
import com.example.satfinderpro.data.model.Alignment
import com.example.satfinderpro.data.model.User

@Database(entities = [User::class, Alignment::class], version = 1, exportSchema = false)
abstract class SatFinderDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun alignmentDao(): AlignmentDao

    companion object {
        @Volatile
        private var INSTANCE: SatFinderDatabase? = null

        fun getDatabase(context: Context): SatFinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SatFinderDatabase::class.java,
                    "satfinder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
