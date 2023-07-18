/*
 * Copyright (c) 2022, Nordic Semiconductor
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.core.ApplicationScope
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.core.utils.simpleSharedFlow
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.logger.BlekLogger
import no.nordicsemi.android.kotlin.ble.logger.DefaultBlekLogger
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.OnClientConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.server.api.OnServerMtuChanged
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyRead
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyUpdate
import no.nordicsemi.android.kotlin.ble.server.api.OnServiceAdded
import no.nordicsemi.android.kotlin.ble.server.api.ServiceEvent
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattService
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServices
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServiceFactory

class ServerBleGatt internal constructor(
    private val server: GattServerAPI,
    private val logger: BlekLogger,
) {

    companion object {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        suspend fun create(
            context: Context,
            vararg config: ServerBleGattServiceConfig,
            logger: BlekLogger = DefaultBlekLogger(context),
            mock: MockServerDevice? = null,
        ): ServerBleGatt {
            return ServerBleGattFactory.create(context, logger, *config, mock = mock)
        }
    }

    private val _onNewConnection = simpleSharedFlow<ServerBluetoothGattConnection>()
    val onNewConnection = _onNewConnection.asSharedFlow()

    private val _connections =
        MutableStateFlow(mapOf<ClientDevice, ServerBluetoothGattConnection>())
    val connections = _connections.asStateFlow()

    private var services: List<IBluetoothGattService> = emptyList()

    init {
        server.event.onEach {
            logger.log(Log.VERBOSE, "On gatt event: $it")
            when (it) {
                is OnServiceAdded -> onServiceAdded(it.service, it.status)
                is OnClientConnectionStateChanged -> onConnectionStateChanged(
                    it.device, it.status, it.newState
                )

                is ServiceEvent -> connections.value[it.device]?.services?.onEvent(it)
                is OnServerPhyRead -> onPhyRead(it)
                is OnServerPhyUpdate -> onPhyUpdate(it)
                is OnServerMtuChanged -> onMtuChanged(it)
            }
        }.launchIn(ApplicationScope)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopServer() {
        logger.log(Log.INFO, "Stopping server")
        server.close()
    }

    fun cancelConnection(device: ClientDevice) {
        logger.log(Log.INFO, "Cancelling connection with client: ${device.address}")
        server.cancelConnection(device)
    }

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

    private fun removeDevice(device: ClientDevice) {
        val mutableMap = connections.value.toMutableMap()
        mutableMap.remove(device)
        _connections.value = mutableMap.toMap()
    }

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

    private fun onServiceAdded(service: IBluetoothGattService, status: BleGattOperationStatus) {
        logger.log(Log.DEBUG, "Service added: ${service.uuid}, status: $status")
        if (status == BleGattOperationStatus.GATT_SUCCESS) {
            services = services + service
        }
    }

    private fun onPhyRead(event: OnServerPhyRead) {
        logger.log(Log.DEBUG, "Phy - device: ${event.device.address}, tx: ${event.txPhy}, rx: ${event.rxPhy}")
        _connections.value = _connections.value.toMutableMap().also {
            val connection = it.getValue(event.device).copy(
                txPhy = event.txPhy, rxPhy = event.rxPhy
            )
            it[event.device] = connection
        }.toMap()
    }

    private fun onPhyUpdate(event: OnServerPhyUpdate) {
        logger.log(Log.DEBUG, "New phy - device: ${event.device.address}, tx: ${event.txPhy}, rx: ${event.rxPhy}")
        _connections.value = _connections.value.toMutableMap().also {
            val connection = it.getValue(event.device).copy(
                txPhy = event.txPhy, rxPhy = event.rxPhy
            )
            it[event.device] = connection
        }.toMap()
    }

    private fun onMtuChanged(event: OnServerMtuChanged) {
        logger.log(Log.DEBUG, "New mtu - device: ${event.device.address}, mtu: ${event.mtu}")
        _connections.value[event.device]?.mtuProvider?.updateMtu(event.mtu)
    }
}
