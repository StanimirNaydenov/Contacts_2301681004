package com.example.contacts

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManager(
    context: Context,
    private val dao: ContactDao,
    private val remote: FirebaseContactService = FirebaseContactService(),
    private val connectivity: ConnectivityObserver = ConnectivityObserver(context)
) {
    // Собствен жизнен цикъл – живее колкото приложението.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        // (1) PULL – сваляне от облака в реално време, само когато ИМА интернет.
        scope.launch {
            connectivity.observe()
                .filter { online -> online }
                .flatMapLatest { remote.observeRemoteChanges() }
                .collect { remoteList -> applyRemote(remoteList) }
        }

        // (2) PUSH – качване на локалните промени, когато ИМА интернет
        scope.launch {
            combine(connectivity.observe(), dao.observeDirty()) { online, dirty ->
                online to dirty
            }
                .filter { (online, dirty) -> online && dirty.isNotEmpty() }
                .collect { (_, dirty) -> pushDirty(dirty) }
        }
    }

    // ---- Качване на локалните промени към Firestore ----
    private suspend fun pushDirty(dirty: List<Contact>) {
        for (local in dirty) {
            try {

                // Присвояваме му нов уникален идентификатор.
                val toPush =
                    if (local.firebaseId.isEmpty())
                        local.copy(firebaseId = UUID.randomUUID().toString())
                    else local
                remote.push(toPush)
                dao.update(toPush.copy(isDirty = false)) // успех → вече не е "мръсен"
            } catch (e: Exception) {
                // Грешка (напр. прекъсната връзка) – оставяме isDirty = true.
            }
        }
    }

    // ---- Прилагане на дошлите от облака промени към локалната база ----
    private suspend fun applyRemote(remoteList: List<RemoteContact>) {
        for (r in remoteList) {
            val local = dao.getByFirebaseId(r.firebaseId)
            when {
                // Нов запис, какъвто нямаме локално.
                local == null -> {
                    if (!r.deleted) {
                        dao.insert(
                            Contact(
                                name = r.name, phone = r.phone, email = r.email,
                                address = r.address, photoUrl = r.photoUrl, photoPath = "",
                                firebaseId = r.firebaseId, version = r.version,
                                isDirty = false, isDeleted = false, updatedAt = r.updatedAt
                            )
                        )
                    }
                }

                r.version > local.version -> {
                    dao.update(
                        local.copy(
                            name = r.name, phone = r.phone, email = r.email,
                            address = r.address, photoUrl = r.photoUrl,
                            version = r.version, isDeleted = r.deleted,
                            isDirty = false, updatedAt = r.updatedAt

                        )
                    )
                }

                else -> { /* нищо */ }
            }
        }
    }

    fun stop() = scope.cancel()
}