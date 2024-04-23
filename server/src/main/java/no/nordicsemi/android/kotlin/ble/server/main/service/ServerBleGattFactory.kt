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

package no.nordicsemi.android.kotlin.ble.server.main.service

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.data.ServerConnectionOption
import no.nordicsemi.android.kotlin.ble.server.mock.MockServerAPI
import no.nordicsemi.android.kotlin.ble.server.real.NativeServerBleAPI
import org.slf4j.LoggerFactory
import kotlin.coroutines.resume

/**
 * Factory object responsible for creating an instance of [ServerBleGatt].
 */
@Suppress("InlinedApi")
internal object ServerBleGattFactory {
    private var logger = LoggerFactory.getLogger(ServerBleGattFactory::class.java)

    /**
     * Creates [ServerBleGatt] instance. It can be both mocked or real BLE variant.
     *
     * @param context An application context.
     * @param logger An object responsible for displaying logs.
     * @param config Prescription for future BLE services creation.
     * @param mock Mock server device if a server should be run locally.
     * @param options An additional options to configure a server behaviour.
     * @return New instance of [ServerBleGatt].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun create(
        context: Context,
        scope: CoroutineScope,
        vararg config: ServerBleGattServiceConfig,
        mock: MockServerDevice? = null,
        options: ServerConnectionOption,
    ): ServerBleGatt = mock?.let {
        createMockServer(it, scope, options, *config)
    } ?: createRealServer(context, scope, options, *config)

    /**
     * Creates mock variant of the server which will be used locally on a device without the
     * actual use of BLE stack.
     *
     * @param device Mocked server device associated with this server.
     * @param logger An object responsible for displaying logs.
     * @param config Prescription for future BLE services creation.
     * @return New instance of [ServerBleGatt].
     */
    private fun createMockServer(
        device: MockServerDevice,
        scope: CoroutineScope,
        options: ServerConnectionOption,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {
        val api = MockServerAPI(MockEngine, device, options.bufferSize)
        val services = config.map { BluetoothGattServiceFactory.createMock(it) }

        return ServerBleGatt(api, scope, options.bufferSize)
            .also { MockEngine.registerServer(api, device, services) }
    }

    /**
     * Creates real variant of a server which will use BLE stack for communication.
     *
     * @param context An application context.
     * @param logger An object responsible for displaying logs.
     * @param config Prescription for future BLE services creation.
     * @return New instance of [ServerBleGatt].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun createRealServer(
        context: Context,
        scope: CoroutineScope,
        options: ServerConnectionOption,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {

        /*
            When using PERMISSION_READ_ENCRYPTED_MITM on some hardware then onServiceAdded is not
            invoked. Coroutine needs to be cancellable then.
         */
        return suspendCancellableCoroutine {
            val nativeServer = NativeServerBleAPI.create(context, options.bufferSize)
            val services = ArrayList<IBluetoothGattService>(config.size)
            var index = 0

            nativeServer.callback.onServiceAdded = { service, status ->
                logger.trace("Service added (uuid: {}), status: {}", service.uuid, status)
                if (status == BleGattOperationStatus.GATT_SUCCESS) {
                    services += service
                }

                if (index <= config.lastIndex) {
                    val nativeService = BluetoothGattServiceFactory.createNative(config[index++])
                    nativeServer.server.addService(nativeService.service)
                } else {
                    //https://github.com/NordicSemiconductor/Kotlin-BLE-Library/issues/112
                    //we create ServerBleGatt when services add complete
                    //so we can avoid the uncertainty subscription time
                    nativeServer.callback.onServiceAdded = null
                    it.resume(ServerBleGatt(nativeServer, scope, options.bufferSize, services))
                }
            }

            if (config.isNotEmpty()) {
                val service = BluetoothGattServiceFactory.createNative(config[index++])
                nativeServer.server.addService(service.service)
            } else {
                it.resume(ServerBleGatt(nativeServer, scope, options.bufferSize, services))
            }

            it.invokeOnCancellation {
                nativeServer.callback.onServiceAdded = null
            }
        }
    }
}
