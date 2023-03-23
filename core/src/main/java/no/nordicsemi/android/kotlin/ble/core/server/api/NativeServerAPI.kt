package no.nordicsemi.android.kotlin.ble.core.server.api

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.RealClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent
import no.nordicsemi.android.kotlin.ble.core.server.OnServerPhyRead
import no.nordicsemi.android.kotlin.ble.core.server.OnServerPhyUpdate
import no.nordicsemi.android.kotlin.ble.core.server.callback.BleGattServerCallback
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BluetoothGattServiceFactory

@SuppressLint("MissingPermission")
internal class NativeServerAPI(
    private val server: BluetoothGattServer,
    private val callback: BleGattServerCallback
) : ServerAPI {

    override val event: SharedFlow<GattServerEvent> = callback.event

    companion object {
        fun create(context: Context): NativeServerAPI {
            val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            val callback = BleGattServerCallback()
            val bluetoothGattServer = bluetoothManager.openGattServer(context, callback)
            return NativeServerAPI(bluetoothGattServer, callback)
        }
    }

    internal fun configure(vararg config: BleServerGattServiceConfig) {
        config.onEach {
            server.addService(BluetoothGattServiceFactory.create(it))
        }

        var index = 0

        callback.onServiceAdded = {
            if  (index <= config.lastIndex) {
                server.addService(BluetoothGattServiceFactory.create(config[index++]))
            } else {
                callback.onServiceAdded = null
            }
        }

        if (config.isNotEmpty()) {
            server.addService(BluetoothGattServiceFactory.create(config[index++]))
        }
    }

    override fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    ) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        server.sendResponse(bleDevice, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        val bleDevice = (device as? RealClientDevice)?.device!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm, value)
        } else {
            characteristic.value = value
            server.notifyCharacteristicChanged(bleDevice, characteristic, confirm)
        }
    }

    override fun close() {
        server.close() //TODO
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
