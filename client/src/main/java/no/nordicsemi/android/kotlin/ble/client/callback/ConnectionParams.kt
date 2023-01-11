package no.nordicsemi.android.kotlin.ble.client.callback

import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

data class ConnectionParams(
    val mtu: Int? = null,
    val rssi: Int? = null,
    val txPhy: BleGattPhy? = null,
    val rxPhy: BleGattPhy? = null
)
