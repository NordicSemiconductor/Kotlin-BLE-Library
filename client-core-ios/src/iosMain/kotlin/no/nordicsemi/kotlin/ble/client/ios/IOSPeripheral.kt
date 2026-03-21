package no.nordicsemi.kotlin.ble.client.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.exception.ConnectionFailedException
import no.nordicsemi.kotlin.ble.client.exception.PeripheralNotConnectedException
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.WriteType
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSUUID

class IOSPeripheral(
    scope: CoroutineScope,
    impl: Executor,
    val cbPeripheral: CBPeripheral
): Peripheral<NSUUID, IOSPeripheral.Executor>(scope, impl) {

    suspend fun connect() {
        if (state.value is ConnectionState.Connected) return

        _state.update { ConnectionState.Connecting }
        val result = await(
            action = { impl.connect(autoConnect = false) },
            condition = { it.isConnected || it.isDisconnected },
        )
        when (result) {
            is ConnectionState.Connected -> {
                _state.update { ConnectionState.Connected }
                startCollectingGattEvents(closeWhenDisconnected = true)
            }
            is ConnectionState.Disconnected -> {
                _state.update { result }
                throw ConnectionFailedException(
                    result.reason ?: ConnectionState.Disconnected.Reason.Unknown(0)
                )
            }
            else -> { /* Connecting / Disconnecting — should not happen */ }
        }
    }

    override fun maximumWriteValueLength(type: WriteType): Int {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        val cbWriteType = when (type) {
            WriteType.WITH_RESPONSE -> CBCharacteristicWriteWithResponse
            WriteType.WITHOUT_RESPONSE -> CBCharacteristicWriteWithoutResponse
            WriteType.SIGNED -> throw UnsupportedOperationException("Signed writes are not supported on iOS")
        }
        return cbPeripheral.maximumWriteValueLengthForType(cbWriteType).toInt()
    }

    interface Executor : Peripheral.Executor<NSUUID>
}