package no.nordicsemi.android.kotlin.ble.core.server.api

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.common.core.simpleSharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.core.server.service.service.BluetoothGattServiceFactory

internal class MockServerAPI(
    private val mockEngine: MockEngine
) : ServerAPI {

    private val _event = simpleSharedFlow<GattServerEvent>()
    override val event: SharedFlow<GattServerEvent> = _event.asSharedFlow()

    init {
        mockEngine.registerServer(this)
    }

    fun onEvent(event: GattServerEvent) {
        _event.tryEmit(event)
    }

    companion object {
        fun create(vararg config: BleServerGattServiceConfig): ServerAPI {
            val services = config.map { BluetoothGattServiceFactory.create(it) }
            MockEngine.addServices(services)

            return MockServerAPI(MockEngine)
        }
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
