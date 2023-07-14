package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.common.core.toDisplayString
import java.util.UUID

data class NativeBluetoothGattDescriptor(
    val descriptor: BluetoothGattDescriptor,
) : IBluetoothGattDescriptor {

    override val uuid: UUID
        get() = descriptor.uuid

    override val permissions: Int
        get() = descriptor.permissions

    override var value: ByteArray
        get() = descriptor.value ?: byteArrayOf()
        set(value) {
            descriptor.value = value
        }

    override val characteristic: IBluetoothGattCharacteristic =
        NativeBluetoothGattCharacteristic(descriptor.characteristic)

    override fun toString(): String {
        return StringBuilder()
            .append("{")
            .append("uuid: $uuid")
            .append("permissions: $permissions")
            .append("value: ${value.toDisplayString()}")
            .append("}")
            .toString()
    }
}
