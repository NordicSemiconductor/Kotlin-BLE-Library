package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothGatt
import androidx.annotation.RequiresApi

/**
 * Class representing available connection priorities. Selecting a specific mode will affect
 * two parameters: latency and power consumption.
 *
 * @property value Native Android API value.
 */
enum class BleGattConnectionPriority(val value: Int) {

    /**
     * Connection parameter update - Use the connection parameters recommended by the Bluetooth SIG.
     * This is the default value if no connection parameter update is requested.
     */
    BALANCED(BluetoothGatt.CONNECTION_PRIORITY_BALANCED),


    /**
     * Connection parameter update - Request a high priority, low latency connection. An application
     * should only request high priority connection parameters to transfer large amounts of data
     * over LE quickly. Once the transfer is complete, the application should request
     * [BALANCED] connection parameters to reduce energy use.
     */
    HIGH(BluetoothGatt.CONNECTION_PRIORITY_HIGH),

    /**
     * Connection parameter update - Request low power, reduced data rate connection parameters.
     */
    LOW_POWER(BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER),

    /**
     * Connection parameter update - Request the priority preferred for Digital Car Key for a lower
     * latency connection. This connection parameter will consume more power than
     * [BALANCED], so it is recommended that apps do not use this
     * unless it specifically fits their use case.
     */
    @RequiresApi(34)
    DIGITAL_CAR_KEY(BluetoothGatt.CONNECTION_PRIORITY_DCK),
}
