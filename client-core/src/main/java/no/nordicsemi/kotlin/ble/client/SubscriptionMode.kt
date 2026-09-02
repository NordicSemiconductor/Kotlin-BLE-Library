/*
 * Copyright (c) 2026, Nordic Semiconductor
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
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.client.internal.BaseRemoteCharacteristic
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.util.MergeResult

/**
 * Selects how a characteristic subscribes for value changes.
 */
enum class SubscriptionMode {
    /**
     * Selects the subscription mode from the characteristic properties.
     *
     * Indications have priority over notifications when both are supported, preserving the
     * existing behavior.
     */
    AUTOMATIC,

    /** Requires and enables notifications. */
    NOTIFICATION,

    /** Requires and enables indications. */
    INDICATION,
}

/**
 * Enables or disables value changes using an explicit [mode].
 *
 * Disabling value changes ignores [mode]. Implementations not based on
 * [BaseRemoteCharacteristic] support only [SubscriptionMode.AUTOMATIC] when enabling.
 */
suspend fun RemoteCharacteristic.setNotifying(
    enabled: Boolean,
    mode: SubscriptionMode,
) {
    when (this) {
        is BaseRemoteCharacteristic -> setNotifying(enabled, mode)
        else -> {
            if (enabled && mode != SubscriptionMode.AUTOMATIC) unsupportedSubscriptionMode()
            setNotifying(enabled)
        }
    }
}

/**
 * Subscribes for value changes using an explicit [mode].
 */
fun RemoteCharacteristic.subscribe(
    mode: SubscriptionMode,
    onSubscription: suspend RemoteCharacteristic.() -> Unit = {},
): Flow<ByteArray> = when (this) {
    is BaseRemoteCharacteristic -> subscribe(mode, onSubscription)
    else -> {
        if (mode != SubscriptionMode.AUTOMATIC) unsupportedSubscriptionMode()
        subscribe(onSubscription)
    }
}

/**
 * Waits for a value change using an explicit [mode].
 */
suspend fun RemoteCharacteristic.waitForValueChange(
    mode: SubscriptionMode,
    rawDataFilter: ((ByteArray) -> Boolean) = { true },
    merge: suspend (accumulator: ByteArray, received: ByteArray, index: Int) -> MergeResult =
        { _, received, _ -> MergeResult.Completed(received) },
    filter: ((ByteArray) -> Boolean) = { true },
    trigger: suspend RemoteCharacteristic.() -> Unit = {},
): ByteArray = when (this) {
    is BaseRemoteCharacteristic -> waitForValueChange(mode, rawDataFilter, merge, filter, trigger)
    else -> {
        if (mode != SubscriptionMode.AUTOMATIC) unsupportedSubscriptionMode()
        waitForValueChange(rawDataFilter, merge, filter, trigger)
    }
}

private fun unsupportedSubscriptionMode(): Nothing =
    throw OperationFailedException(OperationStatus.SubscribeNotPermitted)
