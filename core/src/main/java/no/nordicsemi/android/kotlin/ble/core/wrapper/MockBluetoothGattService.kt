package no.nordicsemi.android.kotlin.ble.core.wrapper

import java.util.UUID

/**
 * Mock variant of the service. It's special feature is that it is independent from
 * Android dependencies and can be used for unit testing.
 */
data class MockBluetoothGattService(
    override val uuid: UUID,
    override val type: Int,
    private var _characteristics: List<IBluetoothGattCharacteristic>,
) : IBluetoothGattService {

    override val characteristics: List<IBluetoothGattCharacteristic>
        get() = _characteristics
}
