package com.kha98.emograph.data.repository

import android.content.Context
import com.kha98.emograph.data.local.EmotionDatabase
import com.kha98.emograph.data.model.EmotionRecord
import kotlinx.coroutines.flow.Flow

object EmotionRepository {

    private lateinit var db: EmotionDatabase

    fun init(context: Context) {
        db = EmotionDatabase.getInstance(context)
    }

    fun getRecords(): Flow<List<EmotionRecord>> = db.emotionDao().getAllRecords()

    fun getCategories(): Flow<List<String>> = db.emotionDao().getAllCategories()

    suspend fun getRecordById(id: String): EmotionRecord? = db.emotionDao().getRecordById(id)

    suspend fun addRecord(record: EmotionRecord) {
        db.emotionDao().insertRecord(record)
    }

    suspend fun deleteRecord(id: String) {
        db.emotionDao().deleteRecord(id)
    }
}
