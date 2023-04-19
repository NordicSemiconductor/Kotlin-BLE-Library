package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothDevice

enum class BondState(private val value: Int) {
    NONE(BluetoothDevice.BOND_NONE),
    BONDING(BluetoothDevice.BOND_BONDING),
    BONDED(BluetoothDevice.BOND_BONDED);

    companion object {
        fun create(value: Int): BondState {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create state for value: $value")
        }
    }
}
