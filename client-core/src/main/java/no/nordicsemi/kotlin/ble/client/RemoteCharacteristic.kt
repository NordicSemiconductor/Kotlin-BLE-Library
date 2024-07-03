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

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.client.exception.PeripheralNotConnectedException
import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.core.exception.BluetoothException
import no.nordicsemi.kotlin.ble.core.Characteristic
import no.nordicsemi.kotlin.ble.core.WriteType

/**
 * A GATT characteristic of a service on a remote connected peripheral device.
 *
 * The API allows to access the value of the characteristic.
 * Depending on the properties of the characteristic, it may be possible to read, write,
 * subscribe for value changes, etc.
 */
interface RemoteCharacteristic: Characteristic<RemoteDescriptor> {
    override val service: AnyRemoteService
    override val owner: GenericPeripheral<*, *>?
        get() = service.owner

    /**
     * Returns whether the characteristic is notifying or indicating.
     */
    val isNotifying: Boolean

    /**
     * Sets notifications or indications state, depending on the characteristic's properties.
     *
     * Note, that calling [suspend] or [waitForValueChange] will enable notifications
     * automatically.
     *
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the characteristic has been invalidated.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     */
    suspend fun setNotifying(enabled: Boolean)

    /**
     * Reads the value of the characteristic.
     *
     * @return The value of the characteristic.
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the characteristic has been invalidated.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     */
    suspend fun read(): ByteArray

    /**
     * Writes the value of the characteristic.
     *
     * @param data The data to be written.
     * @param writeType The write type to be used.
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the characteristic has been invalidated.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     */
    suspend fun write(data: ByteArray, writeType: WriteType)

    /**
     * Subscribes for notifications or indications of the characteristic.
     *
     * This method suspends until the notifications are enabled.
     *
     * The client will NOT unsubscribe when the flow is closed. Use [isNotifying] to disable
     * or check the notifications state.
     *
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the characteristic has been invalidated.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     * @see isNotifying
     * @see setNotifying
     */
    suspend fun subscribe(): Flow<ByteArray>

    /**
     * Waits for the value of the characteristic to change.
     *
     * This method suspends until the value of the characteristic changes. If the notifications
     * are not enabled, it will enable them.
     *
     * @return The new value of the characteristic.
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the characteristic has been invalidated.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     * @see subscribe
     * @see isNotifying
     * @see setNotifying
     */
    suspend fun waitForValueChange(): ByteArray
}