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

package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.util.UUID

/**
 * Interface representing bluetooth gatt descriptor. It's task is to hide an implementation
 * which can be both [NativeBluetoothGattDescriptor] (which is a wrapper around native Android
 * [BluetoothGattDescriptor]) or [MockBluetoothGattDescriptor]. The role of an interface
 * is to hide a detailed implementation and separate native Android [BluetoothGattDescriptor]
 * for mock variant as it can't be unit tested.
 */
interface IBluetoothGattDescriptor {

    /**
     * [UUID] of a descriptor.
     */
    val uuid: UUID

    /**
     * Not parsed permissions of a descriptor as [Int].
     */
    val permissions: Int

    /**
     * [ByteArray] value of this descriptor.
     */
    var value: DataByteArray

    /**
     * Parent characteristic of this descriptor. There is a circular dependency between
     * a characteristic and a descriptor which makes usage of an underline data classes tricky.
     */
    val characteristic: IBluetoothGattCharacteristic
}
