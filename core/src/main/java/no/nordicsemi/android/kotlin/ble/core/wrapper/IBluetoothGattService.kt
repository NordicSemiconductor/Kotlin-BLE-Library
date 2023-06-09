package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

sealed interface IBluetoothGattService {

    val uuid: UUID

    val type: Int

    val characteristics: List<IBluetoothGattCharacteristic>
}
