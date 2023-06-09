package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

interface IBluetoothGattDescriptor {
    val uuid: UUID
    val permissions: Int
    var value: ByteArray
    val characteristic: IBluetoothGattCharacteristic
}
