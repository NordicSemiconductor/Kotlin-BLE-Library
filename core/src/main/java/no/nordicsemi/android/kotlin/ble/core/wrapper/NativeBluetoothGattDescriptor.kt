package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

class NativeBluetoothGattDescriptor(
    val descriptor: BluetoothGattDescriptor,
) : IBluetoothGattDescriptor {

    override val uuid: UUID
        get() = descriptor.uuid

    override val permissions: Int
        get() = descriptor.permissions

    override var value: ByteArray
        get() = descriptor.value
        set(value) {
            descriptor.value = value
        }

    override val characteristic: IBluetoothGattCharacteristic =
        NativeBluetoothGattCharacteristic(descriptor.characteristic)
}
