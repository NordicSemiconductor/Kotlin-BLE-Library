package no.nordicsemi.android.kotlin.ble.core.mutex

import kotlinx.coroutines.sync.Mutex

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
