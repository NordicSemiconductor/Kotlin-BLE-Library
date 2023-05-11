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
import android.os.ParcelUuid
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.BondState

const val RSSI_UNKNOWN = 0

sealed interface BleDevice : Parcelable {

    val name: String
    val address: String
    val bondState: BondState
    val isBonded: Boolean
        get() = bondState == BondState.BONDED
    val isBonding: Boolean
        get() = bondState == BondState.BONDING

    val serviceUuids: List<ParcelUuid>
    val highestRssi: Int
    val hasName
        get() = name.isNotEmpty()
}

sealed interface ServerDevice : BleDevice {

    override val name: String
    override val address: String
}

sealed interface ClientDevice : BleDevice

@SuppressLint("MissingPermission")
@Parcelize
data class RealClientDevice(
    val device: BluetoothDevice
) : ClientDevice, Parcelable {

    @IgnoredOnParcel
    override val name: String = device.name ?: ""

    @IgnoredOnParcel
    override val address: String = device.address

    override val bondState: BondState
        get() = BondState.create(device.bondState)

    @IgnoredOnParcel
    override val serviceUuids: List<ParcelUuid> = emptyList()

    @IgnoredOnParcel
    override val highestRssi: Int = RSSI_UNKNOWN

}

@SuppressLint("MissingPermission")
@Parcelize
data class RealServerDevice(
    val device: BluetoothDevice,
    override val highestRssi: Int = RSSI_UNKNOWN,
    override val serviceUuids: List<ParcelUuid> = emptyList()
) : ServerDevice, Parcelable {

    constructor(device: BluetoothDevice, scanResult: ScanResult) : this(device, scanResult.rssi, scanResult.scanRecord?.serviceUuids ?: emptyList())

    @IgnoredOnParcel
    override val name: String = device.name ?: ""

    @IgnoredOnParcel
    override val address: String = device.address

    override val bondState: BondState
        get() = BondState.create(device.bondState)
}

@Parcelize
data class MockClientDevice(
    override val name: String = "CLIENT",
    override val address: String = "11:22:33:44:55",
    override val bondState: BondState = BondState.NONE
) : ClientDevice, Parcelable {

    @IgnoredOnParcel
    override val serviceUuids: List<ParcelUuid> = emptyList()

    @IgnoredOnParcel
    override val highestRssi: Int = RSSI_UNKNOWN
}

@Parcelize
data class MockServerDevice(
    override val name: String = "SERVER",
    override val address: String = "11:22:33:44:55",
    override val bondState: BondState = BondState.NONE
) : ServerDevice, Parcelable {

    @IgnoredOnParcel
    override val serviceUuids: List<ParcelUuid> = emptyList()

    @IgnoredOnParcel
    override val highestRssi: Int = RSSI_UNKNOWN
}
