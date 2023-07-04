package no.nordicsemi.android.kotlin.ble.core.scanner

import android.bluetooth.BluetoothDevice

enum class BleGattPrimaryPhy(private val value: Int) {

    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    fun toNative(): Int = value

    companion object {
        fun create(value: Int): BleGattPrimaryPhy {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleScanDataStatus for value: $value")
        }
    }
}
