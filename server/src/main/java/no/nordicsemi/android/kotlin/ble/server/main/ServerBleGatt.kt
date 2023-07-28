/*
 * Copyright (c) 2023, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.server.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.core.ApplicationScope
import no.nordicsemi.android.common.logger.BleLogger
import no.nordicsemi.android.common.logger.DefaultConsoleLogger
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.core.utils.simpleSharedFlow
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.*
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServiceFactory
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattDescriptor
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattFactory
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattService
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServices
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection

/**
 * A class for managing BLE connections. It propagates events ([ServerGattEvent]) to specified connection's
 * corresponding characteristics ([ServerBleGattCharacteristic]) and descriptors ([ServerBleGattDescriptor]).
 * Thanks to that values are getting updated.
 *
 * @property server [GattServerAPI] for communication with a client devices.
 * @property logger Logger instance for displaying logs.
 */
class ServerBleGatt internal constructor(
    private val server: GattServerAPI,
    private val logger: BleLogger,
) {

    companion object {

        /**
         * Declares and starts server based on [config]. It can be either real BLE server or mocked
         * variant which run locally on a device.
         *
         * @param context An application context.
         * @param config Service config which is used later to create services per each connection.
         * @param logger Logger instance for displaying logs.
         * @param mock A mock device if run as a mocked variant.
         * @return An instance of [ServerBleGatt]
         */
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        suspend fun create(
            context: Context,
            vararg config: ServerBleGattServiceConfig,
            logger: BleLogger = DefaultConsoleLogger(context),
            mock: MockServerDevice? = null,
        ): ServerBleGatt {
            return ServerBleGattFactory.create(context, logger, *config, mock = mock)
        }
    }

    private val _onNewConnection = simpleSharedFlow<ServerBluetoothGattConnection>()

    /**
     * [Flow] which emits each time a new connection is established.
     * It can be used to set up new connection's services behaviour.
     */
    val onNewConnection = _onNewConnection.asSharedFlow()

    private val _connections =
        MutableStateFlow(mapOf<ClientDevice, ServerBluetoothGattConnection>())

    /**
     * [Flow] which emits collected connections as a [Map] each time a new connection is established.
     */
    val connections = _connections.asStateFlow()

    private var services: List<IBluetoothGattService> = emptyList()

    init {
        server.event.onEach {
            logger.log(Log.VERBOSE, "On gatt event: $it")
            when (it) {
                is ServiceAdded -> onServiceAdded(it.service, it.status)
                is ClientConnectionStateChanged -> onConnectionStateChanged(
                    it.device, it.status, it.newState
                )

                is ServiceEvent -> connections.value[it.device]?.services?.onEvent(it)
                is ServerPhyRead -> onPhyRead(it)
                is ServerPhyUpdate -> onPhyUpdate(it)
                is ServerMtuChanged -> onMtuChanged(it)
            }
        }.launchIn(ApplicationScope)
    }

    /**
     * Stops server. All connections will be dropped.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopServer() {
        logger.log(Log.INFO, "Stopping server")
        server.close()
    }

    /**
     * Cancels connection with a particular device.
     *
     * @param device A client device.
     */
    fun cancelConnection(device: ClientDevice) {
        logger.log(Log.INFO, "Cancelling connection with client: ${device.address}")
        server.cancelConnection(device)
    }

    /**
     * Callback informing that the client's connection has been changed.
     *
     * @param device A client device which has connected/disconnected.
     * @param status Status of the operation.
     * @param newState New connection state.
     */
    private fun onConnectionStateChanged(
        device: ClientDevice,
        status: BleGattConnectionStatus,
        newState: GattConnectionState,
    ) {
        logger.log(
            Log.INFO,
            "Connection changed, device: ${device.address}, state: $newState, status: $status"
        )
        when (newState) {
            GattConnectionState.STATE_CONNECTED -> connectDevice(device)
            GattConnectionState.STATE_DISCONNECTED,
            GattConnectionState.STATE_CONNECTING,
            GattConnectionState.STATE_DISCONNECTING -> removeDevice(device)
        }
    }

    /**
     * Removes device from connections list each time device gets disconnected.
     *
     * @param device A client device to remove.
     */
    private fun removeDevice(device: ClientDevice) {
        val mutableMap = connections.value.toMutableMap()
        mutableMap.remove(device)
        _connections.value = mutableMap.toMap()
    }

    /**
     * Connects server to a new device. Each new connection is created by making a new instances
     * of services declared for this server and by assigning connection parameters like MTU, PHY etc.
     *
     * @param device A client device to connect.
     */
    @SuppressLint("MissingPermission")
    private fun connectDevice(device: ClientDevice) {
        val mtuProvider = MtuProvider()
        val copiedServices = services.map {
            ServerBleGattService(
                server, device, BluetoothGattServiceFactory.copy(it), mtuProvider
            )
        }
        val mutableMap = connections.value.toMutableMap()
        val connection = ServerBluetoothGattConnection(
            device, server, ServerBleGattServices(server, device, copiedServices)
        )
        mutableMap[device] = connection
        _onNewConnection.tryEmit(connection)
        _connections.value = mutableMap.toMap()

        server.connect(device, true)
    }

    /**
     * This callback is invoked each time a server has been added to the server.
     * Generally the flow assumes adding all services at the server initiation so this callback
     * should be invoked once per each service declared at the start of the server.
     *
     * @param service An added service.
     * @param status If service has been added successfully.
     */
    private fun onServiceAdded(service: IBluetoothGattService, status: BleGattOperationStatus) {
        logger.log(Log.DEBUG, "Service added: ${service.uuid}, status: $status")
        if (status == BleGattOperationStatus.GATT_SUCCESS) {
            services = services + service
        }
    }

    /**
     * Callback informing that PHY parameters of the connection has been read.
     * This function updates local information for the specific device and
     * propagates this information up.
     *
     * @param event PHY read event data.
     */
    private fun onPhyRead(event: ServerPhyRead) {
        logger.log(Log.DEBUG, "Phy - device: ${event.device.address}, tx: ${event.txPhy}, rx: ${event.rxPhy}")
        _connections.value = _connections.value.toMutableMap().also {
            val connection = it.getValue(event.device).copy(
                txPhy = event.txPhy, rxPhy = event.rxPhy
            )
            it[event.device] = connection
        }.toMap()
    }

    /**
     * Callback informing that PHY parameters of the connection has been updated.
     * This function updates local information for the specific device and
     * propagates this information up.
     *
     * @param event PHY update event data.
     */
    private fun onPhyUpdate(event: ServerPhyUpdate) {
        logger.log(Log.DEBUG, "New phy - device: ${event.device.address}, tx: ${event.txPhy}, rx: ${event.rxPhy}")
        _connections.value = _connections.value.toMutableMap().also {
            val connection = it.getValue(event.device).copy(
                txPhy = event.txPhy, rxPhy = event.rxPhy
            )
            it[event.device] = connection
        }.toMap()
    }

    /**
     * Callback informing that MTU parameter of the connection has been updated.
     * This function updates local information for the specific device and
     * propagates this information up.
     *
     * @param event MTU changed event data.
     */
    private fun onMtuChanged(event: ServerMtuChanged) {
        logger.log(Log.DEBUG, "New mtu - device: ${event.device.address}, mtu: ${event.mtu}")
        _connections.value[event.device]?.mtuProvider?.updateMtu(event.mtu)
    }
}
