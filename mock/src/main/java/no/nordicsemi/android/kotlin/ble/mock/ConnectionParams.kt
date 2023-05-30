package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption

data class ConnectionParams(
    val mtu: Int = 0,
    val rssi: Int = 0,
    val txPhy: BleGattPhy = BleGattPhy.PHY_LE_1M,
    val rxPhy: BleGattPhy = BleGattPhy.PHY_LE_1M,
    val phyOption: PhyOption = PhyOption.NO_PREFERRED
)
