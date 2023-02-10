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

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.client.BleGatt
import no.nordicsemi.android.kotlin.ble.core.client.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.client.BleMockGatt
import no.nordicsemi.android.kotlin.ble.core.client.BluetoothGattWrapper
import no.nordicsemi.android.kotlin.ble.core.client.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.core.client.callback.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine

sealed interface BleDevice {

    val name: String
    val address: String
    val isBonded: Boolean
}

sealed interface ServerDevice : BleDevice {

    override val name: String
    override val address: String

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun connect(
        context: Context,
        options: BleGattConnectOptions = BleGattConnectOptions()
    ): BleGattClient
}

sealed interface ClientDevice : BleDevice

@SuppressLint("MissingPermission")
@Parcelize
class RealClientDevice(
    val device: BluetoothDevice
) : ClientDevice, Parcelable {

    @IgnoredOnParcel
    override val name: String = device.name

    @IgnoredOnParcel
    override val address: String = device.address

    @IgnoredOnParcel
    override val isBonded: Boolean = device.bondState == BluetoothDevice.BOND_BONDED

}

@SuppressLint("MissingPermission")
@Parcelize
class RealServerDevice(
    private val device: BluetoothDevice
) : ServerDevice, Parcelable {

    @IgnoredOnParcel
    override val name: String = device.name ?: ""

    @IgnoredOnParcel
    override val address: String = device.address

    @IgnoredOnParcel
    override val isBonded: Boolean = device.bondState == BluetoothDevice.BOND_BONDED

    override suspend fun connect(
        context: Context,
        options: BleGattConnectOptions
    ): BleGattClient {
        return BleGattClient(createConnection(context, options)).also {
            it.connect()
        }
    }

    private fun createConnection(
        context: Context,
        options: BleGattConnectOptions,
    ): BleGatt {
        val gattCallback = BluetoothGattClientCallback()

        val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.connectGatt(
                context,
                options.autoConnect,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE,
                options.getPhy()
            )
        } else {
            device.connectGatt(context, options.autoConnect, gattCallback)
        }

        return BluetoothGattWrapper(gatt, gattCallback)
    }
}

@Parcelize
class MockClientDevice(
    override val name: String = "CLIENT",
    override val address: String = "11:22:33:44:55",
    override val isBonded: Boolean = false
) : ClientDevice, Parcelable

@Parcelize
class MockServerDevice(
    override val name: String = "SERVER",
    override val address: String = "11:22:33:44:55",
    override val isBonded: Boolean = false
) : ServerDevice, Parcelable {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(
        context: Context,
        options: BleGattConnectOptions
    ): BleGattClient {
        val gatt = BleMockGatt(MockEngine, this)
        return BleGattClient(gatt)
            .also { it.connect() }
            .also { MockEngine.connectToServer(this, gatt) }
    }
}
