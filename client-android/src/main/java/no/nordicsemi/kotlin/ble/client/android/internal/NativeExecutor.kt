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

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption

/**
 * A native implementation of [Peripheral.Executor] for Android.
 *
 * This class uses the Android Bluetooth API to connect to the device.
 *
 * @param context the application context.
 * @param bluetoothDevice the Bluetooth device to connect to.
 * @param name the name of the device, defaults to [BluetoothDevice.getName].
 */
internal class NativeExecutor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice,
    override val name: String? = try {
        // This may throw Security Exception if Bluetooth Connect permission isn't granted.
        bluetoothDevice.name
    } catch (e: SecurityException) {
        null
    },
): Peripheral.Executor {
    override val identifier: String = bluetoothDevice.address
    override val type: PeripheralType = try {
        // This may throw Security Exception if Bluetooth Connect permission isn't granted.
        bluetoothDevice.type.toPeripheralType()
    } catch (e: SecurityException) {
        PeripheralType.UNKNOWN
    }
    override val initialState: ConnectionState = ConnectionState.Disconnected()
    override val initialServices: List<RemoteService> = emptyList()

    /**
     * The [BluetoothGatt] object used to communicate with the physical device.
     *
     * This is set to `null` when the connection is closed. If the peripheral was connected
     * using [auto connect][CentralManager.ConnectionOptions.AutoConnect], the [gatt] object
     * is set until [disconnect] is called.
     */
    private var gatt: BluetoothGatt? = null

    /**
     * The [NativeGattCallback] receives callbacks from the [BluetoothGatt] and emits them
     * as [GattEvent] to [events].
     */
    private val gattCallback: NativeGattCallback = NativeGattCallback()

    /** The current bond state. */
    private var _bondState = MutableStateFlow(bluetoothDevice.bondState.toBondState())
    override val bondState = _bondState.asStateFlow()

    /**
     * This method is called when the bond state of the device changes.
     */
    internal fun onBondStateChanged(state: BondState) {
        _bondState.value = state
    }

    // Implementation

    override val events: Flow<GattEvent>
        get() = gattCallback.events

    override val isClosed: Boolean
        get() = gatt == null

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        gatt?.close()
        gatt = bluetoothDevice.connect(context, autoConnect, gattCallback, preferredPhy)
    }

    override fun discoverServices() {
        gatt?.discoverServices()
    }

    override fun refreshCache() {
        gatt?.let { gatt ->
            try {
                val method = gatt.javaClass.getMethod("refresh")
                method.invoke(gatt)
                gattCallback.onServiceChanged(gatt)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override fun requestConnectionPriority(priority: ConnectionPriority) {
        gatt?.let { gatt ->
            gatt.requestConnectionPriority(priority.toPriority())

            // Prior to Android Oreo there is no callback for connection parameters change.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                gattCallback.onConnectionUpdated()
            }
        }
    }

    override fun requestMtu(mtu: Int) {
        gatt?.requestMtu(mtu)
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption) {
        gatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.setPreferredPhy(txPhy.toPhy(), rxPhy.toPhy(), phyOptions.toOption())
            } else {
                gattCallback.onPhyUpdate(gatt,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    BluetoothGatt.GATT_SUCCESS)
            }
        }
    }

    override fun readPhy() {
        gatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.readPhy()
            } else {
                gattCallback.onPhyRead(gatt,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    BluetoothGatt.GATT_SUCCESS)
            }
        }
    }

    override fun readRssi() {
        gatt?.readRemoteRssi()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun close() {
        try {
            gatt?.disconnect()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            gatt?.close()
        } catch (e: Exception) {
            // Ignore
        }
        gatt = null
    }
}