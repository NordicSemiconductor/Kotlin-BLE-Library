package no.nordicsemi.android.kotlin.ble.core.advertiser

data class BleAdvertiseConfig (
    val settings: BleAdvertiseSettings = BleAdvertiseSettings(),
    val advertiseData: BleAdvertiseData? = null,
    val scanResponseData: BleAdvertiseData? = null
)
