package com.example.contacts

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    // Списъкът за UI – НЕ показваме изтритите.
    @Query("SELECT * FROM contacts WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): Flow<List<Contact>>

    @Update
    suspend fun update(contact: Contact)

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Int): Contact?

    @Delete
    suspend fun delete(contact: Contact)

    // Намира локалния запис по неговия firebaseId (за съпоставяне с облака).
    @Query("SELECT * FROM contacts WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getByFirebaseId(firebaseId: String): Contact?

    @Query("SELECT * FROM contacts WHERE isDirty = 1")
    fun observeDirty(): Flow<List<Contact>>
}