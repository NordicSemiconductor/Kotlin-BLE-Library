package no.nordicsemi.kotlin.ble.client.ios.remote

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.internal.BaseRemoteDescriptor
import no.nordicsemi.kotlin.ble.client.internal.OperationEvent
import no.nordicsemi.kotlin.ble.core.ios.toNSData
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// TODO: This class is not tested and may have issues with certain edge cases, such as malformed manufacturer data or service data. Consider adding unit tests to verify its correctness.
@OptIn(ExperimentalUuidApi::class)
internal class IOSRemoteDescriptor(
    parent: RemoteCharacteristic,
    private val cbPeripheral: CBPeripheral,
    internal val cbDescriptor: CBDescriptor,
    events: SharedFlow<GattEvent>,
) : BaseRemoteDescriptor(parent, events) {
    override val uuid: Uuid = Uuid.parse(cbDescriptor.UUID.UUIDString)
    override val instanceId: Int = cbDescriptor.hashCode()

    override suspend fun FlowCollector<GattEvent>.executeRead() {
        cbPeripheral.readValueForDescriptor(cbDescriptor)
    }

    override suspend fun FlowCollector<GattEvent>.executeWrite(data: ByteArray) {
        val nsData = data.toNSData()
        cbPeripheral.writeValue(nsData, forDescriptor = cbDescriptor)
    }

    override fun OperationEvent.matches(): Boolean = subject == cbDescriptor
}
