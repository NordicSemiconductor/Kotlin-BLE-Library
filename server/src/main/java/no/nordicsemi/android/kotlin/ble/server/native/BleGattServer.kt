package no.nordicsemi.android.kotlin.ble.server.native

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

interface BleGattServer {

    fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    )

    fun notifyCharacteristicChanged(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    )

    fun close()

    fun connect(device: BluetoothDevice, autoConnect: Boolean)
}
