package no.nordicsemi.android.kotlin.ble.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SingleCall {
    val mutex = Mutex()
}

fun CoroutineScope.synchronisedLaunch(block: suspend () -> Unit) {
    launch {
        SingleCall.mutex.withLock { block() }
    }
}
