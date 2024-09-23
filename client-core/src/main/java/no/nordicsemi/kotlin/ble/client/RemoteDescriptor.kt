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

import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.core.Descriptor
import no.nordicsemi.kotlin.ble.core.exception.BluetoothException

/**
 * A GATT descriptor of a characteristic on a remote connected peripheral device.
 *
 * The API allows to access the value of the descriptor.
 * Depending on the descriptor, it may be possible to read or write its value.
 */
interface RemoteDescriptor: Descriptor {
    override val characteristic: RemoteCharacteristic
    override val owner: Peripheral<*, *>?
        get() = characteristic.owner

    /**
     * Reads the value of the descriptor.
     *
     * @return The value of the descriptor.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the descriptor has been invalidated due to
     * disconnection of service change event.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     */
    suspend fun read(): ByteArray

    /**
     * Writes the value of the descriptor.
     *
     * @param data The data to be written.
     * @throws OperationFailedException if the operation failed.
     * @throws InvalidAttributeException if the descriptor has been invalidated due to
     * disconnection of service change event.
     * @throws BluetoothException if the implementation fails, see cause for a reason.
     */
    suspend fun write(data: ByteArray)
}