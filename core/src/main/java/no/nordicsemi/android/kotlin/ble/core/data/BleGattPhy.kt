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

package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * The secondary Physical Layer on which this advertisement was received.
 *
 * @property value Native Android API value.
 * @see [ScanResult.getsecondaryphy](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult?hl=en#getsecondaryphy)
 */
enum class BleGattPhy(val value: Int) {

    /**
     * Bluetooth LE 1M PHY. Used to refer to LE 1M Physical Channel for advertising, scanning or connection.
     */
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),

    /**
     * Bluetooth LE 2M PHY. Used to refer to LE 2M Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_2M(BluetoothDevice.PHY_LE_2M),

    /**
     * Bluetooth LE Coded PHY. Used to refer to LE Coded Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    companion object {
        fun create(value: Int): BleGattPhy {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleGattPhy for value: $value")
        }
    }
}
