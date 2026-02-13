/*
 * Copyright (c) 2025, Nordic Semiconductor
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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.core.android

import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.core.Environment
import no.nordicsemi.kotlin.ble.core.Manager
import org.jetbrains.annotations.Range

/**
 * A mock environment that can be used to test the behavior of the Central Manager.
 *
 * @property bluetoothState A flow emitting the current Bluetooth state.
 * @property locationState A flow emitting the state of the location service, *true* when enabled,
 * *false* otherwise.
 * Location service is required to scan for Bluetooth LE beacons when
 * [isLocationRequiredForScanning] is *true*.
 * @property isLocationRequiredForScanning Whether location is required to scan for Bluetooth LE devices.
 * @property isLocationPermissionGranted Whether the `ACCESS_FINE_LOCATION` permission is granted.
 * @property isLocationEnabled Whether location service is enabled on the device.
 * @property androidSdkVersion The Android SDK version.
 * @property deviceName The name of the device, used for Bluetooth LE advertising if local name is set.
 * Reading and setting the name requires `BLUETOOTH_CONNECT` permission.
 * @property isLe2MPhySupported Whether LE 2M PHY is supported on the device.
 * @property isLeCodedPhySupported Whether LE Coded PHY is supported on the device.
 * @property isBluetoothScanPermissionGranted Whether the `BLUETOOTH_SCAN` permission is granted.
 * @property isBluetoothConnectPermissionGranted Whether the `BLUETOOTH_CONNECT` permission is granted.
 * @property isBluetoothAdvertisePermissionGranted Whether the `BLUETOOTH_ADVERTISE` permission is granted.
 * @property isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
 * @property isLeExtendedAdvertisingSupported Whether LE Extended Advertising feature is supported.
 * @property isLePeriodicAdvertisingSupported Whether LE Periodic Advertising feature is supported.
 * @property leMaximumAdvertisingDataLength The maximum LE advertising data length in bytes,
 * if LE Extended Advertising feature is supported.
 */
interface AndroidEnvironment : Environment {
    /**
     * Android permissions.
     *
     * The constants are useful to request permissions on devices running older Android version,
     * i.e. using mock implementation.
     */
    object Permission {
        /** Bluetooth Connect permission. */
        const val BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"
        /** Bluetooth Scan permission. */
        const val BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
        /** Bluetooth Advertise permission. */
        const val BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE"
    }

    /**
     * Android SDK versions.
     */
    object SdkVersion {
        /** Android 5.0 */
        const val LOLLIPOP = 21
        /** Android 6.0 */
        const val MARSHMALLOW = 23
        /** Android 8.0 */
        const val OREO = 26
        /** Android 12 */
        const val S = 31
        /** Android 15 */
        const val VANILLA_ICE_CREAM = 35
    }

    val bluetoothState: StateFlow<Manager.State>
    override val isBluetoothEnabled: Boolean
        get() = bluetoothState.value == Manager.State.POWERED_ON

    /**
     * Sends an intent to enable Bluetooth adapter on the device.
     */
    fun enableBluetooth()

    val locationState: StateFlow<Boolean>
    val isLocationEnabled: Boolean
        get() = locationState.value

    val androidSdkVersion: Int
    val isLocationRequiredForScanning: Boolean
    val isLocationPermissionGranted: Boolean
    val isLe2MPhySupported: Boolean
    val isLeCodedPhySupported: Boolean
    val isBluetoothScanPermissionGranted: Boolean
    val isBluetoothConnectPermissionGranted: Boolean
    val isBluetoothAdvertisePermissionGranted: Boolean
    val isMultipleAdvertisementSupported: Boolean
    val isLeExtendedAdvertisingSupported: Boolean
    val isLePeriodicAdvertisingSupported: Boolean
    val leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int

    /**
     * The local Bluetooth adapter name, or *null* if the required permission is not granted.
     */
    val deviceNameOrNull: String?
        get() = try { deviceName } catch (_: Exception) { null }

    /**
     * Whether the device requires runtime permissions to use Bluetooth.
     *
     * See: [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions)
     */
    val requiresBluetoothRuntimePermissions: Boolean
        get() = androidSdkVersion >= SdkVersion.S

    /**
     * Whether the device supports runtime permissions.
     *
     * See: [Request runtime permissions](https://developer.android.com/training/permissions/requesting)
     */
    val supportsRuntimePermissions: Boolean
        get() = androidSdkVersion >= SdkVersion.MARSHMALLOW

    /**
     * Unregisters the broadcast receiver that listens for Bluetooth state changes.
     *
     * This should be called when the environment is no longer needed.
     */
    fun close()
}