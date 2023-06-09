package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

data class MockBluetoothGattService private constructor(
    override val uuid: UUID,
    override val type: Int,
    private var _characteristics: List<IBluetoothGattCharacteristic>
) : IBluetoothGattService {

    constructor(uuid: UUID, type: Int) : this (uuid, type, emptyList())

    override val characteristics: List<IBluetoothGattCharacteristic> = _characteristics

    fun addCharacteristic(descriptor: IBluetoothGattCharacteristic) {
        _characteristics = _characteristics + descriptor
    }
}
