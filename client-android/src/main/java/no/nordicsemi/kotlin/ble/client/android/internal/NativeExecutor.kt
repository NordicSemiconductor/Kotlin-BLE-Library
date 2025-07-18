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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.PeripheralType
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption

/**
 * A native implementation of [Peripheral.Executor] for Android.
 *
 * This class uses the Android Bluetooth API to connect to the device.
 *
 * @param context The application context.
 * @param bluetoothDevice The Bluetooth device to connect to.
 * @param name The name of the device from the advertisement data.
 */
internal class NativeExecutor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice,
    name: String?
): Peripheral.Executor {
    override val identifier: String = bluetoothDevice.address
    override val type: PeripheralType = try {
        // This may throw Security Exception if Bluetooth Connect permission isn't granted.
        bluetoothDevice.type.toPeripheralType()
    } catch (e: SecurityException) {
        PeripheralType.UNKNOWN
    }
    override val name: String? = try {
        // This may throw Security Exception if Bluetooth Connect permission isn't granted.
        bluetoothDevice.name ?: name
    } catch (e: SecurityException) {
        name
    }
    override val initialState: ConnectionState = ConnectionState.Closed
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

    override val events: SharedFlow<GattEvent>
        get() = gattCallback.events

    override val isClosed: Boolean
        get() = gatt == null

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        // On retry the previous GATT object may not be null and must be closed.
        gatt?.close()
        gatt = bluetoothDevice.connect(context, autoConnect, gattCallback, preferredPhy)
    }

    override fun discoverServices(): Boolean {
        return gatt?.discoverServices() ?: false
    }

    override fun createBond(): Boolean {
        return bluetoothDevice.createBond()
    }

    override fun removeBond(): Boolean {
        try {
            val method = BluetoothDevice::class.java.getMethod("removeBond")
            return method.invoke(bluetoothDevice) as Boolean
        } catch (e: ReflectiveOperationException) {
            return false
        }
    }

    override fun refreshCache(): Boolean {
        gatt?.let { gatt ->
            val result = try {
                val method = BluetoothGatt::class.java.getMethod("refresh")
                method.invoke(gatt) as Boolean
            } catch (e: ReflectiveOperationException) {
                false
            }
            if (!result) {
                return false
            }

            // There is no callback for services invalidated.
            gattCallback.onServiceChanged(gatt)
            return true
        }
        return false
    }

    override fun requestConnectionPriority(priority: ConnectionPriority): Boolean {
        gatt?.let { gatt ->
            val result = gatt.requestConnectionPriority(priority.toPriority())
            if (!result) {
                return false
            }

            // Prior to Android Oreo there is no callback for connection parameters change.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                gattCallback.onConnectionUpdated()
            }
            return true
        }
        return false
    }

    override fun requestMtu(mtu: Int): Boolean {
        return gatt?.requestMtu(mtu) ?: false
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption): Boolean {
        gatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.setPreferredPhy(txPhy.toPhy(), rxPhy.toPhy(), phyOptions.toOption())
            } else {
                gattCallback.onPhyUpdate(gatt,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    BluetoothGatt.GATT_SUCCESS)
            }
            return true
        }
        return false
    }

    override fun readPhy(): Boolean {
        gatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.readPhy()
            } else {
                gattCallback.onPhyRead(gatt,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    1 /* BluetoothDevice.PHY_LE_1M */,
                    BluetoothGatt.GATT_SUCCESS)
            }
            return true
        }
        return false
    }

    override fun readRssi(): Boolean {
        return gatt?.readRemoteRssi() ?: false
    }

    override fun disconnect(): Boolean {
        gatt?.let { gatt ->
            gattCallback.disconnectRequest = true
            gatt.disconnect()
            return true
        }
        return false
    }

    override fun close() {
        gatt?.let { gatt ->
            this.gatt = null
            gattCallback.disconnectRequest = false
            try {
                gatt.disconnect()
            } catch (_: Exception) {
                // Ignore
            }
            try {
                gatt.close()
            } catch (_: Exception) {
                // Ignore
            }
        }
    }
}