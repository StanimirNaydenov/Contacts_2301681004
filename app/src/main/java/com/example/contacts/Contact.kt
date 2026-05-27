package com.example.contacts

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["name", "phone"], unique = true)]
)
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val photoUrl: String,
    val photoPath: String,
    // Уникален идентификатор, ОБЩ за всички устройства.
    // Това е и ID на документа във Firestore.
    val firebaseId: String = "",
    // Версия на записа. Нараства при всяка локална промяна.
    // Използва се за разрешаване на конфликти.
    val version: Long = 1L,
    // true = има локални промени, които още НЕ са качени в облака.
    val isDirty: Boolean = true,
    // true = записът е изтрит.
    // Не се показва в списъка, но се синхронизира, за да изчезне и от другите устройства.
    val isDeleted: Boolean = false,
    // Кога е променен за последно. Резервен критерий при конфликт.
    val updatedAt: Long = System.currentTimeMillis()
)