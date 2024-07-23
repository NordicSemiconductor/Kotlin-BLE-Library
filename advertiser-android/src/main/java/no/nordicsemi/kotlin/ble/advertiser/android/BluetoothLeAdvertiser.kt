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
import no.nordicsemi.kotlin.ble.advertiser.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.legacy.BluetoothLeAdvertiserLegacy
import no.nordicsemi.kotlin.ble.advertiser.android.internal.oreo.BluetoothLeAdvertiserOreo


/**
 * Creates an instance of [BluetoothLeAdvertiser] for Android.
 *
 * The implementation differs based on Android version.
 * Limited functionality is available prior to Android O.
 *
 * @param context An application context.
 * @return Instance of [BluetoothLeAdvertiserAndroid].
 */
@Suppress("unused")
fun BluetoothLeAdvertiser.Factory.native(context: Context): BluetoothLeAdvertiserAndroid = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> BluetoothLeAdvertiserOreo(context)
    else -> BluetoothLeAdvertiserLegacy(context)
}

internal abstract class NativeBluetoothLeAdvertiser(
    private val context: Context,
): BluetoothLeAdvertiserAndroid {

    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            return manager?.adapter
        }
    internal val bluetoothLeAdvertiser: android.bluetooth.le.BluetoothLeAdvertiser?
        get() = bluetoothAdapter?.bluetoothLeAdvertiser

    internal val validator: AdvertisingDataValidator
        get() {
            val bluetoothAdapter = bluetoothAdapter
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && bluetoothAdapter != null)
                AdvertisingDataValidator(
                    androidSdkVersion = Build.VERSION.SDK_INT,
                    isLe2MPhySupported = bluetoothAdapter.isLe2MPhySupported,
                    isLeCodedPhySupported = bluetoothAdapter.isLeCodedPhySupported,
                    // Up until Android 15 BluetoothLeAdvertiser was checking
                    // if periodic advertising is supported, not extended advertising:
                    // https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Bluetooth/framework/java/android/bluetooth/le/BluetoothLeAdvertiser.java;l=556?q=BluetoothLeAdvertiser
                    isLeExtendedAdvertisingSupported = bluetoothAdapter.isLePeriodicAdvertisingSupported,
                    leMaximumAdvertisingDataLength = bluetoothAdapter.leMaximumAdvertisingDataLength,
                    deviceName = name ?: ""
                )
            else
                AdvertisingDataValidator(
                    androidSdkVersion = Build.VERSION.SDK_INT,
                    isLe2MPhySupported = false,
                    isLeCodedPhySupported = false,
                    isLeExtendedAdvertisingSupported = false,
                    leMaximumAdvertisingDataLength = 31,
                    deviceName = name ?: ""
                )
            }

    /**
     * Checks if Bluetooth adapter is enabled.
     */
    internal fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Checks if the [Manifest.permission.BLUETOOTH_ADVERTISE] permission is granted.
     *
     * This method returns true on Android versions below S.
     */
    internal fun isPermissionGranted(permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * The local Bluetooth adapter name.
     *
     * On Android, it is this name that is advertised when
     * [AdvertisingPayload.AdvertisingData.includeDeviceName] is enabled.
     */
    @get:RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT])
    @set:RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT])
    override var name: String?
        set(value) { bluetoothAdapter?.name = value }
        get() {
            if (isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                return bluetoothAdapter?.name
            }
            return null
        }
}