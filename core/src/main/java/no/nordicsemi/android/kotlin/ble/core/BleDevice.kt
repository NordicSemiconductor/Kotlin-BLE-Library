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

package no.nordicsemi.android.kotlin.ble.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.client.BleGatt
import no.nordicsemi.android.kotlin.ble.core.client.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.client.BluetoothGattWrapper
import no.nordicsemi.android.kotlin.ble.core.client.callback.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.core.mock.MockClientAPI
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine

sealed interface BleDevice {

    fun createConnection(
        context: Context,
        options: BleGattConnectOptions = BleGattConnectOptions()
    ) : BleGatt
}

@SuppressLint("MissingPermission")
@Parcelize
class RealBleDevice(
    private val device: BluetoothDevice,
    val scanResult: ScanResult
) : BleDevice, Parcelable {
    val name: String
    val address: String
    val isBonded: Boolean

    init {
        name = device.name ?: "NO_NAME"
        address = device.address ?: "NO_ADDRESS"
        isBonded = device.bondState == BluetoothDevice.BOND_BONDED
    }

    override fun createConnection(
        context: Context,
        options: BleGattConnectOptions
    ) : BleGatt {
        val gattCallback = BluetoothGattClientCallback()

        val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.connectGatt(context, options.autoConnect, gattCallback, BluetoothDevice.TRANSPORT_LE, options.getPhy())
        } else {
            device.connectGatt(context, options.autoConnect, gattCallback)
        }

        return BluetoothGattWrapper(gatt, gattCallback)
    }
}

class MockBleDevice : BleDevice {

    override fun createConnection(context: Context, options: BleGattConnectOptions): BleGatt {
        return MockClientAPI(MockEngine)
    }
}
