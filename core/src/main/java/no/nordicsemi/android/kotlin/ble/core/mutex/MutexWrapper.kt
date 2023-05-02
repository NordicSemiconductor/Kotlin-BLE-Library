package no.nordicsemi.android.kotlin.ble.core.mutex

import android.util.Log
import kotlinx.coroutines.sync.Mutex

class MutexWrapper(private val mutex: Mutex = Mutex()) {

    suspend fun lock() {
        Log.d("AAATESTAAA", "Mutex lock")
        mutex.lock()
    }

    fun tryLock() {
        Log.d("AAATESTAAA", "Mutex try lock")
        mutex.tryLock()
    }

    fun unlock() {
        Log.d("AAATESTAAA", "Mutex unlock")
        mutex.unlock()
    }
}
