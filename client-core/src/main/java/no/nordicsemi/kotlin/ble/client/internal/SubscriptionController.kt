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

package no.nordicsemi.kotlin.ble.client.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.ble.client.SubscriptionMode
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.exception.BluetoothException

/** Coordinates local registration and the remote CCCD as one serialized transition. */
internal class SubscriptionController {
    private val mutex = Mutex()
    private var activeMode: SubscriptionMode? = null

    @Volatile
    var isEnabled: Boolean = false
        private set

    suspend fun setNotifying(
        enabled: Boolean,
        requestedMode: SubscriptionMode,
        properties: Set<CharacteristicProperty>,
        setLocalRegistration: (Boolean) -> Unit,
        writeCccd: suspend (ByteArray) -> Unit,
    ) = mutex.withLock {
        val resolvedMode = if (enabled) resolveSubscriptionMode(requestedMode, properties) else null

        if (enabled == isEnabled && (!enabled || resolvedMode == activeMode)) return@withLock

        val localRegistrationChanged = enabled != isEnabled
        if (localRegistrationChanged) {
            try {
                setLocalRegistration(enabled)
            } catch (e: OperationFailedException) {
                throw e
            } catch (e: Exception) {
                throw BluetoothException(e)
            }
        }

        var descriptorWritten = false
        try {
            writeCccd(cccdValue(enabled, resolvedMode))
            descriptorWritten = true
        } finally {
            if (!descriptorWritten && localRegistrationChanged) {
                try {
                    setLocalRegistration(!enabled)
                } catch (_: Exception) {
                    // Preserve the descriptor write failure.
                }
            }
        }

        isEnabled = enabled
        activeMode = resolvedMode
    }
}

internal fun resolveSubscriptionMode(
    requestedMode: SubscriptionMode,
    properties: Set<CharacteristicProperty>,
): SubscriptionMode = when (requestedMode) {
    SubscriptionMode.AUTOMATIC -> when {
        CharacteristicProperty.INDICATE in properties -> SubscriptionMode.INDICATION
        CharacteristicProperty.NOTIFY in properties -> SubscriptionMode.NOTIFICATION
        else -> unsupportedSubscriptionMode()
    }

    SubscriptionMode.NOTIFICATION ->
        if (CharacteristicProperty.NOTIFY in properties) requestedMode else unsupportedSubscriptionMode()

    SubscriptionMode.INDICATION ->
        if (CharacteristicProperty.INDICATE in properties) requestedMode else unsupportedSubscriptionMode()
}

internal fun cccdValue(
    enabled: Boolean,
    resolvedMode: SubscriptionMode?,
): ByteArray = when {
    !enabled -> BaseRemoteDescriptor.DISABLE_NOTIFICATIONS_VALUE
    resolvedMode == SubscriptionMode.NOTIFICATION -> BaseRemoteDescriptor.ENABLE_NOTIFICATIONS_VALUE
    resolvedMode == SubscriptionMode.INDICATION -> BaseRemoteDescriptor.ENABLE_INDICATIONS_VALUE
    else -> unsupportedSubscriptionMode()
}

private fun unsupportedSubscriptionMode(): Nothing =
    throw OperationFailedException(OperationStatus.SubscribeNotPermitted)
