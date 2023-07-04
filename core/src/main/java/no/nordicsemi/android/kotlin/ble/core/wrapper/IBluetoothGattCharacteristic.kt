package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

interface IBluetoothGattCharacteristic {

    val uuid: UUID
    val instanceId: Int
    val permissions: Int
    val properties: Int
    var writeType: Int
    var value: ByteArray

    val descriptors: List<IBluetoothGattDescriptor>
}
