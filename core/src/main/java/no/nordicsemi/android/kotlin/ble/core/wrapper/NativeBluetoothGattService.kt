package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattService
import java.util.UUID

class NativeBluetoothGattService(
    val service: BluetoothGattService
) : IBluetoothGattService {

    override val uuid: UUID
        get() = service.uuid
    override val type: Int
        get() = service.type
    override val characteristics: List<IBluetoothGattCharacteristic>
        get() = service.characteristics.map { NativeBluetoothGattCharacteristic(it) }
}
