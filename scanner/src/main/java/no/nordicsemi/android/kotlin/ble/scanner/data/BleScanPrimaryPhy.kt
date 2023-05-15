package no.nordicsemi.android.kotlin.ble.scanner.data

import android.bluetooth.BluetoothDevice

enum class BleScanPrimaryPhy(private val value: Int) {

    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    companion object {
        fun create(value: Int): BleScanPrimaryPhy {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleScanDataStatus for value: $value")
        }
    }
}
