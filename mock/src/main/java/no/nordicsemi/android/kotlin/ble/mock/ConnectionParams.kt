package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption

//null means unknown
data class ConnectionParams(
    val mtu: Int? = null,
    val rssi: Int? = null,
    val txPhy: BleGattPhy? = null,
    val rxPhy: BleGattPhy? = null,
    val phyOption: PhyOption? = null
)
