package no.nordicsemi.android.kotlin.ble.core.wrapper

import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.UUID

data class MockBluetoothGattCharacteristic private constructor(
    override val uuid: UUID,
    override val permissions: Int,
    override val properties: Int,
    override val instanceId: Int, //TODO check if instance id should change during copy()
    override var value: ByteArray,
    private var _descriptors: List<IBluetoothGattDescriptor>,
    override var writeType: Int,
) : IBluetoothGattCharacteristic {

    override fun toString(): String {
        return super.toString()
    }

    constructor(uuid: UUID, permissions: Int, properties: Int) : this(
        uuid,
        permissions,
        properties,
        InstanceIdGenerator.nextValue(),
        byteArrayOf(),
        emptyList(),
        BleWriteType.DEFAULT.value
    )

    override val descriptors: List<IBluetoothGattDescriptor>
        get() = _descriptors

    fun addDescriptor(descriptor: IBluetoothGattDescriptor) {
        _descriptors = _descriptors + descriptor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockBluetoothGattCharacteristic

        if (uuid != other.uuid) return false
        if (permissions != other.permissions) return false
        if (properties != other.properties) return false
        if (instanceId != other.instanceId) return false
        if (!value.contentEquals(other.value)) return false
        if (_descriptors != other._descriptors) return false
        if (descriptors != other.descriptors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + permissions
        result = 31 * result + properties
        result = 31 * result + instanceId
        result = 31 * result + value.contentHashCode()
        result = 31 * result + _descriptors.hashCode()
        result = 31 * result + descriptors.hashCode()
        return result
    }
}
