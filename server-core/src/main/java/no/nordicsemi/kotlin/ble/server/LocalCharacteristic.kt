/*
 * Copyright (c) 2024, Nordic Semiconductor
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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.server

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.ble.core.Characteristic
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty

/**
 * A local characteristic is a characteristic that is hosted on the local GATT server and can be
 * accessed by the remote [Central] device.
 *
 * @property subscribedCentrals A flow of subscription events for the characteristic. Events will
 * be emitted for characteristics that have the [NOTIFY][CharacteristicProperty.NOTIFY]
 * or [INDICATE][CharacteristicProperty.INDICATE] property set.
 */
interface LocalCharacteristic: Characteristic<LocalDescriptor> {
    val subscribedCentrals: Flow<SubscriptionEvent>

    /**
     * Updates the value of the characteristic and notifies the subscribed [Central] device of
     * a value change.
     *
     * @param data The data to be written.
     */
    suspend fun updateValue(data: ByteArray)

    /**
     * Sets a value provider for the characteristic.
     *
     * Every time a read request is received, the provider will be called to get the current value
     * of the characteristic to be sent back to the [Central] device.
     *
     * If the length of the value is longer than the maximum update value length, the provider will
     * automatically use Long Read Procedure. Maximum data length is 512 bytes.
     *
     * @param onRead The callback that will be called to get the current value of the
     *        characteristic.
     * @see [waitForReadRequest] to wait for a single read request.
     */
    fun provideValue(onRead: () -> ByteArray)

    /**
     * Subscribes for write requests for the characteristic from the [Central] device.
     *
     * As this is a "local" characteristic, the [Central] device may update its value by writing
     * the value. The returned cold flow will emit the written values.
     *
     * Multiple subscribers may subscribe to the same characteristic. // TODO check if this is true
     *
     * @return The flow of packets written to the characteristic by the [Central] device.
     */
    fun subscribe(): Flow<ByteArray>

    /**
     * Waits for the [Central] device to read the value of the characteristic.
     *
     * This method has higher priority then [provideValue], which means that even if the value
     * provider is set, this method will be used to provide the value. // TODO check if this is true
     *
     * If there are multiple objects awaiting a read on a single characteristic instance,
     * they will be queued. // TODO check if this is true
     *
     * If the length of the value is longer than the maximum update value length, the provider will
     * automatically use Long Read Procedure. Maximum data length is 512 bytes.
     *
     * @param response A callback called when the read request has been received. It should return
     *        the value to be sent back to the requesting device.
     */
    suspend fun waitForReadRequest(response: () -> ByteArray)

    /**
     * Waits for the [Central] device to write the value of the characteristic and returns the
     * written value.
     *
     * If there are multiple objects awaiting a write on a single characteristic instance,
     * they will be queued. // TODO check if this is true
     *
     * @return The value sent to the characteristic.
     */
    suspend fun waitForWriteRequest(): ByteArray
}

/**
 * A characteristic subscription event.
 *
 * Events may only be emitted for characteristics that have the [CharacteristicProperty.NOTIFY]
 * or [CharacteristicProperty.INDICATE] property set.
 */
sealed interface SubscriptionEvent

/**
 * A subscription event that indicates that the [Central] device has subscribed to the
 * characteristic value updates.
 *
 * @param central The [Central] device that has subscribed.
 */
class CentralSubscribed(val central: Central<*>): SubscriptionEvent

/**
 * A subscription event that indicates that the [Central] device has unsubscribed from the
 * characteristic value updates.
 *
 * @param central The [Central] device that has unsubscribed.
 */
class CentralUnsubscribed(val central: Central<*>): SubscriptionEvent
