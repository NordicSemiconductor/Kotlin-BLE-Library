package no.nordicsemi.android.kotlin.ble.server.real

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.RealClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyRead
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyUpdate
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent

/**
 * A wrapper around [BluetoothGattServer] and [BluetoothGattServerCallback].
 * As an input it uses callbacks of [BluetoothGattServerCallback] and as an output calls to
 * [BluetoothGattServer].
 *
 * @property server Native Android API [BluetoothGattServer].
 * @property callback Native wrapper around Android [BluetoothGattServerCallback].
 */
@SuppressLint("MissingPermission")
class NativeServerBleAPI(
    val server: BluetoothGattServer,
    val callback: ServerBleGattCallback
) : GattServerAPI {

    override val event: SharedFlow<ServerGattEvent> = callback.event

    override fun onEvent(event: ServerGattEvent) {
        callback.onEvent(event)
    }

    companion object {
        fun create(context: Context): NativeServerBleAPI {
            val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            val callback = ServerBleGattCallback()
            val bluetoothGattServer = bluetoothManager.openGattServer(context, callback)
            return NativeServerBleAPI(bluetoothGattServer, callback)
        }
    }

    override fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: DataByteArray?
    ) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.sendResponse(bleDevice, requestId, status, offset, value?.value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        confirm: Boolean,
        value: DataByteArray
    ) {
        val characteristic = (characteristic as NativeBluetoothGattCharacteristic).characteristic
        val bleDevice = (device as? RealClientDevice)?.device!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm, value.value)
        } else {
            characteristic.value = value.value
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm)
        }
    }

    override fun close() {
        server.clearServices()
        server.close() //TODO
    }

    override fun cancelConnection(device: ClientDevice) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.cancelConnection(bleDevice)
    }

    override fun connect(device: ClientDevice, autoConnect: Boolean) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.connect(bleDevice, autoConnect)
    }

    override fun readPhy(device: ClientDevice) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.readPhy(bleDevice)
        } else {
            callback.onEvent(OnServerPhyRead(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    override fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.setPreferredPhy((device as RealClientDevice).device, txPhy.value, rxPhy.value, phyOption.value)
        } else {
            callback.onEvent(OnServerPhyUpdate(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }
}
