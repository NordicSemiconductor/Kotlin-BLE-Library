package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

class NativeBluetoothGattCharacteristic(
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
        get() = characteristic.value
        set(value) {
            characteristic.value = value
        }
    override val descriptors: List<IBluetoothGattDescriptor>
        get() = characteristic.descriptors.map { NativeBluetoothGattDescriptor(it) }
}