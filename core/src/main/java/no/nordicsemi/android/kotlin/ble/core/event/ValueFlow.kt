package no.nordicsemi.android.kotlin.ble.core.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalApi
class ValueFlow private constructor(
    private val mutableSharedFlow: MutableSharedFlow<ByteArray>
) : MutableSharedFlow<ByteArray> by mutableSharedFlow {

    val value = mutableSharedFlow.replayCache.firstOrNull() ?: byteArrayOf()

    companion object {
        fun create(): ValueFlow {
            return ValueFlow(
                MutableSharedFlow(
                    replay = 1,
                    extraBufferCapacity = 10,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST
                )
            )
        }
    }
}
