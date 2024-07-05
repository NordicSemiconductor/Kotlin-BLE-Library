/*
 * Copyright (c) 2024, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.client

import org.slf4j.LoggerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import no.nordicsemi.kotlin.ble.client.exception.PeripheralNotConnectedException
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Peer
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.WriteType
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Class representing a Bluetooth LE peripheral.
 *
 * @property scope The coroutine scope.
 * @property impl The executor that provides methods to interact with the peripheral.
 * @property name The friendly Bluetooth name of the remote device.
 * The local adapter will automatically retrieve remote names when performing a device scan,
 * and will cache them. This method just returns the name for this device from the cache
 * @property state The connection state of the peripheral, as [StateFlow]. The flow emits a new
 * value whenever the state of the peripheral changes.
 */
abstract class GenericPeripheral<ID, EX: GenericPeripheral.GenericExecutor<ID>>(
    protected val scope: CoroutineScope,
    protected val impl: EX,
): Peer<ID> {
    private val logger = LoggerFactory.getLogger(GenericPeripheral::class.java)

    val name: String?
        get() = impl.name

    override val identifier: ID
        get() = impl.identifier

    /**
     * A job that collects GATT events from the peripheral.
     *
     * This is not-null when the device is connected or was connected using auto connect,
     * that is when any GATT event for the device, including connection state change, is expected.
     */
    private var gattEventCollector: Job? = null

    /** The current state of the peripheral as a state flow. */
    protected var _state = MutableStateFlow(impl.initialState)
    val state = _state.asStateFlow()

    /** Current list of GATT services. */
    private var _services = MutableStateFlow(impl.initialServices)

    /**
     * A flag indicating that the services have been discovered.
     *
     * This flag is reset on disconnection and when services are invalidated.
     */
    private var servicesDiscovered = false

    /**
     * An interface that provides methods to interact with the peripheral.
     *
     * The implementation should initiate requests and report events using [events] flow.
     */
    interface GenericExecutor<ID> {
        /** The peripheral identifier. */
        val identifier: ID

        /**
         * The name of the device, if available.
         *
         * The name may change during the lifetime of the peripheral.
         */
        val name: String?

        /** The initial state of the peripheral. */
        val initialState: ConnectionState

        /** The initial services of the peripheral. */
        val initialServices: List<RemoteService>

        /** A flow of GATT events from the peripheral. */
        val events: Flow<GattEvent>

        /**
         * Makes a connection to the peripheral.
         *
         * @param autoConnect True to use auto connect feature, false to use direct connection.
         * @param preferredPhy The preferred PHYs for connection.
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun connect(autoConnect: Boolean, preferredPhy: List<Phy> = listOf(Phy.PHY_LE_1M))

        /**
         * Discovers services on the peripheral.
         */
        fun discoverServices()

        /**
         * Initiates a read of the RSSI value from the peripheral.
         *
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun readRssi()

        /**
         * Disconnects from the peripheral.
         *
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun disconnect()

        /**
         * Closes the connection to the peripheral.
         *
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun close()
    }

    /**
     * Returns `true` if the peripheral is currently connected.
     */
    val isConnected: Boolean
        get() = state.value is ConnectionState.Connected

    /**
     * Returns `true` if the peripheral is disconnected of getting disconnected.
     */
    val isDisconnected: Boolean
        get() = state.value is ConnectionState.Disconnected || state.value is ConnectionState.Disconnecting

    /**
     * Waits until the given condition is met.
     *
     * @param timeout The timeout, or [Duration.INFINITE] (default) to wait indefinitely.
     * @param condition The condition to meet, which takes the current state as an argument.
     * @throws TimeoutCancellationException If the timeout is set and the condition is not met.
     */
    protected suspend fun waitUntil(
        timeout: Duration = Duration.INFINITE,
        condition: suspend (ConnectionState) -> Boolean
    ): ConnectionState = withTimeout(timeout) {
        impl.events
            .filterIsInstance(ConnectionStateChanged::class)
            .map { it.newState }
            .filter(condition)
            .first()
    }

    /**
     * Starts collecting GATT events from the peripheral.
     *
     * The implementation may want to continue collecting events after the peripheral disconnects.
     * This may be the case when the connection may automatically get reestablished when the
     * peripheral is again in range. In such cases, the [closeWhenDisconnected] parameter should
     * be set to `false`.
     *
     * @param closeWhenDisconnected True to close the connection when the peripheral disconnects;
     *        false to keep collecting events.
     */
    protected fun startCollectingGattEvents(closeWhenDisconnected: Boolean = true) {
        gattEventCollector?.cancel()
        gattEventCollector = impl.events
            .onEach { handle(it) }
            .filterIsInstance(ConnectionStateChanged::class)
            .filter { closeWhenDisconnected && it.newState is ConnectionState.Disconnected }
            .onEach { close() }
            .launchIn(scope)
    }

    /**
     * Handles GATT events.
     *
     * @param event The GATT event to process.
     */
    protected open fun handle(event: GattEvent) = when (event) {
        is ConnectionStateChanged -> {
            if (event.newState is ConnectionState.Disconnected) {
                handleDisconnection()
            }
            _state.update { event.newState }
        }
        ServicesChanged -> {
            logger.trace("Services invalidated")
            invalidateServices()
            discoverServices()
        }
        is ServicesDiscovered -> _services.update {
            // Assign the owner to each service, making them valid.
            event.services.onEach { it.owner = this }
        }
        else -> { /* Ignore */ }
    }

    /**
     * This method is called when the connection to the peripheral was terminated.
     *
     * It should reset data associated with the connection.
     */
    protected open fun handleDisconnection() {
        invalidateServices()
    }

    /**
     * Invalidates current GATT services.
     */
    private fun invalidateServices() {
        _services.value.onEach { it.owner = null }
        _services.update { emptyList() }
        servicesDiscovered = false
    }

    /**
     * Initiates service discovery on the peripheral.
     *
     * This method does nothing if [servicesDiscovered] is `true`.
     */
    private fun discoverServices() {
        if (!servicesDiscovered) {
            servicesDiscovered = true
            logger.trace("Discovering services")
            impl.discoverServices()
        }
    }

    /**
     * Cancels collection of GATT events and closes the connection.
     */
    protected fun close() {
        gattEventCollector?.cancel()
        gattEventCollector = null
        handleDisconnection()
        impl.close()
    }

    /**
     * Returns a flow with a list of services discovered on the device.
     *
     * Initially, the flow will emit an empty list. The list will be updated when the services
     * are discovered. The flow will be closed when the device disconnects.
     *
     * @param uuids An optional list of service UUID to filter the results. If empty, all services
     *        will be returned. Some platforms may do partial service discovery and return only
     *        services with given UUIDs.
     */
    fun services(uuids: List<UUID> = emptyList()): StateFlow<List<RemoteService>> {
        check (isConnected) {
            throw PeripheralNotConnectedException()
        }
        // First call to this method triggers service discovery.
        discoverServices()

        // If there is no filter, return the original flow.
        if (uuids.isEmpty()) {
            return _services.asStateFlow()
        }

        // A method to filter services by UUIDs.
        fun List<RemoteService>.filterBy(uuids: List<UUID>): List<RemoteService> {
            return filter { service -> uuids.any { it == service.uuid } }
        }

        // If there is a filter, create a new flow that will emit filtered services only.
        val filteredServices = _services.value.filterBy(uuids)
        return MutableStateFlow(filteredServices)
            .apply {
                // Each time the list of discovered services changes, filter it by `uuids`
                // and populate the flow.
                _services.onEach { allServices ->
                    update { allServices.filterBy(uuids) }
                }
            }
    }

    /**
     * The maximum amount of data, in bytes, you can send to a characteristic in a single write
     * request.
     *
     * Maximum size for [WriteType.WITH_RESPONSE] type is *512 bytes* or to *ATT MTU - 5 bytes*
     * when writing reliably (see [ReliableWriteScope]).
     * For [WriteType.WITHOUT_RESPONSE] it is equal to *ATT MTU - 3 bytes*.
     *
     * The ATT MTU value can be negotiated during the connection setup.
     *
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     */
    abstract fun maximumWriteValueLength(type: WriteType): Int

    /**
     * Reads the received signal strength indicator (RSSI) of the peripheral.
     *
     * Usually, the RSSI value is between -120 dBm (vary far) and -30 dBm (very close),
     * but the exact value depends on the TX power, antenna, environment, and other factors.
     *
     * @return The RSSI value in dBm.
     * @throws PeripheralNotConnectedException if the peripheral is not connected.
     */
    suspend fun readRssi(): Int {
        check (isConnected) {
            throw PeripheralNotConnectedException()
        }
        logger.trace("Reading RSSI")
        impl.readRssi()
        return impl.events
            .takeWhile { !it.isDisconnectionEvent }
            .filterIsInstance(RssiRead::class)
            .firstOrNull()?.rssi
            ?.also {
                logger.info("RSSI read: {} dBm", it)
            }
            ?: throw PeripheralNotConnectedException()
    }

    /**
     * Disconnects the client from the peripheral.
     *
     * Note, that calling this method does not guarantee that the peripheral will disconnect;
     * other clients, also in other applications, may still be connected to the peripheral.
     *
     * This method does nothing if the peripheral is already disconnected.
     *
     * Hint: Use [GenericCentralManager.connect] to connect to the peripheral.
     */
    suspend fun disconnect() {
        // Check if the peripheral isn't already disconnected or has a pending disconnection.
        state.value.let { currentState ->
            if (currentState is ConnectionState.Disconnected) {
                return
            }
            if (currentState is ConnectionState.Disconnecting) {
                waitUntil { it is ConnectionState.Disconnected }
                return
            }
        }

        // Disconnect from the peripheral.
        logger.trace("Disconnecting from {}", this)
        _state.update { ConnectionState.Disconnecting }
        impl.disconnect()
        try {
            waitUntil(500.milliseconds) { it is ConnectionState.Disconnected }
        } catch (e: TimeoutCancellationException) {
            logger.warn("Disconnection takes longer than expected, closing")
        }
        close()
        logger.trace("Disconnected from {}", this)
    }
}