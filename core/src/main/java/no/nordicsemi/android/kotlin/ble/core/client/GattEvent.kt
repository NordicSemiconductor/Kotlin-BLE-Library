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

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

sealed interface GattEvent

class OnServicesDiscovered(val gatt: BluetoothGatt, val status: BleGattOperationStatus) : GattEvent

class OnConnectionStateChanged(
    val status: BleGattOperationStatus,
    val newState: Int
) : GattEvent

class OnMtuChanged(val mtu: Int, val status: BleGattOperationStatus) : GattEvent

class OnPhyRead(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattEvent

class OnPhyUpdate(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattEvent

class OnReadRemoteRssi(val rssi: Int, val status: BleGattOperationStatus) : GattEvent
object OnServiceChanged : GattEvent

sealed interface CharacteristicEvent : GattEvent

class OnCharacteristicChanged(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray
) : CharacteristicEvent

class OnCharacteristicRead(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnCharacteristicWrite(
    val characteristic: BluetoothGattCharacteristic,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnDescriptorRead(
    val descriptor: BluetoothGattDescriptor,
    val value: ByteArray,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnDescriptorWrite(
    val descriptor: BluetoothGattDescriptor,
    val status: BleGattOperationStatus
) : CharacteristicEvent

class OnReliableWriteCompleted(val status: BleGattOperationStatus) : CharacteristicEvent
