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

package no.nordicsemi.android.kotlin.ble.test

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import java.util.UUID
import javax.inject.Inject

val RELIABLE_WRITE_SERVICE: UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")

val FIRST_CHARACTERISTIC = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
val SECOND_CHARACTERISTIC = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
class ReliableWriteServerProvider @Inject constructor(
    private val scope: CoroutineScope
) {

    lateinit var server: ServerBleGatt

    lateinit var firstCharacteristic: ServerBleGattCharacteristic
    lateinit var secondCharacteristic: ServerBleGattCharacteristic

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "Reliable Write Server",
            address = "55:44:33:22:11"
        ),
    ) = scope.launch {
        val firstCharacteristic = ServerBleGattCharacteristicConfig(
            FIRST_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val secondCharacteristic = ServerBleGattCharacteristicConfig(
            SECOND_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val serviceConfig = ServerBleGattServiceConfig(
            RELIABLE_WRITE_SERVICE,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(firstCharacteristic, secondCharacteristic)
        )

        server = ServerBleGatt.create(
            context = context,
            config = arrayOf(serviceConfig),
            mock = device,
            scope = scope
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(config = BleAdvertisingConfig(), mock = device).launchIn(scope)

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect { setUpConnection(it) }
        }
    }

    internal fun stopServer() {
        server.stopServer()
    }

    private fun setUpConnection(connection: ServerBluetoothGattConnection) {
        val glsService = connection.services.findService(RELIABLE_WRITE_SERVICE)!!
        firstCharacteristic = glsService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        secondCharacteristic = glsService.findCharacteristic(SECOND_CHARACTERISTIC)!!
    }
}