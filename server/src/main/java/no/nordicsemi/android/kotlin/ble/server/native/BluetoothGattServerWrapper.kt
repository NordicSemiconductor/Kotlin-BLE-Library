package no.nordicsemi.android.kotlin.ble.server.native

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build

@SuppressLint("MissingPermission")
class BluetoothGattServerWrapper(
    private val server: BluetoothGattServer
) : BleGattServer {

    override fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    ) {
        server.sendResponse(device, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(device, characteristic, confirm, value)
        } else {
            characteristic.value = value
            server.notifyCharacteristicChanged(device, characteristic, confirm)
        }
    }

    override fun close() {
        server.close()
    }

    override fun connect(device: BluetoothDevice, autoConnect: Boolean) {
        server.connect(device, autoConnect)
    }
}
