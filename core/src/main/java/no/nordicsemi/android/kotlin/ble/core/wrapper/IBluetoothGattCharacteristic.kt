package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

/**
 * Interface representing bluetooth gatt characteristic. It's task is to hide an implementation
 * which can be both [NativeBluetoothGattCharacteristic] (which is a wrapper around native Android
 * [BluetoothGattCharacteristic]) or [MockBluetoothGattCharacteristic]. The role of an interface
 * is to hide a detailed implementation and separate native Android [BluetoothGattCharacteristic]
 * for mock variant as it can't be unit tested.
 */
interface IBluetoothGattCharacteristic {

    /**
     * [UUID] of a characteristic.
     */
    val uuid: UUID

    /**
     * Instance id of a characteristic.
     */
    val instanceId: Int

    /**
     * Not parsed permissions of a characteristic as [Int].
     */
    val permissions: Int

    /**
     * Not parsed properties of a characteristic as [Int].
     */
    val properties: Int

    /**
     * Not parsed write type of a characteristic.
     */
    var writeType: Int

    /**
     * [ByteArray] value of this characteristic.
     */
    var value: ByteArray

    /**
     * List of descriptors of this characteristic.
     */
    val descriptors: List<IBluetoothGattDescriptor>
}
