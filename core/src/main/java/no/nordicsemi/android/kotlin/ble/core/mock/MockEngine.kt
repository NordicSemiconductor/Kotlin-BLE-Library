/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.core.mock

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.client.BleClient
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent

internal object MockEngine {
//    private val registeredServers = mutableMapOf<BleDevice, BleServer>()
//    private val registeredClients = mutableMapOf<BleServer, BleClient>()
//
//    private fun registerServer(device: BleDevice, server: BleServer) {
//        registeredServers[device] = server
//    }



    private fun connectClient(client: BleClient, device: BleDevice) {
//        val server = registeredServers[device]
    }

    val event: SharedFlow<GattServerEvent>
        get() = TODO("Not yet implemented")

    fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
//        val client = registeredClients[registeredServers[device]]


    }

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        TODO("Not yet implemented")
    }

    fun close() {
        TODO("Not yet implemented")
    }

    fun connect(device: ClientDevice, autoConnect: Boolean) {
        TODO("Not yet implemented")
    }

    fun readPhy(device: ClientDevice) {
        TODO("Not yet implemented")
    }

    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        TODO("Not yet implemented")
    }
}
