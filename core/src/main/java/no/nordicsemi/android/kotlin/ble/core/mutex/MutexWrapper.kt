package no.nordicsemi.android.kotlin.ble.core.mutex

import kotlinx.coroutines.sync.Mutex

/**
 * Wrapper class around [Mutex]. It is used to for debugging when [lock], [unlock] functions needs
 * to be logged.
 *
 * Mutex should be shared between all client calls to GATT client, otherwise pending calls will be
 * ignored by Android API.
 *
 * @property mutex Original Kotlin [Mutex].
 */
class MutexWrapper(private val mutex: Mutex = Mutex()) {

    suspend fun lock() {
        mutex.lock()
    }

    fun tryLock() {
        mutex.tryLock()
    }

    fun unlock() {
        mutex.unlock()
    }
}
