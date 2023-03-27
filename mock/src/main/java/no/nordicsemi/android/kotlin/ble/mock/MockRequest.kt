package no.nordicsemi.android.kotlin.ble.mock

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

internal sealed interface MockRequest {
    val requestId: Int
}

data class MockCharacteristicRead(
    override val requestId: Int,
    val characteristic: BluetoothGattCharacteristic
) : MockRequest

data class MockCharacteristicWrite(override val requestId: Int, val characteristic: BluetoothGattCharacteristic) : MockRequest

data class MockDescriptorRead(
    override val requestId: Int,
    val descriptor: BluetoothGattDescriptor
) : MockRequest

data class MockDescriptorWrite(override val requestId: Int, val descriptor: BluetoothGattDescriptor) : MockRequest
