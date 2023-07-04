package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

internal sealed interface MockRequest {
    val requestId: Int
}

data class MockCharacteristicRead(
    override val requestId: Int,
    val characteristic: IBluetoothGattCharacteristic
) : MockRequest

data class MockCharacteristicWrite(override val requestId: Int, val characteristic: IBluetoothGattCharacteristic) : MockRequest

data class MockDescriptorRead(
    override val requestId: Int,
    val descriptor: IBluetoothGattDescriptor
) : MockRequest

data class MockDescriptorWrite(override val requestId: Int, val descriptor: IBluetoothGattDescriptor) : MockRequest
