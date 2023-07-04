package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothDevice

enum class BleGattPhy(private val value: Int) {
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),
    PHY_LE_2M(BluetoothDevice.PHY_LE_2M),
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    fun toNative(): Int = value

    companion object {
        fun create(value: Int): BleGattPhy {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleGattPhy for value: $value")
        }
    }
}
