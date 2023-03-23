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

package no.nordicsemi.android.kotlin.ble.core.client

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState

sealed interface GattEvent

class OnServicesDiscovered(val services: List<BluetoothGattService>, val status: BleGattOperationStatus) : GattEvent

data class OnConnectionStateChanged(
    val status: BleGattConnectionStatus,
    val newState: GattConnectionState
) : GattEvent

data class OnMtuChanged(val mtu: Int, val status: BleGattOperationStatus) : GattEvent

data class OnPhyRead(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattEvent

data class OnPhyUpdate(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattEvent

data class OnReadRemoteRssi(val rssi: Int, val status: BleGattOperationStatus) : GattEvent
object OnServiceChanged : GattEvent

sealed interface DataChangedEvent : GattEvent

sealed interface CharacteristicEvent : DataChangedEvent {
    val characteristic: BluetoothGattCharacteristic
}

sealed interface DescriptorEvent : DataChangedEvent {
    val descriptor: BluetoothGattDescriptor
}

class OnCharacteristicChanged(
    override val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray
) : CharacteristicEvent

class OnCharacteristicRead(
    override val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnCharacteristicWrite(
    override val characteristic: BluetoothGattCharacteristic,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnDescriptorRead(
    override val descriptor: BluetoothGattDescriptor,
    val value: ByteArray,
    val status: BleGattOperationStatus
) : DescriptorEvent

class OnDescriptorWrite(
    override val descriptor: BluetoothGattDescriptor,
    val status: BleGattOperationStatus
) : DescriptorEvent

class OnReliableWriteCompleted(val status: BleGattOperationStatus) : DataChangedEvent
