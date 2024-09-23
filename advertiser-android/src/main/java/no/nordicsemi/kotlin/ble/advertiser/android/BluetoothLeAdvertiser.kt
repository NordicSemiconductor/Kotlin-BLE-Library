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

package no.nordicsemi.kotlin.ble.advertiser.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import no.nordicsemi.kotlin.ble.advertiser.android.internal.AdvertisingParametersValidator
import no.nordicsemi.kotlin.ble.advertiser.android.internal.legacy.BluetoothLeAdvertiserLegacy
import no.nordicsemi.kotlin.ble.advertiser.android.internal.oreo.BluetoothLeAdvertiserOreo

/**
 * Creates an instance of [BluetoothLeAdvertiser] for Android.
 *
 * The implementation differs based on Android version.
 * Limited functionality is available prior to Android O.
 *
 * @param context An application context.
 * @return Instance of [BluetoothLeAdvertiser].
 */
@Suppress("unused")
fun BluetoothLeAdvertiser.Factory.native(context: Context): BluetoothLeAdvertiser = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> BluetoothLeAdvertiserOreo(context)
    else -> BluetoothLeAdvertiserLegacy(context)
}

internal abstract class NativeBluetoothLeAdvertiser(
    private val context: Context,
): BluetoothLeAdvertiser() {

    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            return manager?.adapter
        }

    protected val bluetoothLeAdvertiser: android.bluetooth.le.BluetoothLeAdvertiser?
        get() = bluetoothAdapter?.bluetoothLeAdvertiser

    @get:RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT])
    @set:RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT])
    override var name: String?
        set(value) {
            require(value != null)
            bluetoothAdapter?.name = value
        }
        get() {
            checkConnectPermission()
            return bluetoothAdapter?.name
        }

    override fun getMaximumAdvertisingDataLength(legacy: Boolean): Int =
        if (!legacy && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.leMaximumAdvertisingDataLength ?: 31
        } else {
            31
        }

    override val isLeExtendedAdvertisingSupported: Boolean
        get() = when {
            Build.VERSION.SDK_INT >= 35 /* Vanilla Ice Cream */ ->
                bluetoothAdapter?.isLeExtendedAdvertisingSupported == true

            // When checking support for max advertising events up until Android 15
            // the BluetoothLeAdvertiser was checking if periodic advertising is supported,
            // not extended advertising:
            // https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Bluetooth/framework/java/android/bluetooth/le/BluetoothLeAdvertiser.java;l=556?q=BluetoothLeAdvertiser
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                bluetoothAdapter?.isLePeriodicAdvertisingSupported == true &&
                bluetoothAdapter?.isLeExtendedAdvertisingSupported == true

            // Before Android Oreo Extended Advertising wasn't supported.
            else -> false
        }

    override val validator: AdvertisingDataValidator
        get() {
            val bluetoothAdapter = bluetoothAdapter
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && bluetoothAdapter != null)
                AdvertisingDataValidator(
                    deviceName = nameOrNull ?: "",
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
                    deviceName = nameOrNull ?: "",
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

    override fun isBluetoothEnabled() = bluetoothAdapter?.isEnabled == true

    override fun checkConnectPermission() {
        check(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("BLUETOOTH_CONNECT permission not granted")
        }
    }

    override fun checkAdvertisePermission() {
        check(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("BLUETOOTH_ADVERTISE permission not granted")
        }
    }
}