package no.nordicsemi.android.kotlin.ble.core.scanner

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Returns the primary Physical Layer on which this advertisment was received.
 *
 * @property value Native Android API value.
 * @see [ScanResult.getprimaryphy](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult#getprimaryphy)
 */
@RequiresApi(Build.VERSION_CODES.O)
enum class BleGattPrimaryPhy(val value: Int) {

    /**
     * Bluetooth LE 1M PHY. Used to refer to LE 1M Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),

    /**
     * Bluetooth LE Coded PHY. Used to refer to LE Coded Physical Channel for advertising, scanning or connection.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    companion object {
        fun create(value: Int): BleGattPrimaryPhy {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleGattPrimaryPhy for value: $value")
        }
    }
}
