package no.nordicsemi.android.kotlin.ble.advertiser.data

data class BleAdvertiseConfig (
    val settings: BleAdvertiseSettings = BleAdvertiseSettings(),
    val advertiseData: BleAdvertiseData? = null,
    val scanResponseData: BleAdvertiseData? = null
)
