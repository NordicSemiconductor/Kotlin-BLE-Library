package no.nordicsemi.android.kotlin.ble.server

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.server.callback.BleGattServerCallback
import no.nordicsemi.android.kotlin.ble.server.native.BleServer
import no.nordicsemi.android.kotlin.ble.server.native.BluetoothGattServerWrapper
import no.nordicsemi.android.kotlin.ble.server.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.service.BluetoothGattServiceFactory

internal interface BleGattFactory {

    fun create(context: Context, vararg config: BleServerGattServiceConfig) : BleServer
}

internal class BleGattFactoryImpl : BleGattFactory {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun create(context: Context, vararg config: BleServerGattServiceConfig) : BleServer {
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val callback = BleGattServerCallback()
        val bluetoothGattServer = bluetoothManager.openGattServer(context, callback)
        val server = BluetoothGattServerWrapper(bluetoothGattServer, callback)

        config.forEach {
            bluetoothGattServer.addService(BluetoothGattServiceFactory.create(it))
        }

        return server
    }
}
