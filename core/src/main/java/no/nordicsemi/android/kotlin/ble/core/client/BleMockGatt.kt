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

    fun onEvent(event: GattEvent) {
        _event.tryEmit(event)
    }

    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        mockEngine.writeCharacteristic(serverDevice, characteristic, value, writeType)
    }

    override fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        mockEngine.readCharacteristic(serverDevice, characteristic)
    }

    override fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        mockEngine.enableCharacteristicNotification(serverDevice, characteristic)
    }

    override fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        mockEngine.disableCharacteristicNotification(serverDevice, characteristic)
    }

    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        mockEngine.writeDescriptor(serverDevice, descriptor, value)
    }

    override fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        mockEngine.readDescriptor(serverDevice, descriptor)
    }

    override fun readRemoteRssi() {
        mockEngine.readRemoteRssi(serverDevice)
    }

    override fun readPhy() {
        mockEngine.readPhy(serverDevice)
    }

    override fun discoverServices() {
        mockEngine.discoverServices(serverDevice)
    }

    override fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) {
        mockEngine.setPreferredPhy(serverDevice, txPhy, rxPhy, phyOptions)
    }
}
