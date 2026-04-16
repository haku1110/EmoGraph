package com.kha98.emograph.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kha98.emograph.data.model.EmotionRecord

@Database(entities = [EmotionRecord::class], version = 1, exportSchema = false)
abstract class EmotionDatabase : RoomDatabase() {

    abstract fun emotionDao(): EmotionDao

    companion object {
        @Volatile
        private var INSTANCE: EmotionDatabase? = null

        fun getInstance(context: Context): EmotionDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    EmotionDatabase::class.java,
                    "emograph_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
