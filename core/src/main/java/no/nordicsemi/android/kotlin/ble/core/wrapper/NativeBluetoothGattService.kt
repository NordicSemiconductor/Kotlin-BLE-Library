package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattService
import java.util.UUID

/**
 * Native variant of a service. It's a wrapper around [BluetoothGattService].
 */
data class NativeBluetoothGattService(
    val service: BluetoothGattService,
) : IBluetoothGattService {

    override val uuid: UUID
        get() = service.uuid
    override val type: Int
        get() = service.type
    override val characteristics: List<IBluetoothGattCharacteristic>
        get() = service.characteristics.map { NativeBluetoothGattCharacteristic(it) }

    override fun toString(): String {
        return StringBuilder()
            .append("{")
            .append("uuid: $uuid")
            .append("type: $type")
            .append("characteristics: $characteristics")
            .append("}")
            .toString()
    }
}
