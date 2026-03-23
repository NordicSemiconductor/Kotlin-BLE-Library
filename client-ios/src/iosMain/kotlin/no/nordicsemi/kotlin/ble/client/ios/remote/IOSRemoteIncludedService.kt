package no.nordicsemi.kotlin.ble.client.ios.remote

import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteIncludedService
import no.nordicsemi.kotlin.ble.client.ios.toKotlinUuid
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
internal class IOSRemoteIncludedService(
    parent: AnyRemoteService,
    private val cbPeripheral: CBPeripheral,
    private val cbService: CBService,
    private val events: SharedFlow<GattEvent>,
) : RemoteIncludedService {
    override val service: AnyRemoteService = parent
    override val uuid: Uuid = cbService.UUID.toKotlinUuid()
    override val instanceId: Int = cbService.hashCode()

    override val characteristics: List<RemoteCharacteristic> =
        (cbService.characteristics ?: emptyList<Any>()).map { cbChar ->
            @Suppress("UNCHECKED_CAST")
            IOSRemoteCharacteristic(
                parent = this,
                cbPeripheral = cbPeripheral,
                cbCharacteristic = cbChar as CBCharacteristic,
                events = events,
            )
        }

    override val includedServices: List<RemoteIncludedService> =
        (cbService.includedServices ?: emptyList<Any>()).map { cbIncluded ->
            @Suppress("UNCHECKED_CAST")
            IOSRemoteIncludedService(
                parent = this,
                cbPeripheral = cbPeripheral,
                cbService = cbIncluded as CBService,
                events = events,
            )
        }

    override fun toString(): String = uuid.toString()
}
