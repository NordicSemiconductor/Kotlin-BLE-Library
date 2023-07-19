package no.nordicsemi.android.kotlin.ble.server.main.service

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.logger.BlekLogger
import no.nordicsemi.android.kotlin.ble.logger.DefaultBlekLogger
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.mock.MockServerAPI
import no.nordicsemi.android.kotlin.ble.server.real.NativeServerAPI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Factory object responsible for creating an instance of [ServerBleGatt].
 */
internal object ServerBleGattFactory {

    /**
     * Creates [ServerBleGatt] instance. It can be
     *
     * @param context An application context.
     * @param logger An object responsible for displaying logs.
     * @param config
     * @param mock
     * @return
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun create(
        context: Context,
        logger: BlekLogger = DefaultBlekLogger(context),
        vararg config: ServerBleGattServiceConfig,
        mock: MockServerDevice? = null,
    ): ServerBleGatt {
        return mock?.let {
            createMockServer(it, logger, *config)
        } ?: createRealServer(context, logger, *config)
    }

    private fun createMockServer(
        device: MockServerDevice,
        logger: BlekLogger,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {
        val api = MockServerAPI(MockEngine, device)
        val services = config.map { BluetoothGattServiceFactory.createMock(it) }

        return ServerBleGatt(api, logger).also { MockEngine.registerServer(api, device, services) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun createRealServer(
        context: Context,
        logger: BlekLogger,
        vararg config: ServerBleGattServiceConfig,
    ): ServerBleGatt {
        return suspendCoroutine {
            val nativeServer = NativeServerAPI.create(context)
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
