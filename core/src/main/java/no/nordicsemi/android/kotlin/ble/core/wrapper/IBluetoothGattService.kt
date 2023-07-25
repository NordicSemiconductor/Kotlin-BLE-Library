package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattService
import java.util.UUID

/**
 * Interface representing bluetooth gatt service. It's task is to hide an implementation
 * which can be both [NativeBluetoothGattService] (which is a wrapper around native Android
 * [BluetoothGattService]) or [MockBluetoothGattService]. The role of an interface
 * is to hide a detailed implementation and separate native Android [BluetoothGattService]
 * for mock variant as it can't be unit tested.
 */
sealed interface IBluetoothGattService {

    /**
     * [UUID] of a service.
     */
    val uuid: UUID

    /**
     * Not parsed type of a service.
     */
    val type: Int

    /**
     * List of characteristics of this service.
     */
    val characteristics: List<IBluetoothGattCharacteristic>
}
