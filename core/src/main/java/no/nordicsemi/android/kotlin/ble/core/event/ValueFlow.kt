/*
 * Copyright (c) 2023, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.kotlin.ble.core.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

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
         * @param bufferSize A buffer size for incoming values.
         * @return An instance of [ValueFlow].
         */
        fun create(bufferSize: Int): ValueFlow {
            return ValueFlow(
                MutableSharedFlow(
                    replay = 1,
                    extraBufferCapacity = bufferSize,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST
                )
            )
        }
    }
}
