package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.common.core.toDisplayString
import java.util.UUID

data class NativeBluetoothGattCharacteristic(
    val characteristic: BluetoothGattCharacteristic
) : IBluetoothGattCharacteristic {

    override val uuid: UUID
        get() = characteristic.uuid
    override val instanceId: Int
        get() = characteristic.instanceId
    override val permissions: Int
        get() = characteristic.permissions
    override val properties: Int
        get() = characteristic.properties
    override var writeType: Int
        get() = characteristic.writeType
        set(value) {}
    override var value: ByteArray
        get() = characteristic.value ?: byteArrayOf()
        set(value) {
            characteristic.value = value
        }
    override val descriptors: List<IBluetoothGattDescriptor>
        get() = characteristic.descriptors.map { NativeBluetoothGattDescriptor(it) }

    override fun toString(): String {
        return StringBuilder()
            .append("{")
            .append("uuid: $uuid")
            .append("instanceId: $instanceId")
            .append("permissions: $permissions")
            .append("properties: $properties")
            .append("writeType: $writeType")
            .append("value: ${value.toDisplayString()}")
            .append("descriptors: $descriptors")
            .append("}")
            .toString()
    }
}