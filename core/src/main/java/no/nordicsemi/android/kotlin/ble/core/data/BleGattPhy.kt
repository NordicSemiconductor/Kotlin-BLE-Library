package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * The secondary Physical Layer on which this advertisement was received.
 *
 * @property value Native Android API value.
 * @see [ScanResult.getsecondaryphy](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult?hl=en#getsecondaryphy)
 */
enum class BleGattPhy(val value: Int) {

    /**
     * Bluetooth LE 1M PHY. Used to refer to LE 1M Physical Channel for advertising, scanning or connection.
     */
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),

    /**
     * Bluetooth LE 2M PHY. Used to refer to LE 2M Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_2M(BluetoothDevice.PHY_LE_2M),

    /**
     * Bluetooth LE Coded PHY. Used to refer to LE Coded Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    companion object {
        fun create(value: Int): BleGattPhy {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleGattPhy for value: $value")
        }
    }
}
