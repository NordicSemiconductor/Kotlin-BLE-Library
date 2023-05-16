package no.nordicsemi.android.kotlin.ble.server.mock

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.api.GattServerEvent
import no.nordicsemi.android.kotlin.ble.server.api.ServerAPI

class MockServerAPI(
    private val mockEngine: MockEngine
) : ServerAPI {

    private val _event = MutableSharedFlow<GattServerEvent>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val event: SharedFlow<GattServerEvent> = _event.asSharedFlow()

    override fun onEvent(event: GattServerEvent) {
        _event.tryEmit(event)
    }

    override fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
        mockEngine.sendResponse(device, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        mockEngine.notifyCharacteristicChanged(device, characteristic, confirm, value)
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun cancelConnection(device: ClientDevice) {
        TODO("Not yet implemented")
    }

    override fun connect(device: ClientDevice, autoConnect: Boolean) {
        mockEngine.connect(device, autoConnect)
    }

    override fun readPhy(device: ClientDevice) {
        mockEngine.readPhy(device)
    }

    override fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        mockEngine.requestPhy(device, txPhy, rxPhy, phyOption)
    }
}
