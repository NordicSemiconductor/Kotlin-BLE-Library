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
     * Connection parameter update - Use the connection parameters recommended by the
     * Bluetooth SIG. This is the default value if no connection parameter update
     * is requested.
     * <p>
     * Interval: 30 - 50 ms, latency: 0, supervision timeout: 5 sec (Android 8+) or 20 sec (before).
     *
     * @see <a href="https://android.googlesource.com/platform/packages/modules/Bluetooth/+/673c5903c4a920510c371af26e5870857a584ead%5E!">commit 673c5903c4a920510c371af26e5870857a584ead</a>
     */
    BALANCED(BluetoothGatt.CONNECTION_PRIORITY_BALANCED),


    /**
     * Connection parameter update - Request a high priority, low latency connection.
     * An application should only request high priority connection parameters to transfer
     * large amounts of data over LE quickly. Once the transfer is complete, the application
     * should request [BALANCED] connection parameters
     * to reduce energy use.
     * <p>
     * Interval: 11.25 - 15 ms (Android 6+) or 7.5 - 10 ms (Android 4.3 - 5.1),
     * latency: 0, supervision timeout: 5 sec (Android 8+) or 20 sec (before).
     *
     * @see <a href="https://android.googlesource.com/platform/packages/modules/Bluetooth/+/4bc7c7e877c9d18f2781229c553b6144f9fd7236%5E%21/">commit 4bc7c7e877c9d18f2781229c553b6144f9fd7236</a>
     * @see <a href="https://android.googlesource.com/platform/packages/modules/Bluetooth/+/673c5903c4a920510c371af26e5870857a584ead%5E!">commit 673c5903c4a920510c371af26e5870857a584ead</a>
     */
    HIGH(BluetoothGatt.CONNECTION_PRIORITY_HIGH),

    /**
     * Connection parameter update - Request low power, reduced data rate connection parameters.
     * <p>
     * Interval: 100 - 125 ms, latency: 2, supervision timeout: 5 sec (Android 8+) or 20 sec (before).
     *
     * @see <a href="https://android.googlesource.com/platform/packages/modules/Bluetooth/+/673c5903c4a920510c371af26e5870857a584ead%5E!">commit 673c5903c4a920510c371af26e5870857a584ead</a>
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
