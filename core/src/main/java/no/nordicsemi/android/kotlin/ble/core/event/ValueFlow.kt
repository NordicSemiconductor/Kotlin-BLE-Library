package no.nordicsemi.android.kotlin.ble.core.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.common.core.DataByteArray

/**
 * Helper class which joins capability of setting up an extra buffer from [MutableSharedFlow] with
 * a value field available for [StateFlow].
 *
 * @property mutableSharedFlow An instance which provides [SharedFlow].
 */
class ValueFlow private constructor(
    private val mutableSharedFlow: MutableSharedFlow<DataByteArray>,
) : MutableSharedFlow<DataByteArray> by mutableSharedFlow {

    /**
     * Returns last value emitted to the [MutableSharedFlow].
     */
    val value
        get() = mutableSharedFlow.replayCache.firstOrNull() ?: DataByteArray()

    companion object {

        /**
         * Creates an instance of [ValueFlow] with predefined parameters.
         *
         * @return
         */
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
