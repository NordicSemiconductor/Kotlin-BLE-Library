package no.nordicsemi.android.kotlin.ble.core.advertiser

import android.bluetooth.le.AdvertiseSettings

/**
 * A class for BLE advertisement configuration.
 *
 * @property settings Settings ([BleAdvertisingSettings]) for Bluetooth LE advertising.
 * @property advertiseData Advertisement data ([BleAdvertisingData]) to be broadcasted.
 * @property scanResponseData Scan response ([BleAdvertisingData]) associated with the advertisement data.
 *
 * @see [AdvertiseSettings](https://developer.android.com/reference/android/bluetooth/le/AdvertiseSettings)
 */
data class BleAdvertisingConfig (
    val settings: BleAdvertisingSettings = BleAdvertisingSettings(),
    val advertiseData: BleAdvertisingData? = null,
    val scanResponseData: BleAdvertisingData? = null
)
