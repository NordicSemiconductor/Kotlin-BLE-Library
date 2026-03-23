package no.nordicsemi.kotlin.ble.client.ios.remote

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.ios.toKotlinUuid
import no.nordicsemi.kotlin.ble.client.internal.BaseRemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.internal.OperationEvent
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.ios.toNSData
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicPropertyBroadcast
import platform.CoreBluetooth.CBCharacteristicPropertyExtendedProperties
import platform.CoreBluetooth.CBCharacteristicPropertyIndicate
import platform.CoreBluetooth.CBCharacteristicPropertyNotify
import platform.CoreBluetooth.CBCharacteristicPropertyRead
import platform.CoreBluetooth.CBCharacteristicPropertyWrite
import platform.CoreBluetooth.CBCharacteristicPropertyWriteWithoutResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class IOSRemoteCharacteristic(
    parent: AnyRemoteService,
    private val cbPeripheral: CBPeripheral,
    internal val cbCharacteristic: CBCharacteristic,
    events: SharedFlow<GattEvent>,
) : BaseRemoteCharacteristic(parent, events) {
    override val uuid: Uuid = cbCharacteristic.UUID.toKotlinUuid()
    override val instanceId: Int = cbCharacteristic.hashCode()
    override val properties: Set<CharacteristicProperty> = cbCharacteristic.properties.toCharacteristicProperties()

    override val descriptors: List<RemoteDescriptor> =
        (cbCharacteristic.descriptors ?: emptyList<Any>()).map { cbDesc ->
            IOSRemoteDescriptor(
                parent = this,
                cbPeripheral = cbPeripheral,
                cbDescriptor = cbDesc as CBDescriptor,
                events = events,
            )
        }

    override fun setCharacteristicNotification(enabled: Boolean) {
        cbPeripheral.setNotifyValue(enabled, forCharacteristic = cbCharacteristic)
    }

    override suspend fun FlowCollector<GattEvent>.executeRead() {
        cbPeripheral.readValueForCharacteristic(cbCharacteristic)
    }

    override suspend fun FlowCollector<GattEvent>.executeWrite(data: ByteArray, writeType: WriteType) {
        val cbWriteType = when (writeType) {
            WriteType.WITH_RESPONSE -> CBCharacteristicWriteWithResponse
            WriteType.WITHOUT_RESPONSE -> CBCharacteristicWriteWithoutResponse
            WriteType.SIGNED -> throw UnsupportedOperationException("Signed writes are not supported on iOS")
        }
        val nsData = data.toNSData()
        cbPeripheral.writeValue(nsData, forCharacteristic = cbCharacteristic, type = cbWriteType)
    }

    override fun OperationEvent.matches(): Boolean = subject == cbCharacteristic
}

private fun ULong.toCharacteristicProperties(): Set<CharacteristicProperty> = buildSet {
    if (this@toCharacteristicProperties and CBCharacteristicPropertyBroadcast != 0UL)
        add(CharacteristicProperty.BROADCAST)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyRead != 0UL)
        add(CharacteristicProperty.READ)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyWriteWithoutResponse != 0UL)
        add(CharacteristicProperty.WRITE_WITHOUT_RESPONSE)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyWrite != 0UL)
        add(CharacteristicProperty.WRITE)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyNotify != 0UL)
        add(CharacteristicProperty.NOTIFY)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyIndicate != 0UL)
        add(CharacteristicProperty.INDICATE)
    if (this@toCharacteristicProperties and CBCharacteristicPropertyExtendedProperties != 0UL)
        add(CharacteristicProperty.EXTENDED_PROPERTIES)
}
