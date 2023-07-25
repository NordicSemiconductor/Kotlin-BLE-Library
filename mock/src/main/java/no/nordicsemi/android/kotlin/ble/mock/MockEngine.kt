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

package no.nordicsemi.android.kotlin.ble.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorRead
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyRead
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.client.api.OnReadRemoteRssi
import no.nordicsemi.android.kotlin.ble.client.api.OnReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.api.OnServiceChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.OnCharacteristicReadRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnCharacteristicWriteRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnClientConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.server.api.OnDescriptorReadRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnDescriptorWriteRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.api.OnServerMtuChanged
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyRead
import no.nordicsemi.android.kotlin.ble.server.api.OnServerPhyUpdate
import no.nordicsemi.android.kotlin.ble.server.api.OnServiceAdded
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent

/**
 * An object responsible for connecting client with mocked servers.
 * Each server can be started by setting [MockServerDevice] parameter.
 * If so then instead of calling BLE API it will be communicating with client using [MockEngine].
 * All communication will happen locally, but API the server and client side will be using is the
 * same as for BLE calls.
 *
 * Generally this is a proxy object. It catches requests emitted by [GattClientAPI] and emits them
 * as an [ServerGattEvent] on a server side. The server responds to those events
 * using [GattServerAPI] which are mapped here to [ClientGattEvent] and send back to client.
 *
 * Using this solution may be helpful for testing BLE communication before getting real BLE device
 * or for local junit tests.
 */
object MockEngine {
    private val _advertisedServers = MutableStateFlow(mapOf<MockServerDevice, BleScanResultData>())
    internal val advertisedServers = _advertisedServers.asStateFlow()

    private val servers = mutableMapOf<MockServerDevice, OpenedServer>()
    private val serverConnections = mutableMapOf<MockServerDevice, List<ClientDevice>>()
    private val clientConnections = mutableMapOf<ClientDevice, ServerConnection>()

    private var requests = MockRequestProvider()

    fun registerServer(
        server: GattServerAPI,
        device: MockServerDevice,
        services: List<IBluetoothGattService>,
    ) {
        servers[device] = OpenedServer(server, services)
        services.forEach {
            server.onEvent(OnServiceAdded(it, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    fun unregisterServer(device: MockServerDevice) {
        servers.remove(device)
        serverConnections.remove(device)?.forEach {
            clientConnections.remove(it)?.clientApi?.onEvent(
                OnConnectionStateChanged(
                    BleGattConnectionStatus.SUCCESS,
                    GattConnectionState.STATE_DISCONNECTED
                )
            )
        }
    }

    fun cancelConnection(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        val connection = clientConnections[clientDevice]!!

        connection.serverApi.onEvent(OnClientConnectionStateChanged(
            clientDevice,
            BleGattConnectionStatus.SUCCESS,
            GattConnectionState.STATE_DISCONNECTED
        ))

        connection.clientApi.onEvent(OnConnectionStateChanged(
            BleGattConnectionStatus.SUCCESS,
            GattConnectionState.STATE_DISCONNECTED
        ))
    }

    fun connectToServer(
        serverDevice: MockServerDevice,
        clientDevice: MockClientDevice,
        client: GattClientAPI,
        options: BleGattConnectOptions,
    ) {
        val server = servers[serverDevice]!!
        val phy = options.phy ?: BleGattPhy.PHY_LE_1M
        val connection = ServerConnection(
            serverDevice,
            clientDevice,
            server.serverApi,
            client,
            server.services,
            ConnectionParams(txPhy = phy, rxPhy = phy),
        )
        serverConnections[serverDevice] = (serverConnections[serverDevice] ?: emptyList()) + clientDevice
        clientConnections[clientDevice] = connection

        server.serverApi.onEvent(
            OnClientConnectionStateChanged(
                clientDevice,
                BleGattConnectionStatus.SUCCESS,
                GattConnectionState.STATE_CONNECTED
            )
        )
    }

    fun advertiseServer(device: MockServerDevice, config: BleAdvertisingConfig) {
        _advertisedServers.value = _advertisedServers.value + (device to config.toScanResult())
    }

    fun stopAdvertising(device: MockServerDevice) {
        _advertisedServers.value = _advertisedServers.value - device
    }

    //Server side

    fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: DataByteArray?,
    ) {
        val gattStatus = BleGattOperationStatus.create(status)
        val connection = clientConnections[device]
        val client = connection?.clientApi

        if (value == null) {
            client?.onEvent(OnReliableWriteCompleted(BleGattOperationStatus.GATT_SUCCESS))
            return
        }

        val event = when (val request = requests.getRequest(requestId)) {
            is MockCharacteristicRead -> OnCharacteristicRead(
                request.characteristic,
                value,
                gattStatus
            )

            is MockCharacteristicWrite -> OnCharacteristicWrite(request.characteristic, gattStatus)
            is MockDescriptorRead -> OnDescriptorRead(request.descriptor, value, gattStatus)
            is MockDescriptorWrite -> OnDescriptorWrite(request.descriptor, gattStatus)
        }
        client?.onEvent(event)
    }

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        confirm: Boolean,
        value: DataByteArray,
    ) {
        val connection = clientConnections[device] ?: return
        if (connection.enabledNotification[characteristic.uuid] == true) {
            connection.clientApi.onEvent(OnCharacteristicChanged(characteristic, value))
        }
    }

    fun connect(device: ClientDevice, autoConnect: Boolean) {
        clientConnections[device]?.clientApi?.onEvent(
            OnConnectionStateChanged(
                BleGattConnectionStatus.SUCCESS,
                GattConnectionState.STATE_CONNECTED
            )
        )
    }

    fun readPhy(device: ClientDevice) {
        val connection = clientConnections[device]!!
        val params = connection.params
        connection.serverApi.onEvent(
            OnServerPhyRead(
                device,
                params.txPhy,
                params.rxPhy,
                BleGattOperationStatus.GATT_SUCCESS
            )
        )
    }

    fun requestPhy(
        device: ClientDevice,
        txPhy: BleGattPhy,
        rxPhy: BleGattPhy,
        phyOption: PhyOption,
    ) {
        val connection = clientConnections[device]!!
        val params = connection.params
        clientConnections[device]?.copy(
            params = params.copy(txPhy = txPhy, rxPhy = rxPhy, phyOption = phyOption)
        )?.let {
            clientConnections[device] = it
        }
        clientConnections[device]?.let {
            it.serverApi.onEvent(
                OnServerPhyUpdate(device, txPhy, rxPhy, BleGattOperationStatus.GATT_SUCCESS)
            )
            it.clientApi.onEvent(OnPhyUpdate(txPhy, rxPhy, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    //Client side

    fun writeCharacteristic(
        serverDevice: ServerDevice,
        clientDevice: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        value: DataByteArray,
        writeType: BleWriteType,
    ) {
        val connection = clientConnections[clientDevice]!!
        val isResponseNeeded = when (writeType) {
            BleWriteType.NO_RESPONSE -> false
            BleWriteType.DEFAULT,
            BleWriteType.SIGNED -> true
        }
        val request = requests.newWriteRequest(characteristic)
        servers[serverDevice]?.serverApi?.onEvent(
            OnCharacteristicWriteRequest(
                clientDevice,
                request.requestId,
                characteristic,
                connection.isReliableWriteOn,
                isResponseNeeded,
                0,
                value
            )
        )
    }

    fun readCharacteristic(device: MockServerDevice, clientDevice: ClientDevice, characteristic: IBluetoothGattCharacteristic) {
        val request = requests.newReadRequest(characteristic)
        servers[device]?.serverApi?.onEvent(
            OnCharacteristicReadRequest(clientDevice, request.requestId, 0, characteristic)
        )
    }

    fun enableCharacteristicNotification(
        clientDevice: ClientDevice,
        device: MockServerDevice,
        characteristic: IBluetoothGattCharacteristic,
    ) {
        val connection = clientConnections[clientDevice]!!
        val newNotifications = connection.enabledNotification.toMutableMap()
        newNotifications[characteristic.uuid] = true
        val newConnection = connection.copy(enabledNotification = newNotifications)
        clientConnections[clientDevice] = newConnection
    }

    fun disableCharacteristicNotification(
        clientDevice: ClientDevice,
        device: MockServerDevice,
        characteristic: IBluetoothGattCharacteristic,
    ) {
        val connection = clientConnections[clientDevice] ?: return
        val newNotifications = connection.enabledNotification.toMutableMap()
        newNotifications[characteristic.uuid] = false
        val newConnection = connection.copy(enabledNotification = newNotifications)
        clientConnections[clientDevice] = newConnection
    }

    fun writeDescriptor(
        device: MockServerDevice,
        clientDevice: ClientDevice,
        descriptor: IBluetoothGattDescriptor,
        value: DataByteArray,
    ) {
        val connection = clientConnections[clientDevice]!!
        val request = requests.newWriteRequest(descriptor)
        servers[device]?.serverApi?.onEvent(
            OnDescriptorWriteRequest(
                device = clientDevice,
                requestId = request.requestId,
                descriptor = descriptor,
                preparedWrite = connection.isReliableWriteOn,
                responseNeeded = true,
                offset = 0,
                value = value
            )
        )
    }

    fun readDescriptor(device: MockServerDevice, clientDevice: ClientDevice, descriptor: IBluetoothGattDescriptor) {
        val request = requests.newReadRequest(descriptor)
        servers[device]?.serverApi?.onEvent(
            OnDescriptorReadRequest(clientDevice, request.requestId, 0, descriptor)
        )
    }

    fun readRemoteRssi(clientDevice: ClientDevice, device: MockServerDevice) {
        val connection = clientConnections[clientDevice]!!
        connection.clientApi.onEvent(
            OnReadRemoteRssi(connection.params.rssi, BleGattOperationStatus.GATT_SUCCESS)
        )
    }

    fun readPhy(clientDevice: ClientDevice, device: MockServerDevice) {
        val connection = clientConnections[clientDevice]!!
        connection.clientApi.onEvent(
            OnPhyRead(connection.params.txPhy, connection.params.rxPhy, BleGattOperationStatus.GATT_ERROR)
        )
    }

    fun discoverServices(clientDevice: ClientDevice, device: MockServerDevice) {
        val connection = clientConnections[clientDevice]!!
        val services = connection.services

        val event = OnServicesDiscovered(services, BleGattOperationStatus.GATT_SUCCESS)

        connection.clientApi.onEvent(event)
    }

    fun setPreferredPhy(
        clientDevice: ClientDevice,
        serverDevice: MockServerDevice,
        txPhy: BleGattPhy,
        rxPhy: BleGattPhy,
        phyOption: PhyOption,
    ) {
        val connection = clientConnections[clientDevice]!!
        val newConnection = connection.copy(
            params = connection.params.copy(txPhy = txPhy, rxPhy = rxPhy, phyOption = phyOption)
        )
        clientConnections[clientDevice] = newConnection

        connection.clientApi.onEvent(
            OnPhyUpdate(txPhy, rxPhy, BleGattOperationStatus.GATT_SUCCESS)
        )

        connection.serverApi.onEvent(
            OnServerPhyUpdate(connection.client, txPhy, rxPhy, BleGattOperationStatus.GATT_SUCCESS)
        )
    }

    fun requestMtu(clientDevice: ClientDevice, serverDevice: MockServerDevice, mtu: Int) {
        val connection = clientConnections[clientDevice]!!
        val newConnection = connection.copy(
            params = connection.params.copy(mtu = mtu)
        )
        clientConnections[clientDevice] = newConnection

        connection.clientApi.onEvent(
            OnMtuChanged(mtu, BleGattOperationStatus.GATT_SUCCESS)
        )

        connection.serverApi.onEvent(
            OnServerMtuChanged(connection.client, mtu)
        )
    }

    fun close(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        serverConnections[serverDevice]?.let {
            if (it.contains(clientDevice)) {
                serverConnections[serverDevice] = it - clientDevice
            }
        }
        clientConnections.remove(clientDevice)
    }

    fun clearServiceCache(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        val connection = clientConnections[clientDevice]!!
        connection.clientApi.onEvent(OnServiceChanged())
    }

    fun beginReliableWrite(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        val connection = clientConnections[clientDevice]!!
        val newConnection = connection.copy(
            isReliableWriteOn = true
        )
        clientConnections[clientDevice] = newConnection
    }

    fun abortReliableWrite(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        val connection = clientConnections[clientDevice]!!
        val newConnection = connection.copy(
            isReliableWriteOn = false
        )
        clientConnections[clientDevice] = newConnection

        val request = requests.newAbortReliableWriteRequest()
        connection.serverApi.onEvent(
            OnExecuteWrite(connection.client, request.requestId, false)
        )
    }

    fun executeReliableWrite(serverDevice: MockServerDevice, clientDevice: ClientDevice) {
        val connection = clientConnections[clientDevice]!!
        val newConnection = connection.copy(
            isReliableWriteOn = false
        )
        clientConnections[clientDevice] = newConnection

        val request = requests.newExecuteReliableWriteRequest()
        connection.serverApi.onEvent(
            OnExecuteWrite(connection.client, request.requestId, true)
        )
    }
}
