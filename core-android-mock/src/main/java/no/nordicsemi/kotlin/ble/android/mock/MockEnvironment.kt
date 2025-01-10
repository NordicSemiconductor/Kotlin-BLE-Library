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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.android.mock

import no.nordicsemi.kotlin.ble.core.TxPowerLevel
import no.nordicsemi.kotlin.ble.core.android.AdvertisingDataDefinition
import no.nordicsemi.kotlin.ble.core.mock.MockEnvironment
import org.jetbrains.annotations.Range

/**
 * The environment for the latest Android API.
 */
typealias LatestApi = no.nordicsemi.kotlin.ble.android.mock.MockEnvironment.Api31
/**
 * A type for a mock advertiser.
 *
 * This callback is called when the app requests the mock Bluetooth LE advertiser to advertise.
 * It should return the TX power level used for mock advertising, in dBm.
 * Valid values are from -127 to +1 dBm.
 */
typealias MockAdvertiser = (requestedTxPower: Int, advertisingData: AdvertisingDataDefinition, scanResponse: AdvertisingDataDefinition?) -> Result<Int>

private const val DEFAULT_NAME = "Mock"
private val DEFAULT_MOCK_ADVERTISER: MockAdvertiser = { requestedTxPower, _, _ ->
    Result.success(requestedTxPower.coerceIn(TxPowerLevel.ULTRA_LOW..TxPowerLevel.HIGH))
}

/**
 * A mock environment that can be used to test the behavior of the Central Manager.
 *
 * @property deviceName The device name, by default set to "Mock".
 * @property isBluetoothSupported Whether Bluetooth is supported on the device.
 * @property isBluetoothEnabled Whether Bluetooth is enabled.
 * @property androidSdkVersion The Android SDK version.
 * @property isLocationRequiredForScanning Whether location is required to scan for Bluetooth devices.
 * @property isLocationPermissionGranted Whether the fine location permission is granted.
 * @property isLocationEnabled Whether location service is enabled on the device.
 * @property isLe2MPhySupported Whether LE 2M PHY is supported on the device.
 * @property isLeCodedPhySupported Whether LE Coded PHY is supported on the device.
 * @property isScanningOnLeCodedPhySupported Whether the device can scan for Bluetooth LE devices
 * advertising on LE Coded PHY as Primary PHY.
 * @property isBluetoothScanPermissionGranted Whether the `BLUETOOTH_SCAN` permission is granted.
 * @property isBluetoothConnectPermissionGranted Whether the `BLUETOOTH_CONNECT` permission is granted.
 * @property isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
 * @property isLeExtendedAdvertisingSupported Whether LE Extended Advertising feature is supported.
 * @property leMaximumAdvertisingDataLength The maximum LE advertising data length in bytes,
 * if LE Extended Advertising feature is supported.
 * @property isBluetoothAdvertisePermissionGranted Whether the `BLUETOOTH_ADVERTISE` permission is granted.
 * @property advertiser A callback that will be called when the app requests to advertise.
 * The callback should return the TX power level used for mock advertising.
 */
sealed class MockEnvironment(
    val androidSdkVersion: Int,
    deviceName: String,
    isBluetoothSupported: Boolean,
    isBluetoothEnabled: Boolean,
    val isLocationRequiredForScanning: Boolean = false,
    val isLocationPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isLe2MPhySupported: Boolean = false,
    val isLeCodedPhySupported: Boolean = false,
    val isScanningOnLeCodedPhySupported: Boolean = isLeCodedPhySupported,
    val isBluetoothScanPermissionGranted: Boolean = false,
    val isBluetoothConnectPermissionGranted: Boolean = false,
    val isMultipleAdvertisementSupported: Boolean, // TODO this is not used
    val isLeExtendedAdvertisingSupported: Boolean = false,
    val leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int = 31,
    val isBluetoothAdvertisePermissionGranted: Boolean = false,
    val advertiser: MockAdvertiser,
): MockEnvironment(deviceName, isBluetoothSupported, isBluetoothEnabled) {

    /**
     * Android SDK versions.
     */
    class AndroidSdkVersion {
        companion object {
            /** Android 5.0 */
            const val LOLLIPOP = 21
            /** Android 6.0 */
            const val MARSHMALLOW = 23
            /** Android 8.0 */
            const val OREO = 26
            /** Android 12 */
            const val S = 31
        }
    }

    /**
     * A mock environment for Android 5.0 (Lollipop).
     *
     * @param deviceName The device name, by default set to "Mock".
     * @param isBluetoothSupported Whether Bluetooth is supported on the device.
     * @param isBluetoothEnabled Whether Bluetooth is enabled on the device.
     * @param isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
     * @param advertiser A callback that will be called when the app requests to advertise.
     * The callback should return TX power level used for mock advertising.
     */
    class Api21(
        deviceName: String = DEFAULT_NAME,
        isBluetoothSupported: Boolean = true,
        isBluetoothEnabled: Boolean = true,
        isMultipleAdvertisementSupported: Boolean = true,
        advertiser: MockAdvertiser = DEFAULT_MOCK_ADVERTISER,
    ): no.nordicsemi.kotlin.ble.android.mock.MockEnvironment(
        androidSdkVersion = AndroidSdkVersion.LOLLIPOP,
        deviceName = deviceName,
        isBluetoothSupported = isBluetoothSupported,
        isBluetoothEnabled = isBluetoothEnabled,
        isMultipleAdvertisementSupported = isMultipleAdvertisementSupported,
        advertiser = advertiser,
    )

    /**
     * A mock environment for Android 6.0 (Marshmallow).
     *
     * Since Android 6.0, location is required to scan for Bluetooth devices.
     *
     * @param deviceName The device name, by default set to "Mock".
     * @param isBluetoothSupported Whether Bluetooth is supported on the device.
     * @param isBluetoothEnabled Whether Bluetooth is enabled on the device.
     * @param isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
     * @param isLocationPermissionGranted Whether the fine location permission is granted.
     * @param isLocationEnabled Whether location service is enabled on the device.
     * @param advertiser A callback that will be called when the app requests to advertise.
     * The callback should return TX power level used for mock advertising.
     */
    class Api23(
        deviceName: String = DEFAULT_NAME,
        isBluetoothSupported: Boolean = true,
        isBluetoothEnabled: Boolean = true,
        isMultipleAdvertisementSupported: Boolean = true,
        isLocationPermissionGranted: Boolean = true,
        isLocationEnabled: Boolean = true,
        advertiser: MockAdvertiser = DEFAULT_MOCK_ADVERTISER,
    ): no.nordicsemi.kotlin.ble.android.mock.MockEnvironment(
        androidSdkVersion =AndroidSdkVersion.MARSHMALLOW,
        deviceName = deviceName,
        isBluetoothSupported = isBluetoothSupported,
        isBluetoothEnabled = isBluetoothEnabled,
        isMultipleAdvertisementSupported = isMultipleAdvertisementSupported,
        isLocationRequiredForScanning = true,
        isLocationPermissionGranted = isLocationPermissionGranted,
        isLocationEnabled = isLocationEnabled,
        advertiser = advertiser,
    )

    /**
     * A mock environment for Android 8.0 (Oreo).
     *
     * Since Android 8.0, LE 2M and LE Coded PHY are supported by the API, but not necessarily by
     * the device. Also, some devices may not support scanning for Bluetooth LE devices advertising
     * on LE Coded PHY as Primary PHY despite supporting LE Coded PHY.
     *
     * @param deviceName The device name, by default set to "Mock".
     * @param isBluetoothSupported Whether Bluetooth is supported on the device.
     * @param isBluetoothEnabled Whether Bluetooth is enabled on the device.
     * @param isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
     * @param isLeExtendedAdvertisingSupported Whether LE Extended Advertising feature is supported.
     * @param leMaximumAdvertisingDataLength The maximum LE advertising data length in bytes,
     * if LE Extended Advertising feature is supported.
     * @param isLe2MPhySupported Whether LE 2M PHY is supported on the device.
     * @param isLeCodedPhySupported Whether LE Coded PHY is supported on the device.
     * @param isScanningOnLeCodedPhySupported Whether the device can scan for Bluetooth LE devices
     * advertising on LE Coded PHY as Primary PHY.
     * @param isLocationPermissionGranted Whether the fine location permission is granted.
     * @param isLocationEnabled Whether location service is enabled on the device.
     * @param advertiser A callback that will be called when the app requests to advertise.
     * The callback should return TX power level used for mock advertising.
     */
    class Api26(
        deviceName: String = DEFAULT_NAME,
        isBluetoothSupported: Boolean = true,
        isBluetoothEnabled: Boolean = true,
        isMultipleAdvertisementSupported: Boolean = true,
        isLeExtendedAdvertisingSupported: Boolean = true,
        leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int =
            if (isLeExtendedAdvertisingSupported) 1650 else 31,
        isLe2MPhySupported: Boolean = true,
        isLeCodedPhySupported: Boolean = true,
        isScanningOnLeCodedPhySupported: Boolean = isLeCodedPhySupported,
        isLocationPermissionGranted: Boolean = true,
        isLocationEnabled: Boolean = true,
        advertiser: MockAdvertiser = DEFAULT_MOCK_ADVERTISER,
    ): no.nordicsemi.kotlin.ble.android.mock.MockEnvironment(
        androidSdkVersion = AndroidSdkVersion.OREO,
        deviceName = deviceName,
        isBluetoothSupported = isBluetoothSupported,
        isBluetoothEnabled = isBluetoothEnabled,
        isMultipleAdvertisementSupported = isMultipleAdvertisementSupported,
        isLeExtendedAdvertisingSupported = isLeExtendedAdvertisingSupported,
        leMaximumAdvertisingDataLength = leMaximumAdvertisingDataLength,
        isLocationRequiredForScanning = true,
        isLocationPermissionGranted = isLocationPermissionGranted,
        isLocationEnabled = isLocationEnabled,
        isLe2MPhySupported = isLe2MPhySupported,
        isLeCodedPhySupported = isLeCodedPhySupported,
        isScanningOnLeCodedPhySupported = isScanningOnLeCodedPhySupported,
        advertiser = advertiser,
    )

    /**
     * A mock environment for Android 12 (S).
     *
     * Since Android 12, the `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` permissions are required to
     * scan and connect to Bluetooth devices. When `BLUETOOTH_SCAN` permission is set using
     * `neverForLocation` flag, the location is not required to scan for Bluetooth devices.
     * Scan results won't contain iBeacons and Eddystone beacons.
     *
     * See:
     * * [Bluetooth Permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions)
     * * [`neverForLocation flag`](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions#assert-never-for-location)
     *
     * @param deviceName The device name, by default set to "Mock".
     * @param isBluetoothSupported Whether Bluetooth is supported on the device.
     * @param isBluetoothEnabled Whether Bluetooth is enabled on the device.
     * @param isMultipleAdvertisementSupported Whether multi advertisement is supported by the chipset.
     * @param isLeExtendedAdvertisingSupported Whether LE Extended Advertising feature is supported.
     * @param leMaximumAdvertisingDataLength The maximum LE advertising data length in bytes,
     * if LE Extended Advertising feature is supported.
     * @param isLe2MPhySupported Whether LE 2M PHY is supported on the device.
     * @param isLeCodedPhySupported Whether LE Coded PHY is supported on the device.
     * @param isScanningOnLeCodedPhySupported Whether the device can scan for Bluetooth LE devices
     * advertising on LE Coded PHY as Primary PHY.
     * @param isBluetoothScanPermissionGranted Whether the `BLUETOOTH_SCAN` permission is granted.
     * @param usesLeScanningForLocation Whether the app is using results of Bluetooth LE scanning
     * to estimate device location. By default, `neverForLocation` flag is assumed.
     * @param isBluetoothConnectPermissionGranted Whether the `BLUETOOTH_CONNECT` permission is granted.
     * @param isLocationPermissionGranted Whether the fine location permission is granted.
     * @param isLocationEnabled Whether location service is enabled on the device.
     * @param advertiser A callback that will be called when the app requests to advertise.
     * The callback should return TX power level used for mock advertising.
     */
    class Api31(
        deviceName: String = DEFAULT_NAME,
        isBluetoothSupported: Boolean = true,
        isBluetoothEnabled: Boolean = true,
        isMultipleAdvertisementSupported: Boolean = true,
        isLeExtendedAdvertisingSupported: Boolean = true,
        leMaximumAdvertisingDataLength: @Range(from = 31, to = 1650) Int =
            if (isLeExtendedAdvertisingSupported) 1650 else 31,
        isLe2MPhySupported: Boolean = true,
        isLeCodedPhySupported: Boolean = true,
        isScanningOnLeCodedPhySupported: Boolean = isLeCodedPhySupported,
        isBluetoothScanPermissionGranted: Boolean = true,
        isBluetoothConnectPermissionGranted: Boolean = true,
        isBluetoothAdvertisePermissionGranted: Boolean = true,
        usesLeScanningForLocation: Boolean = false,
        isLocationPermissionGranted: Boolean = true,
        isLocationEnabled: Boolean = true,
        advertiser: MockAdvertiser = DEFAULT_MOCK_ADVERTISER,
    ): no.nordicsemi.kotlin.ble.android.mock.MockEnvironment(
        androidSdkVersion = AndroidSdkVersion.S,
        deviceName = deviceName,
        isBluetoothSupported = isBluetoothSupported,
        isBluetoothEnabled = isBluetoothEnabled,
        isMultipleAdvertisementSupported = isMultipleAdvertisementSupported,
        isLeExtendedAdvertisingSupported = isLeExtendedAdvertisingSupported,
        leMaximumAdvertisingDataLength = leMaximumAdvertisingDataLength,
        isLocationRequiredForScanning = !usesLeScanningForLocation,
        isLocationPermissionGranted = isLocationPermissionGranted,
        isLocationEnabled = isLocationEnabled,
        isLe2MPhySupported = isLe2MPhySupported,
        isLeCodedPhySupported = isLeCodedPhySupported,
        isScanningOnLeCodedPhySupported = isScanningOnLeCodedPhySupported,
        isBluetoothScanPermissionGranted = isBluetoothScanPermissionGranted,
        isBluetoothConnectPermissionGranted = isBluetoothConnectPermissionGranted,
        isBluetoothAdvertisePermissionGranted = isBluetoothAdvertisePermissionGranted,
        advertiser = advertiser,
    )

    /**
     * Whether the device requires runtime permissions to use Bluetooth.
     *
     * See: [Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions)
     */
    val requiresBluetoothRuntimePermissions: Boolean = androidSdkVersion >= AndroidSdkVersion.S
}