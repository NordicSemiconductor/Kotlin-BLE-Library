package no.nordicsemi.kotlin.ble.client.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.internal.CentralManagerImpl
import no.nordicsemi.kotlin.ble.client.ios.cb.CBCentralManagerDelegate
import no.nordicsemi.kotlin.ble.client.ios.cb.CBCentralManagerEvent
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.ios.IosEnvironment
import platform.CoreBluetooth.CBAdvertisementDataIsConnectable
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private fun CBManagerState.toManagerState(): Manager.State = when (this) {
    CBManagerStatePoweredOn -> Manager.State.POWERED_ON
    CBManagerStatePoweredOff -> Manager.State.POWERED_OFF
    CBManagerStateResetting -> Manager.State.RESETTING
    CBManagerStateUnsupported -> Manager.State.UNSUPPORTED
    CBManagerStateUnauthorized -> Manager.State.POWERED_OFF
    CBManagerStateUnknown -> Manager.State.UNKNOWN
    else -> Manager.State.UNKNOWN
}

internal class IOSCentralManagerImpl(
    scope: CoroutineScope,
    environment: IosEnvironment,
    private val cbCentralManager: CBCentralManager,
) : CentralManagerImpl<
        NSUUID,
        IOSPeripheral,
        IOSPeripheral.Executor,
        IOSScanFilterScope, IOSScanResult
        >(scope, environment),
    IOSCentralManager {
    private val CBCentralManagerDelegate = CBCentralManagerDelegate(
        onError = { _, _ -> }
    )

    private val _state: MutableStateFlow<Manager.State> = MutableStateFlow(
        cbCentralManager.state.toManagerState()
    )
    override val state: StateFlow<Manager.State> = _state.asStateFlow()

    // Maps peripheral identifiers to their executors for routing connection events.
    private val executors = mutableMapOf<NSUUID, IOSNativeExecutor>()

    // Internal flow to broadcast DidDiscover events to scan collectors.
    private val discoverEvents = MutableSharedFlow<CBCentralManagerEvent.DidDiscover>(
        extraBufferCapacity = 64
    )

    init {
        cbCentralManager.delegate = CBCentralManagerDelegate
        // Single consumer for the delegate's Channel, dispatching to appropriate handlers.
        scope.launch {
            for (event in CBCentralManagerDelegate.events) {
                handleCentralManagerEvent(event)
            }
        }
    }

    private fun handleCentralManagerEvent(event: CBCentralManagerEvent) {
        when (event) {
            is CBCentralManagerEvent.StateUpdated -> {
                _state.update { event.state.toManagerState() }
            }
            is CBCentralManagerEvent.DidConnect -> {
                findExecutor(event.peripheral)?.onConnectionStateChanged(ConnectionState.Connected)
            }
            is CBCentralManagerEvent.DidDisconnect -> {
                val reason = mapDisconnectError(event.error)
                findExecutor(event.peripheral)?.onConnectionStateChanged(
                    ConnectionState.Disconnected(reason)
                )
            }
            is CBCentralManagerEvent.DidFailToConnect -> {
                val reason = mapDisconnectError(event.error)
                findExecutor(event.peripheral)?.onConnectionStateChanged(
                    ConnectionState.Disconnected(reason)
                )
            }
            is CBCentralManagerEvent.DidDiscover -> {
                discoverEvents.tryEmit(event)
            }
        }
    }

    private fun findExecutor(cbPeripheral: CBPeripheral): IOSNativeExecutor? {
        return executors[cbPeripheral.identifier]
    }

    private fun getOrCreatePeripheral(cbPeripheral: CBPeripheral): IOSPeripheral {
        return peripheral(cbPeripheral.identifier) {
            val executor = IOSNativeExecutor(scope, cbCentralManager, cbPeripheral)
            executors[cbPeripheral.identifier] = executor
            IOSPeripheral(
                scope = scope,
                impl = executor,
                cbPeripheral = cbPeripheral
            )
        }
    }

    private fun mapDisconnectError(error: platform.Foundation.NSError?): ConnectionState.Disconnected.Reason {
        if (error == null) return ConnectionState.Disconnected.Reason.Success
        return when (error.code.toInt()) {
            6 -> ConnectionState.Disconnected.Reason.LinkLoss
            7 -> ConnectionState.Disconnected.Reason.TerminatePeerUser
            else -> ConnectionState.Disconnected.Reason.Unknown(error.code.toInt())
        }
    }

    override fun getPeripheralsById(ids: List<NSUUID>): List<IOSPeripheral> {
        ensureOpen()
        val cbPeripherals = cbCentralManager.retrievePeripheralsWithIdentifiers(ids)
        @Suppress("UNCHECKED_CAST")
        return (cbPeripherals as List<CBPeripheral>).map { getOrCreatePeripheral(it) }
    }

    override fun scan(
        timeout: Duration,
        filter: IOSScanFilterScope.() -> Unit
    ): Flow<IOSScanResult> = callbackFlow {
        try {
            ensureOpen()
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        if (state.value != Manager.State.POWERED_ON) {
            close(BluetoothUnavailableException())
            return@callbackFlow
        }

        val scanFilter = IOSScanFilter().apply(filter)

        val collectJob = launch {
            discoverEvents.collect { event ->
                val cbPeripheral = event.peripheral
                val advertisingData = IOSAdvertisingData(event.advertisementData)
                val rssi = event.rssi.intValue

                if (!scanFilter.matches(advertisingData, cbPeripheral.name)) return@collect

                val iosPeripheral = getOrCreatePeripheral(cbPeripheral)

                val isConnectable = (event.advertisementData[CBAdvertisementDataIsConnectable] as? NSNumber)
                    ?.boolValue ?: false

                val scanResult = IOSScanResult(
                    peripheral = iosPeripheral,
                    isConnectable = isConnectable,
                    advertisingData = advertisingData,
                    rssi = rssi,
                    txPowerLevel = advertisingData.txPowerLevel,
                    primaryPhy = PrimaryPhy.PHY_LE_1M,
                    secondaryPhy = null,
                    timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong(),
                )
                trySend(scanResult)
            }
        }

        cbCentralManager.scanForPeripheralsWithServices(
            serviceUUIDs = scanFilter.serviceUuids,
            options = null
        )

        if (timeout > 0.milliseconds) {
            launch(Dispatchers.Default) {
                delay(timeout)
                close()
            }
        }

        awaitClose {
            collectJob.cancel()
            cbCentralManager.stopScan()
        }
    }

    override suspend fun connect(peripheral: IOSPeripheral) {
        ensureOpen()
        checkPeripheral(peripheral)

        if (state.value != Manager.State.POWERED_ON) {
            throw BluetoothUnavailableException()
        }

        peripheral.connect()
    }

    override fun monitor(
        timeout: Duration,
        filter: IOSScanFilterScope.() -> Unit
    ): Flow<MonitoringEvent<IOSPeripheral>> {
        TODO("Not yet implemented")
    }

    override fun range(
        peripheral: IOSPeripheral,
        timeout: Duration
    ): Flow<RangeEvent<IOSPeripheral>> {
        TODO("Not yet implemented")
    }

    override fun close() {
        if (!isOpen) return
        super.close()
        cbCentralManager.stopScan()
        cbCentralManager.delegate = null
    }
}
