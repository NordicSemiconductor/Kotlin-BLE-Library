package no.nordicsemi.android.kotlin.ble.client.native

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.client.service.BleWriteType

internal interface BleGatt {
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    )

    fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic
    )

    fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic)

    fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic)

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray)

    fun readDescriptor(descriptor: BluetoothGattDescriptor)
}
