package no.nordicsemi.android.kotlin.ble.core.mock

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

internal sealed interface MockRequest {
    val id: Int
}

data class MockCharacteristicRead(
    override val id: Int,
    val characteristic: BluetoothGattCharacteristic
) : MockRequest

data class MockCharacteristicWrite(override val id: Int, val characteristic: BluetoothGattCharacteristic) : MockRequest

data class MockDescriptorRead(
    override val id: Int,
    val descriptor: BluetoothGattDescriptor
) : MockRequest

data class MockDescriptorWrite(override val id: Int, val descriptor: BluetoothGattDescriptor) : MockRequest
