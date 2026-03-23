package no.nordicsemi.kotlin.ble.client.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.RssiRead
import no.nordicsemi.kotlin.ble.client.ServiceDiscoveryFailed
import no.nordicsemi.kotlin.ble.client.RemoteServices
import no.nordicsemi.kotlin.ble.client.ServicesChanged
import no.nordicsemi.kotlin.ble.client.ServicesDiscovered
import no.nordicsemi.kotlin.ble.client.internal.CharacteristicChanged
import no.nordicsemi.kotlin.ble.client.internal.CharacteristicRead
import no.nordicsemi.kotlin.ble.client.internal.CharacteristicWrite
import no.nordicsemi.kotlin.ble.client.internal.DescriptorRead
import no.nordicsemi.kotlin.ble.client.internal.DescriptorWrite
import no.nordicsemi.kotlin.ble.client.ios.cb.CBPeripheralDelegate
import no.nordicsemi.kotlin.ble.client.ios.cb.CBPeripheralEvent
import no.nordicsemi.kotlin.ble.client.ios.remote.IOSRemoteService
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Descriptor
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.ios.toByteArray
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralStateConnected
import platform.CoreBluetooth.CBPeripheralStateDisconnected
import platform.CoreBluetooth.CBService
import platform.Foundation.NSUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class IOSNativeExecutor(
    private val scope: CoroutineScope,
    private val centralManager: CBCentralManager,
    private val cbPeripheral: CBPeripheral,
) : IOSPeripheral.Executor {

    override val identifier: NSUUID
        get() = cbPeripheral.identifier

    override val name: String?
        get() = cbPeripheral.name

    override val initialState: ConnectionState = when (cbPeripheral.state) {
        CBPeripheralStateConnected -> ConnectionState.Connected
        else -> ConnectionState.Disconnected()
    }

    override val initialServices: List<RemoteService> = emptyList()

    private val _events: MutableSharedFlow<GattEvent> = MutableSharedFlow(extraBufferCapacity = 64)
    override val events: SharedFlow<GattEvent> = _events.asSharedFlow()

    override val isClosed: Boolean
        get() = cbPeripheral.state == CBPeripheralStateDisconnected && _closed

    private var _closed: Boolean = false

    // iOS does not support reliable writes, so this is always false.
    override var isReliableWriteEnabled: Boolean = false

    // Service discovery state tracking.
    // iOS requires cascading discovery: services → characteristics → descriptors.
    private var discoveredCBServices: List<CBService> = emptyList()
    private val pendingServiceDiscovery: MutableSet<CBService> = mutableSetOf()
    private val pendingCharacteristicDiscovery: MutableSet<CBCharacteristic> = mutableSetOf()

    private val peripheralDelegate = CBPeripheralDelegate(
        onError = { _, _ -> }
    )

    init {
        cbPeripheral.delegate = peripheralDelegate
        scope.launch {
            for (event in peripheralDelegate.events) {
                handlePeripheralEvent(event)
            }
        }
    }

    private fun handlePeripheralEvent(event: CBPeripheralEvent) {
        when (event) {
            is CBPeripheralEvent.DidDiscoverServices -> handleDiscoverServices(event)
            is CBPeripheralEvent.DidDiscoverCharacteristics -> handleDiscoverCharacteristics(event)
            is CBPeripheralEvent.DidDiscoverDescriptors -> handleDiscoverDescriptors(event)
            is CBPeripheralEvent.DidReadRSSI -> handleReadRSSI(event)
            is CBPeripheralEvent.DidInvalidateServices -> handleInvalidateServices()
            is CBPeripheralEvent.DidUpdateValueForCharacteristic -> handleUpdateValueForCharacteristic(event)
            is CBPeripheralEvent.DidWriteValueForCharacteristic -> handleWriteValueForCharacteristic(event)
            is CBPeripheralEvent.DidUpdateValueForDescriptor -> handleUpdateValueForDescriptor(event)
            is CBPeripheralEvent.DidWriteValueForDescriptor -> handleWriteValueForDescriptor(event)
            is CBPeripheralEvent.DidUpdateNotificationState -> handleUpdateNotificationState(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleDiscoverServices(event: CBPeripheralEvent.DidDiscoverServices) {
        if (event.error != null) {
            _events.tryEmit(ServiceDiscoveryFailed(RemoteServices.Failed.Reason.Unknown(0)))
            return
        }
        val services = cbPeripheral.services as? List<CBService>
        if (services.isNullOrEmpty()) {
            _events.tryEmit(ServiceDiscoveryFailed(RemoteServices.Failed.Reason.EmptyResult))
            return
        }
        discoveredCBServices = services
        pendingServiceDiscovery.clear()
        pendingCharacteristicDiscovery.clear()
        pendingServiceDiscovery.addAll(services)
        for (service in services) {
            cbPeripheral.discoverCharacteristics(null, forService = service)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleDiscoverCharacteristics(event: CBPeripheralEvent.DidDiscoverCharacteristics) {
        pendingServiceDiscovery.remove(event.service)
        if (event.error == null) {
            val characteristics = event.service.characteristics as? List<CBCharacteristic> ?: emptyList()
            for (characteristic in characteristics) {
                pendingCharacteristicDiscovery.add(characteristic)
                cbPeripheral.discoverDescriptorsForCharacteristic(characteristic)
            }
        }
        checkDiscoveryComplete()
    }

    private fun handleDiscoverDescriptors(event: CBPeripheralEvent.DidDiscoverDescriptors) {
        pendingCharacteristicDiscovery.remove(event.characteristic)
        checkDiscoveryComplete()
    }

    private fun checkDiscoveryComplete() {
        if (pendingServiceDiscovery.isNotEmpty() || pendingCharacteristicDiscovery.isNotEmpty()) return
        val services = discoveredCBServices
        if (services.isEmpty()) return
        discoveredCBServices = emptyList()
        val remoteServices = services.map { cbService ->
            IOSRemoteService(cbPeripheral, cbService, _events.asSharedFlow())
        }
        _events.tryEmit(ServicesDiscovered(remoteServices))
    }

    private fun handleReadRSSI(event: CBPeripheralEvent.DidReadRSSI) {
        if (event.error == null) {
            _events.tryEmit(RssiRead(event.rssi.intValue))
        }
    }

    private fun handleInvalidateServices() {
        _events.tryEmit(ServicesChanged)
    }

    private fun handleUpdateValueForCharacteristic(event: CBPeripheralEvent.DidUpdateValueForCharacteristic) {
        val value = event.characteristic.value?.toByteArray() ?: byteArrayOf()
        if (event.error != null) {
            _events.tryEmit(CharacteristicRead(event.characteristic, value, OperationStatus.UNKNOWN_ERROR, 0))
        } else {
            _events.tryEmit(CharacteristicChanged(event.characteristic, value))
            _events.tryEmit(CharacteristicRead(event.characteristic, value, OperationStatus.SUCCESS, 0))
        }
    }

    private fun handleWriteValueForCharacteristic(event: CBPeripheralEvent.DidWriteValueForCharacteristic) {
        val status = if (event.error != null) OperationStatus.UNKNOWN_ERROR else OperationStatus.SUCCESS
        _events.tryEmit(CharacteristicWrite(event.characteristic, status, 0))
    }

    private fun handleUpdateValueForDescriptor(event: CBPeripheralEvent.DidUpdateValueForDescriptor) {
        val value = (event.descriptor.value as? platform.Foundation.NSData)?.toByteArray() ?: byteArrayOf()
        val status = if (event.error != null) OperationStatus.UNKNOWN_ERROR else OperationStatus.SUCCESS
        _events.tryEmit(DescriptorRead(event.descriptor, value, status, 0))
    }

    private fun handleWriteValueForDescriptor(event: CBPeripheralEvent.DidWriteValueForDescriptor) {
        val status = if (event.error != null) OperationStatus.UNKNOWN_ERROR else OperationStatus.SUCCESS
        _events.tryEmit(DescriptorWrite(event.descriptor, status, 0))
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleUpdateNotificationState(event: CBPeripheralEvent.DidUpdateNotificationState) {
        val status = if (event.error != null) OperationStatus.UNKNOWN_ERROR else OperationStatus.SUCCESS
        val cccd = event.characteristic.descriptors
            ?.filterIsInstance<platform.CoreBluetooth.CBDescriptor>()
            ?.firstOrNull { it.UUID.toKotlinUuid() == Descriptor.CLIENT_CHAR_CONF_UUID }

        _events.tryEmit(DescriptorWrite(cccd ?: event.characteristic, status, 0))
    }

    // Called by CentralManager when connection events arrive via FCentralManagerDelegate
    internal fun onConnectionStateChanged(state: ConnectionState) {
        _events.tryEmit(ConnectionStateChanged(state))
    }

    override suspend fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        _closed = false
        centralManager.connectPeripheral(cbPeripheral, options = null)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun discoverServices(uuids: List<Uuid>): Boolean {
        val cbUuids = if (uuids.isEmpty()) {
            null
        } else {
            uuids.map { platform.CoreBluetooth.CBUUID.UUIDWithString(it.toString()) }
        }
        cbPeripheral.discoverServices(cbUuids)
        return true
    }

    override suspend fun readRssi(): Boolean {
        cbPeripheral.readRSSI()
        return true
    }

    override suspend fun disconnect(reason: ConnectionState.Disconnected.Reason): Boolean {
        centralManager.cancelPeripheralConnection(cbPeripheral)
        return true
    }

    override fun close() {
        _closed = true
        centralManager.cancelPeripheralConnection(cbPeripheral)
        cbPeripheral.delegate = null
    }
}
