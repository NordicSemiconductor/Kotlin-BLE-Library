package no.nordicsemi.android.kotlin.ble.server.main.data

import android.bluetooth.BluetoothGattServerCallback

/**
 * An additional options to configure a server behaviour.
 *
 * @property bufferSize A buffer size used for queuing [BluetoothGattServerCallback] events.
 */
data class ServerConnectionOption(
    val bufferSize: Int = 10
)
