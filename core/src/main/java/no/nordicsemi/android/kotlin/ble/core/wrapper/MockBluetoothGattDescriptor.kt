package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

/**
 * Mock variant of the descriptor. It's special feature is that it is independent from
 * Android dependencies and can be used for unit testing.
 *
 * Circular dependency between characteristic and descriptor results in custom [equals],
 * [hashCode] and [toString] implementations.
 */
data class MockBluetoothGattDescriptor(
    override val uuid: UUID,
    override val permissions: Int,
    override val characteristic: IBluetoothGattCharacteristic,
    override var value: ByteArray = byteArrayOf()
) : IBluetoothGattDescriptor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockBluetoothGattDescriptor

        if (uuid != other.uuid) return false
        if (permissions != other.permissions) return false
        if (characteristic != other.characteristic) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + permissions
        result = 31 * result + value.contentHashCode()
        return result
    }

    override fun toString(): String {
        //todo improve
        return uuid.toString() + value + permissions + characteristic.uuid
    }
}
