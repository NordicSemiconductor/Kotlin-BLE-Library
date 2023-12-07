package no.nordicsemi.android.kotlin.ble.client.api

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.mutex.RequestedLockedFeature
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.mutex.SharedMutexWrapper

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
        event.toRequestFeature()?.let {
            mutexWrapper.unlock(it)
        }
        _event.tryEmit(event)
    }

    private fun ClientGattEvent.toRequestFeature(): RequestedLockedFeature? {
        return when (this) {
            is ClientGattEvent.MtuChanged -> RequestedLockedFeature.MTU
            is ClientGattEvent.PhyRead -> RequestedLockedFeature.PHY_READ
            is ClientGattEvent.PhyUpdate -> RequestedLockedFeature.PHY_UPDATE
            is ClientGattEvent.CharacteristicRead -> RequestedLockedFeature.CHARACTERISTIC_READ
            is ClientGattEvent.CharacteristicWrite -> RequestedLockedFeature.CHARACTERISTIC_WRITE
            is ClientGattEvent.DescriptorRead -> RequestedLockedFeature.DESCRIPTOR_READ
            is ClientGattEvent.DescriptorWrite -> RequestedLockedFeature.DESCRIPTOR_WRITE
            is ClientGattEvent.ServicesDiscovered -> RequestedLockedFeature.SERVICES_DISCOVERED
            is ClientGattEvent.ReadRemoteRssi -> null.also {
                SharedMutexWrapper.unlock(RequestedLockedFeature.READ_REMOTE_RSSI)
            }
            is ClientGattEvent.CharacteristicChanged -> null
            is ClientGattEvent.ReliableWriteCompleted -> null
            is ClientGattEvent.ServiceChanged -> null
            is ClientGattEvent.ConnectionStateChanged -> null
            is ClientGattEvent.BondStateChanged -> null
        }
    }
}
