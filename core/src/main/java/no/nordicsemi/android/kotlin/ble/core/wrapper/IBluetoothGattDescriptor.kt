package no.nordicsemi.android.kotlin.ble.core.wrapper

import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

/**
 * Interface representing bluetooth gatt descriptor. It's task is to hide an implementation
 * which can be both [NativeBluetoothGattDescriptor] (which is a wrapper around native Android
 * [BluetoothGattDescriptor]) or [MockBluetoothGattDescriptor]. The role of an interface
 * is to hide a detailed implementation and separate native Android [BluetoothGattDescriptor]
 * for mock variant as it can't be unit tested.
 */
interface IBluetoothGattDescriptor {

    /**
     * [UUID] of a descriptor.
     */
    val uuid: UUID

    /**
     * Not parsed permissions of a descriptor as [Int].
     */
    val permissions: Int

    /**
     * [ByteArray] value of this descriptor.
     */
    var value: ByteArray

    /**
     * Parent characteristic of this descriptor. There is a circular dependency between
     * a characteristic and a descriptor which makes usage of an underline data classes tricky.
     */
    val characteristic: IBluetoothGattCharacteristic
}
