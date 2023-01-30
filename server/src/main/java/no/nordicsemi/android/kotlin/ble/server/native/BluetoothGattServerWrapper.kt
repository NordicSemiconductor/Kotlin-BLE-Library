package no.nordicsemi.android.kotlin.ble.server.native

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.BleServer
import no.nordicsemi.android.kotlin.ble.server.callback.BleGattServerCallback
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent
import no.nordicsemi.android.kotlin.ble.core.server.OnPhyRead

@SuppressLint("MissingPermission")
internal class BluetoothGattServerWrapper(
    private val server: BluetoothGattServer,
    private val callback: BleGattServerCallback
) : BleServer {

    override val event: SharedFlow<GattServerEvent> = callback.event

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

    override fun readPhy(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.readPhy(device)
        } else {
            callback.onEvent(OnPhyRead(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_2M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    override fun requestPhy(device: BluetoothDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.setPreferredPhy(device, txPhy.value, rxPhy.value, phyOption.value)
        }
    }
}
