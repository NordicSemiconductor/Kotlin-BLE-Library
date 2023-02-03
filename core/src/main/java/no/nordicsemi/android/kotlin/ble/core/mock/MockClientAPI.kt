package no.nordicsemi.android.kotlin.ble.core.mock

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.client.BleGatt
import no.nordicsemi.android.kotlin.ble.core.client.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.client.GattEvent

class MockClientAPI : BleGatt {
    override val event: SharedFlow<GattEvent>
        get() = TODO("Not yet implemented")

    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        TODO("Not yet implemented")
    }

    override fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        TODO("Not yet implemented")
    }

    override fun readRemoteRssi() {
        TODO("Not yet implemented")
    }

    override fun readPhy() {
        TODO("Not yet implemented")
    }

    override fun discoverServices() {
        TODO("Not yet implemented")
    }

    override fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) {
        TODO("Not yet implemented")
    }
}
