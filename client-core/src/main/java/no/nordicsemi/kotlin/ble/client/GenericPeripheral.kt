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

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import no.nordicsemi.kotlin.ble.client.exception.PeripheralNotConnectedException
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Peer
import no.nordicsemi.kotlin.ble.core.WriteType
import java.util.UUID

/**
 * Class representing a Bluetooth LE peripheral.
 *
 * @property name The friendly Bluetooth name of the remote device.
 * The local adapter will automatically retrieve remote names when performing a device scan,
 * and will cache them. This method just returns the name for this device from the cache
 * @property state The connection state of the peripheral, as [StateFlow]. The flow emits a new
 * value whenever the state of the peripheral changes.
 */
interface GenericPeripheral<ID>: Peer<ID> {
    val name: String?
    val state: StateFlow<ConnectionState>

    /**
     * Returns `true` if the peripheral is currently connected.
     */
    val isConnected: Boolean
        get() = state.value is ConnectionState.Connected

    /**
     * Returns `true` if the peripheral is disconnected of getting disconnected.
     */
    val isDisconnected: Boolean
        get() = state.value is ConnectionState.Disconnected || state.value is ConnectionState.Disconnecting

    /**
     * Returns a flow with a list of services discovered on the device.
     *
     * Initially, the flow will emit an empty list. The list will be updated when the services
     * are discovered. The flow will be closed when the device disconnects.
     *
     * @param uuids An optional list of service UUID to filter the results. If empty, all services
     *        will be returned. Some platforms may do partial service discovery and return only
     *        services with given UUIDs.
     */
    fun services(uuids: List<UUID> = emptyList()): StateFlow<List<RemoteService>>

    /**
     * The maximum amount of data, in bytes, you can send to a characteristic in a single write
     * request.
     *
     * Maximum size for [WriteType.WITH_RESPONSE] type is *512 bytes* or to *ATT MTU - 5 bytes*
     * when writing reliably (see [ReliableWriteScope]).
     * For [WriteType.WITHOUT_RESPONSE] it is equal to *ATT MTU - 3 bytes*.
     *
     * The ATT MTU value can be negotiated during the connection setup.
     *
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     */
    fun maximumWriteValueLength(type: WriteType): Int

    /**
     * Reads the received signal strength indicator (RSSI) of the peripheral.
     *
     * Usually, the RSSI value is between -120 dBm (vary far) and -30 dBm (very close),
     * but the exact value depends on the TX power, antenna, environment, and other factors.
     *
     * @return The RSSI value in dBm.
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     */
    suspend fun readRssi(): Int

    /**
     * Disconnects the client from the peripheral.
     *
     * Note, that calling this method does not guarantee that the peripheral will disconnect;
     * other clients, also in other applications, may still be connected to the peripheral.
     *
     * This method does nothing if the peripheral is already disconnected.
     *
     * Hint: Use [GenericCentralManager.connect] to connect to the peripheral.
     */
    suspend fun disconnect()
}