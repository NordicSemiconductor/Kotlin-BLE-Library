package no.nordicsemi.android.kotlin.ble.server.api

import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic

interface GattServerAPI {

    val event: SharedFlow<GattServerEvent>

    fun onEvent(event: GattServerEvent)

    fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: DataByteArray?
    )

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        confirm: Boolean,
        value: DataByteArray
    )

    fun close()

    fun cancelConnection(device: ClientDevice)

    fun connect(device: ClientDevice, autoConnect: Boolean)

    fun readPhy(device: ClientDevice)

    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption)
}
