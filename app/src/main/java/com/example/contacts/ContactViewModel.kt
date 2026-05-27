package com.example.contacts

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.util.UUID

class ContactViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).contactDao()
    // getAll() вече връща само НЕизтритите записи.
    val allContacts: LiveData<List<Contact>> = dao.getAll().asLiveData()

    fun addContact(contact: Contact) = viewModelScope.launch {
        dao.insert(
            contact.copy(
                firebaseId = if (contact.firebaseId.isEmpty())
                    UUID.randomUUID().toString() else contact.firebaseId,
                version = 1L,
                isDirty = true, // нов запис → за качване
                isDeleted = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        dao.update(
            contact.copy(
                version = contact.version + 1, // нова версия
                isDirty = true, // за качване
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        // "Мек" delete: не трием реда, а го маркираме като изтрит и качваме това
        // състояние, за да изчезне записът и от другите устройства.
        dao.update(
            contact.copy(
                isDeleted = true,
                version = contact.version + 1,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getContactById(id: Int): Contact? = dao.getContactById(id)
}