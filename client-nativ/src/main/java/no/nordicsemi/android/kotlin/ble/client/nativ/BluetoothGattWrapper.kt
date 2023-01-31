package no.nordicsemi.android.kotlin.ble.client.nativ

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.client.nativ.callback.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.core.client.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.client.BleGatt
import no.nordicsemi.android.kotlin.ble.core.client.GattEvent
import no.nordicsemi.android.kotlin.ble.core.client.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

internal class BluetoothGattWrapper(
    private val gatt: BluetoothGatt,
    private val callback: BluetoothGattClientCallback
) : BleGatt {

    override val event: SharedFlow<GattEvent> = callback.event

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(characteristic, value, writeType.value)
        } else {
            characteristic.writeType = writeType.value
            characteristic.value = value
            gatt.writeCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt.readCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, false)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value)
        } else {
            descriptor.value = value
            gatt.writeDescriptor(descriptor)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        gatt.readDescriptor(descriptor)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readRemoteRssi() {
        gatt.readRemoteRssi()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readPhy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.readPhy()
        } else {
            callback.onEvent(OnPhyUpdate(BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun discoverServices() {
        gatt.discoverServices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.setPreferredPhy(txPhy, rxPhy, phyOptions)
        } else {
            callback.onEvent(OnPhyUpdate(BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }
}
