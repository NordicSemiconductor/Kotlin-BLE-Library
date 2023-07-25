package no.nordicsemi.android.kotlin.ble.server.main.service

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.logger.BleLogger
import no.nordicsemi.android.kotlin.ble.logger.DefaultBlekLogger
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.mock.MockServerAPI
import no.nordicsemi.android.kotlin.ble.server.real.NativeServerBleAPI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Factory object responsible for creating an instance of [ServerBleGatt].
 */
internal object ServerBleGattFactory {

    /**
     * Creates [ServerBleGatt] instance. It can be both mocked or real BLE variant.
     *
     * @param context An application context.
     * @param logger An object responsible for displaying logs.
     * @param config Prescription for future BLE services creation.
     * @param mock Mock server device if a server should be run locally.
     * @return New instance of [ServerBleGatt].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun create(
        context: Context,
        logger: BleLogger = DefaultBlekLogger(context),
        vararg config: ServerBleGattServiceConfig,
        mock: MockServerDevice? = null,
    ): ServerBleGatt {
        return mock?.let {
            createMockServer(it, logger, *config)
        } ?: createRealServer(context, logger, *config)
    }

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
        logger: BleLogger,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {
        val api = MockServerAPI(MockEngine, device)
        val services = config.map { BluetoothGattServiceFactory.createMock(it) }

        return ServerBleGatt(api, logger).also { MockEngine.registerServer(api, device, services) }
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
        logger: BleLogger,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {
        return suspendCoroutine {
            val nativeServer = NativeServerBleAPI.create(context)
            val server = ServerBleGatt(nativeServer, logger)
            var index = 0

            nativeServer.callback.onServiceAdded = {
                if (index <= config.lastIndex) {
                    val service = BluetoothGattServiceFactory.createNative(config[index++])
                    nativeServer.server.addService(service.service)
                } else {
                    nativeServer.callback.onServiceAdded = null
                    it.resume(server)
                }
            }

            if (config.isNotEmpty()) {
                val service = BluetoothGattServiceFactory.createNative(config[index++])
                nativeServer.server.addService(service.service)
            } else {
                it.resume(server)
            }
        }
    }
}
