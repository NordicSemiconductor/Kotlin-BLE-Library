package no.nordicsemi.android.kotlin.ble.server.native

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun readPhy(device: BluetoothDevice) {
        server.readPhy(device)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun requestPhy(device: BluetoothDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        server.setPreferredPhy(device, txPhy.value, rxPhy.value, phyOption.value)
    }
}
