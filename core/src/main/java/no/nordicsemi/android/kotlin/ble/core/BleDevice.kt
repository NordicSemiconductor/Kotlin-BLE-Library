/*
 * Copyright (c) 2023, Nordic Semiconductor
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
import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import java.util.Locale

/**
 * Class representing BLE device. It can be either mocked or native variant.
 */
sealed interface BleDevice : Parcelable {

    /**
     * Name of a device.
     */
    val name: String?

    /**
     * MAC address of a device.
     */
    val address: String

    /**
     * Bond state. It can change over time.
     */
    val bondState: BondState

    /**
     * Returns true if a device is bonded.
     */
    val isBonded: Boolean
        get() = bondState == BondState.BONDED

    /**
     * Returns true if a device is in the middle of bonding process.
     */
    val isBonding: Boolean
        get() = bondState == BondState.BONDING

    /**
     * Returns true if a device has not empty name.
     */
    val hasName
        get() = name?.isNotEmpty() == true
}

/**
 * Class representing BLE server device. It can be either mocked or native variant.
 * It can be connected to using [ClientBleGatt`].
 */
sealed interface ServerDevice : BleDevice {

    override val name: String?
    override val address: String
}

/**
 * Class representing BLE client device. It can be either mocked or native variant.
 */
sealed interface ClientDevice : BleDevice

/**
 * Class representing real BLE client device. It is a wrapper around native [BluetoothDevice].
 */
@Suppress("InlinedApi")
@Parcelize
data class RealClientDevice(
    val device: BluetoothDevice,
) : ClientDevice, Parcelable {

    @IgnoredOnParcel
    override val name: String?
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        get() = device.name

    @IgnoredOnParcel
    override val address: String = device.address

    override val bondState: BondState
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        get() = BondState.create(device.bondState)
}

/**
 * Class representing real BLE server device. It is a wrapper around native [BluetoothDevice].
 */
@Suppress("InlinedApi")
@Parcelize
data class RealServerDevice(
    val device: BluetoothDevice,
) : ServerDevice, Parcelable {

    @IgnoredOnParcel
    override val name: String?
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        get() = device.name

    @IgnoredOnParcel
    override val address: String = device.address

    override val bondState: BondState
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        get() = BondState.create(device.bondState)
}

/**
 * Class representing mocked BLE server device. It is independent from native Android API.
 * It's good for testing or local connection.
 */
@Parcelize
data class MockClientDevice(
    override val name: String? = "CLIENT",
    override val address: String = "11:22:33:44:55:66",
    override val bondState: BondState = BondState.NONE,
) : ClientDevice, Parcelable {

    companion object {
        private var counter = 0

        fun nextDevice(): MockClientDevice {
            counter = ++counter % 100
            val lastDigit = String.format(Locale.US, "%02d", counter)
            return MockClientDevice(address = "11:22:33:44:55:$lastDigit")
        }
    }
}

/**
 * Class representing mocked BLE server device. It is independent from native Android API.
 * It's good for testing or local connection.
 */
@Parcelize
data class MockServerDevice(
    override val name: String? = "SERVER",
    override val address: String = "11:22:33:44:55:66",
    override val bondState: BondState = BondState.NONE,
) : ServerDevice, Parcelable
