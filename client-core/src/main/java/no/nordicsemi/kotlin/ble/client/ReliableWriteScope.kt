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

import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.client.exception.ValueDoesNotMatchException

/**
 * A scope of reliable write operation.
 *
 * Reliable Write procedure, also known as Queued Write, allows to write one or multiple
 * characteristics and descriptors in a single transaction. Every write request is confirmed by
 * the remote device by sending the received value back to the initiator. If the received value
 * doesn't match the sent value, the transaction is aborted automatically and
 * [ValueDoesNotMatchException] will be thrown.
 *
 * See Bluetooth Core Specification 5.4, Vol 3, Part F, 3.4.6: Queued Writes.
 */
interface ReliableWriteScope {

    /**
     * The maximum amount of data, in bytes, you can send to a characteristic in a single
     * reliable write request.
     *
     * Maximum size is equal to *ATT MTU - 5 bytes*. The ATT MTU can be negotiated during
     * connection setup.
     */
    val maximumWriteValueLength: Int

    /**
     * Writes the value of the characteristic reliably using [WriteType.WITH_RESPONSE].
     *
     * If this method is invoked while a reliable write transaction is in progress, the [data]
     * is compared with the value reported by the remote device.
     * If the reported value doesn't match the reliable write is aborted and the method throws
     * [ValueDoesNotMatchException].
     *
     * The maximum length of the data is limited by the *ATT MTU - 5 bytes*.
     *
     * @param data The data to be written reliably.
     */
    fun RemoteCharacteristic.writeReliably(data: ByteArray)

    /**
     * Writes the value of the descriptor reliably.
     *
     * If this method is invoked while a reliable write transaction is in progress, the [data]
     * is compared with the value reported by the remote device.
     * If the reported value doesn't match the reliable write is aborted and the method throws
     * [ValueDoesNotMatchException].
     *
     * The maximum length of the data is limited by the *ATT MTU - 5 bytes*.
     *
     * @param data The data to be written reliably.
     */
    fun RemoteDescriptor.writeReliably(data: ByteArray)
}