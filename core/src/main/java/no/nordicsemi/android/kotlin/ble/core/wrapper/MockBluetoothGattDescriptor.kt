package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

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
        result = 31 * result + characteristic.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
