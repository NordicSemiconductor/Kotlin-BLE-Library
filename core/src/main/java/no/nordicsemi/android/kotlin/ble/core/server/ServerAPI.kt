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

package no.nordicsemi.android.kotlin.ble.core.server

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.RealClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.core.server.callback.BleGattServerCallback
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BluetoothGattServiceFactory

interface ServerAPI {

    val event: SharedFlow<GattServerEvent>

    fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    )

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    )

    fun close()

    fun connect(device: ClientDevice, autoConnect: Boolean)

    fun readPhy(device: ClientDevice)

    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption)
}

internal class MockServerAPI(
    private val mockEngine: MockEngine
) : ServerAPI {

    private val _event = simpleSharedFlow<GattServerEvent>()
    override val event: SharedFlow<GattServerEvent> = _event.asSharedFlow()

    fun onEvent(event: GattServerEvent) {
        _event.tryEmit(event)
    }

    companion object {
        fun create(vararg config: BleServerGattServiceConfig): ServerAPI {
            return MockServerAPI(MockEngine)
        }
    }

    override fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
        mockEngine.sendResponse(device, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        mockEngine.notifyCharacteristicChanged(device, characteristic, confirm, value)
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun connect(device: ClientDevice, autoConnect: Boolean) {
        mockEngine.connect(device, autoConnect)
    }

    override fun readPhy(device: ClientDevice) {
        mockEngine.readPhy(device)
    }

    override fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        mockEngine.requestPhy(device, txPhy, rxPhy, phyOption)
    }
}

@SuppressLint("MissingPermission")
internal class NativeServerAPI(
    private val server: BluetoothGattServer,
    private val callback: BleGattServerCallback
) : ServerAPI {

    override val event: SharedFlow<GattServerEvent> = callback.event

    companion object {
        fun create(context: Context, vararg config: BleServerGattServiceConfig): ServerAPI {
            val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            val callback = BleGattServerCallback()
            val bluetoothGattServer = bluetoothManager.openGattServer(context, callback)
            val server = NativeServerAPI(bluetoothGattServer, callback)

            config.forEach {
                bluetoothGattServer.addService(BluetoothGattServiceFactory.create(it))
            }

            return server
        }
    }

    override fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    ) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.sendResponse(bleDevice, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm, value)
        } else {
            characteristic.value = value
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm)
        }
    }

    override fun close() {
        server.close() //TODO
    }

    override fun connect(device: ClientDevice, autoConnect: Boolean) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.connect(bleDevice, autoConnect)
    }

    override fun readPhy(device: ClientDevice) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.readPhy(bleDevice)
        } else {
            callback.onEvent(
                OnPhyRead(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS)
            )
        }
    }

    override fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        requestPhy(device, txPhy, rxPhy, phyOption)
    }

    private fun requestPhy(device: RealClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.setPreferredPhy(device.device, txPhy.value, rxPhy.value, phyOption.value)
        } else {
            callback.onEvent(
                OnPhyUpdate(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS)
            )
        }
    }
}
