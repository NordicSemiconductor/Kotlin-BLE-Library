package no.nordicsemi.android.kotlin.ble.client.api

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper

class ClientMutexHandleCallback(
    bufferSize: Int,
    private val mutexWrapper: MutexWrapper,
) {

    private val _event = MutableSharedFlow<ClientGattEvent>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event: SharedFlow<ClientGattEvent> = _event.asSharedFlow()

    fun tryEmit(event: ClientGattEvent) {
        if (
            event is ClientGattEvent.MtuChanged ||
            event is ClientGattEvent.PhyRead ||
            event is ClientGattEvent.PhyUpdate ||
            event is ClientGattEvent.ReadRemoteRssi ||
            event is ClientGattEvent.DescriptorWrite ||
            event is ClientGattEvent.DescriptorRead ||
            event is ClientGattEvent.CharacteristicWrite ||
            event is ClientGattEvent.CharacteristicRead ||
            event is ClientGattEvent.ServicesDiscovered
        ) {
            mutexWrapper.unlock()
        }
        _event.tryEmit(event)
    }
}
