package no.nordicsemi.android.kotlin.ble.core.mock

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.BleServerAPI
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent

class MockServerAPI(
    private val mockEngine: MockEngine
) : BleServerAPI {

    override val event: SharedFlow<GattServerEvent>
        get() = TODO("Not yet implemented")

    override fun sendResponse(device: BleDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun notifyCharacteristicChanged(
        device: BleDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        mockEngine.notifyCharacteristicChanged(device, characteristic, confirm, value)
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun connect(device: BleDevice, autoConnect: Boolean) {
        TODO("Not yet implemented")
    }

    override fun readPhy(device: BleDevice) {
        TODO("Not yet implemented")
    }

    override fun requestPhy(device: BleDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        TODO("Not yet implemented")
    }
}
