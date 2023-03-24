package no.nordicsemi.android.kotlin.ble.core.data

data class PhyInfo(
    val txPhy: BleGattPhy,
    val rxPhy: BleGattPhy
)
