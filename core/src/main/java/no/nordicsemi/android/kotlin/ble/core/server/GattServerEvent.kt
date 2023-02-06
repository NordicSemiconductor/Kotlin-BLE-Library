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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

sealed interface GattServerEvent

data class OnServiceAdded(
    val service: BluetoothGattService,
    val status: BleGattOperationStatus
) : GattServerEvent

data class OnConnectionStateChanged(
    val device: ClientDevice,
    val status: BleGattOperationStatus,
    val newState: Int
) : GattServerEvent

data class OnPhyRead(
    val device: ClientDevice,
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattServerEvent

data class OnPhyUpdate(
    val device: ClientDevice,
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy,
    val status: BleGattOperationStatus
) : GattServerEvent

sealed interface ServiceEvent : GattServerEvent

sealed interface CharacteristicEvent : ServiceEvent

data class OnMtuChanged(
    val device: ClientDevice,
    val mtu: Int
) : CharacteristicEvent, DescriptorEvent

data class OnExecuteWrite(
    val device: ClientDevice,
    val requestId: Int,
    val execute: Boolean
) : CharacteristicEvent, DescriptorEvent

data class OnCharacteristicReadRequest(
    val device: ClientDevice,
    val requestId: Int,
    val offset: Int,
    val characteristic: BluetoothGattCharacteristic
) : CharacteristicEvent

data class OnCharacteristicWriteRequest(
    val device: ClientDevice,
    val requestId: Int,
    val characteristic: BluetoothGattCharacteristic,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray
) : CharacteristicEvent

data class OnNotificationSent(
    val device: ClientDevice,
    val status: BleGattOperationStatus
) : CharacteristicEvent

sealed interface DescriptorEvent : ServiceEvent

data class OnDescriptorReadRequest(
    val device: ClientDevice,
    val requestId: Int,
    val offset: Int,
    val descriptor: BluetoothGattDescriptor
) : DescriptorEvent

data class OnDescriptorWriteRequest(
    val device: ClientDevice,
    val requestId: Int,
    val descriptor: BluetoothGattDescriptor,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray
) : DescriptorEvent
