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

package no.nordicsemi.kotlin.ble.advertiser.android.internal

import android.bluetooth.BluetoothAdapter
import android.os.Build
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingDataValidator
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment

internal abstract class NativeBluetoothLeAdvertiser(
    private val environment: NativeAndroidEnvironment,
): BluetoothLeAdvertiser(environment) {

    private val bluetoothAdapter: BluetoothAdapter?
        get() = environment.bluetoothManager?.adapter

    protected val bluetoothLeAdvertiser: android.bluetooth.le.BluetoothLeAdvertiser?
        get() = bluetoothAdapter?.bluetoothLeAdvertiser

    override val validator: AdvertisingDataValidator
        get() {
            val bluetoothAdapter = bluetoothAdapter
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && bluetoothAdapter != null)
                AdvertisingDataValidator(
                    deviceName = environment.deviceNameOrNull ?: "",
                    isLe2MPhySupported = bluetoothAdapter.isLe2MPhySupported,
                    isLeCodedPhySupported = bluetoothAdapter.isLeCodedPhySupported,
                    // Up until Android 15 BluetoothLeAdvertiser was checking
                    // if periodic advertising is supported, not extended advertising:
                    // https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Bluetooth/framework/java/android/bluetooth/le/BluetoothLeAdvertiser.java;l=556?q=BluetoothLeAdvertiser
                    isLeExtendedAdvertisingSupported = bluetoothAdapter.isLePeriodicAdvertisingSupported,
                    leMaximumAdvertisingDataLength = bluetoothAdapter.leMaximumAdvertisingDataLength,
                )
            else
                AdvertisingDataValidator(
                    deviceName = environment.deviceNameOrNull ?: "",
                    isLe2MPhySupported = false,
                    isLeCodedPhySupported = false,
                    isLeExtendedAdvertisingSupported = false,
                    leMaximumAdvertisingDataLength = 31,
                )
            }

    override val timeoutValidator: AdvertisingParametersValidator
        get() = AdvertisingParametersValidator(
            androidSdkVersion = Build.VERSION.SDK_INT,
        )
}