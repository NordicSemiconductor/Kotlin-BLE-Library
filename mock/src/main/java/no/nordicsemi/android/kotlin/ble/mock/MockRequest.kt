package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

internal sealed interface MockRequest {
    val requestId: Int
}

internal sealed interface SendResponseRequest : MockRequest

internal data class MockCharacteristicRead(
    override val requestId: Int,
    val characteristic: IBluetoothGattCharacteristic,
) : SendResponseRequest

internal data class MockCharacteristicWrite(
    override val requestId: Int,
    val characteristic: IBluetoothGattCharacteristic,
) : SendResponseRequest

internal data class MockDescriptorRead(
    override val requestId: Int,
    val descriptor: IBluetoothGattDescriptor,
) : SendResponseRequest

internal data class MockDescriptorWrite(
    override val requestId: Int,
    val descriptor: IBluetoothGattDescriptor,
) : SendResponseRequest

internal sealed interface ReliableWriteRequest : MockRequest

internal data class MockExecuteReliableWrite(
    override val requestId: Int,
) : ReliableWriteRequest

internal data class MockAbortReliableWrite(
    override val requestId: Int,
) : ReliableWriteRequest
