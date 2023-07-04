package no.nordicsemi.android.kotlin.ble.server.main

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.logger.BlekLogger
import no.nordicsemi.android.kotlin.ble.logger.DefaultBlekLogger
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServiceFactory
import no.nordicsemi.android.kotlin.ble.server.mock.MockServerAPI
import no.nordicsemi.android.kotlin.ble.server.real.NativeServerAPI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object BleGattServerFactory {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun create(
        context: Context,
        logger: BlekLogger = DefaultBlekLogger(context),
        vararg config: BleServerGattServiceConfig,
        mock: MockServerDevice? = null,
    ): BleGattServer {
        return mock?.let {
            createMockServer(it, logger, *config)
        } ?: createRealServer(context, logger, *config)
    }

    private fun createMockServer(
        device: MockServerDevice,
        logger: BlekLogger,
        vararg config: BleServerGattServiceConfig,
    ): BleGattServer {
        val api = MockServerAPI(MockEngine, device)
        val services = config.map { BluetoothGattServiceFactory.createMock(it) }

        return BleGattServer(api, logger).also { MockEngine.registerServer(api, device, services) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun createRealServer(
        context: Context,
        logger: BlekLogger,
        vararg config: BleServerGattServiceConfig,
    ): BleGattServer {
        return suspendCoroutine {
            val nativeServer = NativeServerAPI.create(context)
            val server = BleGattServer(nativeServer, logger)
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
