package com.example.emograph.data.repository

import com.example.emograph.data.model.EmotionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object EmotionRepository {

    private val db get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    private fun emotionsCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("emotions")

    fun getRecords(): Flow<List<EmotionRecord>> = callbackFlow {
        val listener = emotionsCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.mapNotNull {
                    it.toObject(EmotionRecord::class.java)
                } ?: emptyList()
                trySend(records)
            }
        awaitClose { listener.remove() }
    }

    fun getCategories(): Flow<List<String>> = callbackFlow {
        val listener = emotionsCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents
                    ?.mapNotNull { it.getString("category") }
                    ?.distinct()
                    ?.sorted()
                    ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addRecord(record: EmotionRecord) {
        val uid = auth.currentUser?.uid ?: return
        emotionsCollection().add(record.copy(userId = uid)).await()
    }

    suspend fun deleteRecord(id: String) {
        emotionsCollection().document(id).delete().await()
    }
}
