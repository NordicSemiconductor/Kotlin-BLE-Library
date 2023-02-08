package no.nordicsemi.android.kotlin.ble.core.client

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine

internal class BleMockGatt(
    private val mockEngine: MockEngine,
    private val serverDevice: MockServerDevice
) : BleGatt {

    val _event = MutableSharedFlow<GattEvent>()
    override val event: SharedFlow<GattEvent> = _event.asSharedFlow()

    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {

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
