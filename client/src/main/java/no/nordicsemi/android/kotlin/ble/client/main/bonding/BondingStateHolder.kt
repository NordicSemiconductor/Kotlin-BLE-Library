package no.nordicsemi.android.kotlin.ble.client.main.bonding

import no.nordicsemi.android.kotlin.ble.client.real.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.core.data.BondState

object BondingStateHolder {

    private val bondingState = mutableMapOf<String, BondState>()
    private val callbacks = mutableMapOf<String, BluetoothGattClientCallback>()

    internal fun onBondStateUpdate(deviceAddress: String, bondState: BondState) {
        bondingState[deviceAddress] = bondState
    }
}
