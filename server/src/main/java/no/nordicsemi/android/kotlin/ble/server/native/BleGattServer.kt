package no.nordicsemi.android.kotlin.ble.server.native

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption

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

    fun readPhy(device: BluetoothDevice)

    fun requestPhy(device: BluetoothDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption)
}
