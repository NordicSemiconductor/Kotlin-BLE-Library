package no.nordicsemi.android.kotlin.ble.client.mock

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.GattClientEvent
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.mock.MockEngine

class BleMockGatt(
    private val mockEngine: MockEngine,
    private val serverDevice: MockServerDevice,
    private val clientDevice: ClientDevice,
    override val autoConnect: Boolean
) : GattClientAPI {

    private val _event = MutableSharedFlow<GattClientEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val event: SharedFlow<GattClientEvent> = _event.asSharedFlow()

    override val device: ServerDevice
        get() = serverDevice

    override fun onEvent(event: GattClientEvent) {
        _event.tryEmit(event)
    }

    override fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        mockEngine.writeCharacteristic(serverDevice, clientDevice, characteristic, value, writeType)
    }

    override fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        mockEngine.readCharacteristic(serverDevice, clientDevice, characteristic)
    }

    override fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        mockEngine.enableCharacteristicNotification(clientDevice, serverDevice, characteristic)
    }

    override fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        mockEngine.disableCharacteristicNotification(clientDevice, serverDevice, characteristic)
    }

    override fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        mockEngine.writeDescriptor(serverDevice, clientDevice, descriptor, value)
    }

    override fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        mockEngine.readDescriptor(serverDevice, clientDevice, descriptor)
    }

    override fun requestMtu(mtu: Int) {
        mockEngine.requestMtu(clientDevice, serverDevice, mtu)
    }

    override fun readRemoteRssi() {
        mockEngine.readRemoteRssi(clientDevice, serverDevice)
    }

    override fun readPhy() {
        mockEngine.readPhy(clientDevice, serverDevice)
    }

    override fun discoverServices() {
        mockEngine.discoverServices(clientDevice, serverDevice)
    }

    override fun setPreferredPhy(txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        mockEngine.setPreferredPhy(clientDevice, serverDevice, txPhy, rxPhy, phyOption)
    }

    override fun disconnect() {
        mockEngine.cancelConnection(serverDevice, clientDevice)
    }

    override fun clearServicesCache() {
        TODO("Not yet implemented")
    }

    override fun close() {
        mockEngine.close(serverDevice, clientDevice)
    }

    override fun beginReliableWrite() {
        TODO("Not yet implemented")
    }

    override fun abortReliableWrite() {
        TODO("Not yet implemented")
    }

    override fun executeReliableWrite() {
        TODO("Not yet implemented")
    }
}
