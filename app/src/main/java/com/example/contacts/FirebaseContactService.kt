package com.example.contacts

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


data class RemoteContact(
    val firebaseId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val version: Long = 0L,
    val deleted: Boolean = false,
    val updatedAt: Long = 0L
)

class FirebaseContactService {
    private val collection = FirebaseFirestore.getInstance().collection("contacts")

    // Качва (или презаписва) един запис. Документът се идентифицира по firebaseId.
    suspend fun push(contact: Contact) {
        val data = mapOf(
            "firebaseId" to contact.firebaseId,
            "name" to contact.name,
            "phone" to contact.phone,
            "email" to contact.email,
            "address" to contact.address,
            "photoUrl" to contact.photoUrl,
            "version" to contact.version,
            "deleted" to contact.isDeleted,
            "updatedAt" to contact.updatedAt
        )
        collection.document(contact.firebaseId).set(data).await()
    }

    fun observeRemoteChanges(): Flow<List<RemoteContact>> = callbackFlow {
        val registration: ListenerRegistration =
            collection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RemoteContact::class.java)?.copy(firebaseId = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        // При спиране на потока махаме слушателя, за да няма теч на ресурси.
        awaitClose { registration.remove() }
    }
}