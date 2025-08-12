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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.ble.client.android

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.ReliableWriteScope
import no.nordicsemi.kotlin.ble.client.ServicesChanged
import no.nordicsemi.kotlin.ble.client.android.exception.BondingFailedException
import no.nordicsemi.kotlin.ble.client.android.exception.PeripheralClosedException
import no.nordicsemi.kotlin.ble.client.exception.ConnectionFailedException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.client.exception.PeripheralNotConnectedException
import no.nordicsemi.kotlin.ble.client.exception.ValueDoesNotMatchException
import no.nordicsemi.kotlin.ble.core.ATT_MTU_DEFAULT
import no.nordicsemi.kotlin.ble.core.ATT_MTU_MAX
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionParameters
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.ConnectionState.Disconnected.Reason
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.PeripheralType
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.PhyOption
import no.nordicsemi.kotlin.ble.core.WriteType
import org.slf4j.LoggerFactory
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private typealias AndroidExecutor = no.nordicsemi.kotlin.ble.client.android.Peripheral.Executor

/**
 * Android-specific implementation of a peripheral.
 *
 * This class extends [Peripheral] and adds Android-specific methods.
 *
 * @param scope scope The coroutine scope.
 * @param impl The executor that provides methods to interact with the peripheral.
 */
open class Peripheral(
    scope: CoroutineScope,
    impl: Executor,
): Peripheral<String, AndroidExecutor>(scope, impl) {
    private val logger = LoggerFactory.getLogger(Peripheral::class.java)

    /**
     * An interface that provides methods to interact with the peripheral.
     *
     * The implementation should initiate requests and report events using [events] flow.
     */
    interface Executor: Peripheral.Executor<String> {
        /** MAC address of the device. */
        val address: String
            get() = identifier

        /** The Bluetooth device type of the remote device. */
        val type: PeripheralType

        /** Bonding state as a state flow. */
        val bondState: StateFlow<BondState>

        /**
         * Requests the connection priority to be changed.
         *
         * The result should be reported by emitting [ConnectionParametersChanged] event
         * to [events] flow.
         *
         * @param priority The new connection priority.
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun requestConnectionPriority(priority: ConnectionPriority): Boolean

        /**
         * Requests the MTU (Maximum Transmission Unit) to be set to the given value.
         *
         * The result should be reported by emitting [MtuChanged] event to [events] flow.
         *
         * @param mtu Requested MTU value.
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun requestMtu(mtu: Int): Boolean

        /**
         * Requests the PHY to be changed.
         *
         * The result should be reported by emitting [ConnectionParametersChanged] event
         * to [events] flow.
         *
         * @param txPhy The preferred transmitter PHY.
         * @param rxPhy The preferred receiver PHY.
         * @param phyOptions The preferred coding to use when transmitting on the LE Coded PHY.
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption): Boolean

        /**
         * This method should initiate reading the current PHY parameters.
         *
         * The result should be reported by emitting [PhyChanged] event to [events] flow.
         *
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun readPhy(): Boolean

        /**
         * This method should initiate bonding with the peripheral.
         *
         * This method is guaranteed to be called only when the bond information does not exist.
         *
         * The result should be reported by emitting state [BondState.BONDED] (in case of a success)
         * or [BondState.NONE] (in case of a failure) to [bondState] flow.
         *
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun createBond(): Boolean

        /**
         * This method should initiate removing bond information associated with the peripheral.
         *
         * It is expected, that removing bond information will terminate existing connection.
         *
         * This method is guaranteed to be called only when the bond information exists.
         *
         * The result should be reported by emitting state [BondState.NONE] to [bondState] flow.
         *
         * @return True if removing bond information has been initiated.
         * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
         */
        fun removeBond(): Boolean

        /**
         * Refreshes services cache.
         */
        fun refreshCache(): Boolean
    }

    override val identifier: String = impl.address

    /** MAC address of the peripheral as String, alias for [Peripheral.identifier]. */
    val address: String = impl.address

    /** The Bluetooth device type of the remote device. */
    val type: PeripheralType = impl.type

    /** Connection parameters of the peripheral as state flow. */
    private var _connectionParameters = MutableStateFlow<ConnectionParameters?>(null)
    /** The current connection parameters as [StateFlow]. */
    val connectionParameters = _connectionParameters.asStateFlow()

    /** The current PHY as state flow. */
    private val _phy = MutableStateFlow<PhyInUse?>(null)
    /** The current PHY in use for transmitting and receiving data. */
    val phy = _phy.asStateFlow()

    /** Current MTU (Maximum Transmission Unit) value. */
    private var mtu: Int = ATT_MTU_DEFAULT

    /**
     * MTU can be requested only once.
     *
     * Since Android 14 the system will always request value 517 ignoring the requested value.
     * @see requestHighestValueLength
     */
    private var mtuRequested: Boolean = false

    // Common implementation

    /**
     * Initiates a connection to the peripheral.
     *
     * @param options The connection options.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    internal suspend fun connect(options: CentralManager.ConnectionOptions) {
        // Check if the peripheral isn't already connected or has a pending connection.
        if (state.value is ConnectionState.Connected) {
            return
        }

        // Start connection attempt, based on the connection options.
        logger.trace("Connecting to {} using {}", this, options)
        _state.update { ConnectionState.Connecting }
        when (options) {
            // In case of auto connect, the connection attempt does not time out.
            // Cancel the coroutine to abort.
            is CentralManager.ConnectionOptions.AutoConnect -> {
                impl.connect(true, emptyList())
                try {
                    val state = waitUntil { it.isConnected || it.isDisconnected }
                    when (state) {
                        is ConnectionState.Connected -> {
                            logger.info("Connected to {}", this)
                            _state.update { ConnectionState.Connected }
                            _connectionParameters.update { ConnectionParameters.Unknown }
                            _phy.update { PhyInUse.PHY_LE_1M }
                            // Since we're connected, let's start collecting GATT events, including
                            // connection state changes. The device may disconnect and reconnect at
                            // any time. To stop collecting the events one needs to call disconnect().
                            startCollectingGattEvents(closeWhenDisconnected = false)
                            if (options.automaticallyRequestHighestValueLength) {
                                mtuRequested = true
                            }
                            initiateConnection()
                        }
                        is ConnectionState.Disconnected -> {
                            // RPA (Resolvable Private Address) can rotate, causing address to "expire" in the
                            // background connection list. RPA is allowed for direct connect, as such request
                            // times out after 30 seconds.
                            //
                            // Android returns status 133 or 135 when the address is not supported
                            // in background connection. It is possible to connect to such device using
                            // direct connection.
                            //
                            // See: https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Bluetooth/system/stack/gatt/gatt_api.cc;l=1450
                            val reason = state.reason!!
                            if (reason is Reason.Unknown && (reason.status == 133 || reason.status == 135)) {
                                logger.warn("Connection attempt failed (reason: {})", Reason.UnsupportedAddress)
                                _state.update { ConnectionState.Disconnected(Reason.UnsupportedAddress) }
                                throw ConnectionFailedException(Reason.UnsupportedAddress)
                            }
                            logger.warn("Connection attempt failed (reason: {})", reason)
                            _state.update { state }
                            throw ConnectionFailedException(reason)
                        }
                        else -> {}
                    }
                } catch (e: TimeoutCancellationException) {
                    // Although the connection using AutoConnect does not time out on its own,
                    // it's still possible to wrap it in withTimeout. Report this as a timeout, not cancellation.
                    logger.warn(e.message)
                    _state.update { ConnectionState.Disconnected(Reason.Timeout(e.timeout ?: Duration.ZERO)) }
                    close()
                    throw e
                } catch (e: CancellationException) {
                    logger.warn("Connection attempt cancelled")
                    _state.update { ConnectionState.Disconnected(Reason.Cancelled) }
                    close()
                    throw e
                }
            }

            // Direct connection gives more options to configure the connection.
            is CentralManager.ConnectionOptions.Direct -> {
                impl.connect(false, options.preferredPhy)
                try {
                    val state = waitUntil(options.timeout) { it.isConnected || it.isDisconnected }
                    when (state) {
                        is ConnectionState.Connected -> {
                            logger.info("Connected to {}", this)
                            _state.update { ConnectionState.Connected }
                            _connectionParameters.update { ConnectionParameters.Unknown }
                            _phy.update {  PhyInUse.PHY_LE_1M }
                            // Since we're connected, let's start collecting GATT events.
                            // In case of a direct connection, a disconnection will cancel
                            // event collection and close the peripheral.
                            startCollectingGattEvents()
                            if (options.automaticallyRequestHighestValueLength) {
                                mtuRequested = true
                            }
                            initiateConnection()
                        }
                        is ConnectionState.Disconnected -> {
                            check(options.retry > 0) {
                                val reason = state.reason!!
                                logger.warn("Connection attempt failed (reason: {})", reason)
                                _state.update { state }
                                throw ConnectionFailedException(reason)
                            }
                            logger.warn("Connection attempt failed (reason: {}), retrying in {}...",
                                state.reason, options.retryDelay)
                            delay(options.retryDelay)
                            connect(options.copy(retry = options.retry - 1))
                        }
                        else -> {}
                    }
                } catch (e: TimeoutCancellationException) {
                    logger.warn("Connection attempt timed out after {}", options.timeout)
                    _state.update { ConnectionState.Disconnected(Reason.Timeout(options.timeout)) }
                    close()
                    throw e
                } catch (e: CancellationException) {
                    logger.warn("Connection attempt cancelled")
                    _state.update { ConnectionState.Disconnected(Reason.Cancelled) }
                    close()
                    throw e
                }
            }
        }
    }

    override suspend fun handle(event: GattEvent) = when (event) {
        is MtuChanged -> mtu = event.mtu
        is PhyChanged -> _phy.update { event.phy }
        is ConnectionParametersChanged -> _connectionParameters.update { event.newParameters }
        is ConnectionStateChanged -> {
            if (event.isPhyRequestError) {
                super.handle(ConnectionStateChanged(ConnectionState.Disconnected(reason = Reason.UnsupportedConfiguration)))
            } else {
                super.handle(event)
            }
        }
        else -> super.handle(event)
    }

    override suspend fun initiateConnection() {
        // Request high MTU before service discovery.
        if (mtuRequested) {
            try {
                requestHighestValueLength()
            } catch (_: PeripheralNotConnectedException) {
                // Skip service discovery if the peripheral got disconnected.
                return
            } catch (e: Exception) {
                logger.warn("Failed to request MTU: {}", e.message)
            }
        }
        // Super implementation will start service discovery it the services are observed.
        super.initiateConnection()
    }

    override fun handleDisconnection() {
        super.handleDisconnection()
        mtu = ATT_MTU_DEFAULT
        _phy.update { null }
        _connectionParameters.update { null }
        // Note!
        // Do not reset the mtuRequested flag here. MTU will be requested again once
        // the device gets connected, or will be cleared when the peripheral is closed, below.
    }

    override fun handleClose() {
        mtuRequested = false
    }

    /**
     * Read the current transmitter PHY and receiver PHY of the connection.
     *
     * PHY LE 2M or PHY Coded is supported since Android 8.0 (API level 26) or later.
     *
     * @return The PHY in use for transmitting and receiving data.
     * @throws PeripheralNotConnectedException If the device is not connected.
     * @throws OperationFailedException If PHY could not be read.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun readPhy(): PhyInUse {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        logger.trace("Reading PHY")
        return impl.events
            .onSubscription {
                if (!impl.readPhy()) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .takeWhile { !it.isDisconnectionEvent }
            .filterIsInstance(PhyChanged::class)
            .firstOrNull()?.phy
            ?.also { logger.info("PHY read: {}", it) }
            ?: throw PeripheralNotConnectedException()
    }

    /**
     * Set the preferred connection PHY.
     *
     * PHY LE 2M or PHY Coded is supported since Android 8.0 (API level 26) or later.
     * Other devices will continue to use the only PHY they support, that is [Phy.PHY_LE_1M].
     *
     * Please note that this is just a recommendation, whether the PHY change will happen depends
     * on other applications preferences, local and remote controller capabilities.
     * Controller can override these settings.
     *
     * @param txPhy The preferred transmitter PHY.
     * @param rxPhy The preferred receiver PHY.
     * @param phyOptions The preferred coding to use when transmitting on the LE Coded PHY.
     * @return The PHYs in use after the change.
     * @throws PeripheralNotConnectedException If the device is not connected.
     * @throws OperationFailedException If PHY change could not be requested.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun setPreferredPhy(
        txPhy: Phy,
        rxPhy: Phy = txPhy,
        phyOptions: PhyOption = PhyOption.NO_PREFERRED,
    ): PhyInUse {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        logger.trace("Setting preferred PHY: tx={}, rx={}, options={}", txPhy, rxPhy, phyOptions)
        return impl.events
            .onSubscription {
                if (!impl.requestPhy(txPhy, rxPhy, phyOptions)) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .takeWhile { !it.isDisconnectionEvent }
            .filterIsInstance(PhyChanged::class)
            .firstOrNull()?.phy
            ?.also { logger.info("PHY changed to: {}", it) }
            ?: throw PeripheralNotConnectedException()
    }

    /**
     * The maximum amount of data, in bytes, you can send to a characteristic in a single write
     * operation.
     *
     * Maximum size for [WriteType.WITH_RESPONSE] type is *512 bytes* or to *ATT MTU - 5 bytes*
     * when writing reliably (see [usingReliableWrite]).
     * For [WriteType.WITHOUT_RESPONSE] it is equal to *ATT MTU - 3 bytes*.
     *
     * Higher values of ATT MTU for [WriteType.WITHOUT_RESPONSE] can be requested
     * using [requestHighestValueLength].
     *
     * @throws PeripheralNotConnectedException If the device is not connected.
     */
    override fun maximumWriteValueLength(type: WriteType): Int {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        // TODO Return "mtu - 5" when in Reliable Write mode
        return when (type) {
            WriteType.WITH_RESPONSE -> 512
            WriteType.WITHOUT_RESPONSE -> min(mtu - 3, 512)
            WriteType.SIGNED -> mtu - 12
        }
    }

    /**
     * Requests the highest possible MTU ([517][ATT_MTU_MAX]).
     *
     * The MTU will be automatically requested when the peripheral is reconnected
     * when connected using [AutoConnect][CentralManager.ConnectionOptions.AutoConnect].
     *
     * The MTU is negotiated between the client and the server and set to the highest value supported
     * by both devices.
     *
     * Although it was possible to request any MTU from 23 to 517, since Android 14 the
     * system will always request value 517 ignoring the requested value. Hence, this method
     * does not allow to set the MTU value.
     *
     * #### Important
     * It is known that some Android devices (i.e. Samsung Galaxy Tab A8) fail to negotiate
     * LL MTU (packet size on the Link Layer) using Data Length Extension (DLE). They mistakenly
     * claim supporting only 27 bytes of TX, but then try to send up to 251 bytes, causing the
     * connection to terminate. For such devices it is recommended not to request higher MTU
     * or never sending more than 20 bytes in a single write operation.
     *
     * @throws PeripheralNotConnectedException If the device is not connected.
     * @throws OperationFailedException If MTU could not be requested.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun requestHighestValueLength() {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        if (mtu > ATT_MTU_DEFAULT) {
            logger.warn("MTU has been already requested")
            return
        }
        mtuRequested = true
        logger.trace("Requesting MTU: {}", ATT_MTU_MAX)
        impl.events
            .onSubscription {
                if (!impl.requestMtu(ATT_MTU_MAX)) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .takeWhile { !it.isDisconnectionEvent }
            .filterIsInstance(MtuChanged::class)
            .firstOrNull()?.mtu
            ?.also { logger.info("MTU set to {}", it) }
            ?: throw PeripheralNotConnectedException()
    }

    /**
     * Requests new connection parameters.
     *
     * Android API does not allow to set custom connection parameters. Instead, predefined
     * connection priorities can be requested. Corresponding values for each priority are
     * may differ between Android versions and devices.
     *
     * On Android versions prior to Android 8 (Oreo) the updated connection parameters are not
     * returned to the app, therefore the returned value will be [ConnectionParameters.Unknown].
     *
     * @param priority The new connection priority.
     * @return The new connection parameters.
     * @throws PeripheralNotConnectedException If the device is not connected.
     * @throws OperationFailedException If connection priority could not be requested.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun requestConnectionPriority(priority: ConnectionPriority): ConnectionParameters {
        check(isConnected) {
            throw PeripheralNotConnectedException()
        }
        logger.trace("Requesting connection priority: {}", priority)
        return impl.events
            .onSubscription {
                if (!impl.requestConnectionPriority(priority)) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .takeWhile { !it.isDisconnectionEvent }
            .filterIsInstance(ConnectionParametersChanged::class)
            .firstOrNull()?.newParameters
            ?.also { logger.info("Connection parameters updated: {}", it) }
            ?: throw PeripheralNotConnectedException()
    }

    /**
     * Initiates a reliable write transaction for a given characteristic.
     *
     * The purpose of queued writes is to queue up writes of values of multiple
     * attributes in a first-in first-out queue and then execute the write on all of them in
     * a single atomic operation.
     *
     * Use [ReliableWriteScope.writeReliably] to write the value of supported characteristic
     * and descriptors reliably.
     *
     * All queued writes will be executed in a single atomic operation.
     * If any of the write operations throws [ValueDoesNotMatchException], the whole transaction
     * will be aborted.
     *
     * @param operations The lambda that will be called to queue the writes.
     */
    suspend fun usingReliableWrite(operations: suspend ReliableWriteScope.() -> Unit) {
        TODO()
    }

    /**
     * Refreshes the cached GATT database associated with the peripheral and starts new service
     * discovery automatically.
     *
     * All observers subscribed to invalidated attributes will be cancelled. The flows returned
     * by [services] will emit an empty list of services following by updated list of services
     * when the new service discovery is complete.
     *
     * It is safe to call this method when the peripheral is connected, connecting, or disconnecting.
     * It may be called when the device is disconnected but only when the connection was made using
     * [AutoConnect][CentralManager.ConnectionOptions.AutoConnect] option in which case the system
     * is trying to reconnect.
     *
     * A connection made using [Direct][CentralManager.ConnectionOptions.Direct] option closes
     * automatically immediately after disconnection.
     *
     * When invoked on a closed connection the method throws [PeripheralClosedException].
     *
     * @throws PeripheralClosedException If the peripheral is closed.
     * @throws OperationFailedException If cache could not be refreshed.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun refreshCache() {
        check(!impl.isClosed) {
            throw PeripheralClosedException()
        }
        logger.trace("Refreshing cache")
        impl.events
            .onSubscription {
                if (!impl.refreshCache()) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .first { it == ServicesChanged }
            .also { logger.info("Cache refreshed") }
    }

    /**
     * Initiates bonding with the peripheral.
     *
     * @throws BondingFailedException If bonding failed.
     * @throws OperationFailedException If bonding could not be started.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun createBond() {
        if (hasBondInformation) {
            return
        }
        logger.trace("Creating bond")
        impl.bondState
            .onSubscription {
                if (!impl.createBond()) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            // Skip the initial state. It should transition to BONDING quickly.
            .dropWhile { it == BondState.NONE }
            // Now, await for the next state after BONDING.
            .first { it != BondState.BONDING }
            // And process it.
            .also {
                when (it) {
                    BondState.BONDED -> logger.info("Bond created")
                    BondState.NONE -> {
                        logger.warn("Bonding failed")
                        throw BondingFailedException()
                    }
                    else -> { /* Not possible */ }
                }
            }
    }

    /**
     * Removes the bond information associated with the peripheral.
     *
     * This method will disconnect the peripheral it it was connected.
     *
     * @throws OperationFailedException If bond information could not be removed.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    suspend fun removeBond() {
        if (!hasBondInformation) {
            return
        }
        logger.trace("Removing bond information")
        impl.bondState
            .onSubscription {
                if (!impl.removeBond()) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            .first { it == BondState.NONE }
            .also { logger.info("Bond information removed") }
    }

    /**
     * Returns whether the system has bond information associated with this peripheral.
     *
     * #### Security Note
     * Having a bond information does not guarantee that the connection to the device is secure.
     * Android should terminate the connection to a device for which it has a bond information
     * if encryption cannot be resumed, but some devices don't do that. Instead, the bond state
     * is set to [BondState.BONDED] and the connection is kept open without any security.
     */
    val hasBondInformation: Boolean
        get() = bondState.value == BondState.BONDED

    /**
     * Returns the current bond state as [StateFlow].
     *
     * #### Security Note
     * State [BondState.BONDED] does not guarantee that the connection to the device is secure.
     * Some Android devices allow connecting to bonded devices without restoring encryption
     * or they remove the bond information when it fails.
     */
    val bondState: StateFlow<BondState>
        get() = impl.bondState

    /**
     * A helper property to extract the timeout from the [TimeoutCancellationException] message.
     */
    private val TimeoutCancellationException.timeout: Duration?
        get() = message?.let { message ->
            val regex = Regex("""\d+""")
            val match = regex.find(message)
            return match?.value?.toLongOrNull()?.milliseconds
        }

    /**
     * Checks if the disconnection event is caused by a PHY request error.
     *
     * Samsung S8 with Android 9 fails to reconnect to a peripheral that requested PHY LE 2M
     * immediately after establishing the connection. It replies with PhyResponse with Instant
     * from the past, causing the peripheral to drop the connection.
     */
    private val ConnectionStateChanged.isPhyRequestError: Boolean
        get() = (newState as? ConnectionState.Disconnected)?.let {
            // Returned error is 0x08 (TIMEOUT).
            it.reason == Reason.LinkLoss &&
            // This happens before the services are discovered, so the services list is empty,
            _services.value.isNullOrEmpty() &&
            // ...but after the app is notified about change to PHY LE 2M.
            phy.value?.txPhy == Phy.PHY_LE_2M
        } ?: false
}