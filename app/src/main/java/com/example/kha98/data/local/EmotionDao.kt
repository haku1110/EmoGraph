package com.kha98.emograph.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kha98.emograph.data.model.EmotionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionDao {

    @Query("SELECT * FROM emotion_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EmotionRecord>>

    @Query("SELECT DISTINCT category FROM emotion_records ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: EmotionRecord)

    @Query("SELECT * FROM emotion_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): EmotionRecord?

    @Query("DELETE FROM emotion_records WHERE id = :id")
    suspend fun deleteRecord(id: String)
}
