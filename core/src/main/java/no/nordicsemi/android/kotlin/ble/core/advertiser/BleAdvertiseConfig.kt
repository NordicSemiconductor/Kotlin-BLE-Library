package no.nordicsemi.android.kotlin.ble.core.advertiser

import android.bluetooth.le.AdvertiseSettings

/**
 * A class for BLE advertisement configuration.
 *
 * @property settings Settings ([BleAdvertiseSettings]) for Bluetooth LE advertising.
 * @property advertiseData Advertisement data ([BleAdvertiseData]) to be broadcasted.
 * @property scanResponseData Scan response ([BleAdvertiseData]) associated with the advertisement data.
 *
 * @see [AdvertiseSettings](https://developer.android.com/reference/android/bluetooth/le/AdvertiseSettings)
 */
data class BleAdvertiseConfig (
    val settings: BleAdvertiseSettings = BleAdvertiseSettings(),
    val advertiseData: BleAdvertiseData? = null,
    val scanResponseData: BleAdvertiseData? = null
)
