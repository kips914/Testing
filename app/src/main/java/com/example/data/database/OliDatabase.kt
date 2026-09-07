package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.ChatDao
import com.example.data.database.dao.MemoryDao
import com.example.data.database.dao.MessageDao
import com.example.data.database.dao.ServerDao
import com.example.data.database.entities.ChatEntity
import com.example.data.database.entities.MemoryEntity
import com.example.data.database.entities.MessageEntity
import com.example.data.database.entities.ServerEntity

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        ServerEntity::class,
        MemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OliDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun serverDao(): ServerDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: OliDatabase? = null

        fun getInstance(context: Context): OliDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OliDatabase::class.java,
                    "oli_mobile.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
