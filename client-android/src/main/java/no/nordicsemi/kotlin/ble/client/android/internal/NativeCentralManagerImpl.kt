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

@file:Suppress("UnusedReceiverParameter", "unused")

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.log.Layer
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment
import no.nordicsemi.kotlin.log.Log
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import android.bluetooth.le.ScanCallback as NativeScanCallback
import android.bluetooth.le.ScanResult as NativeScanResult
import android.bluetooth.le.ScanSettings as NativeScanSettings

/**
 * A native implementation of [CentralManager] for Android.
 *
 * @param scope The coroutine scope.
 * @param environment Native Android environment object.
 */
internal class NativeCentralManagerImpl(
    scope: CoroutineScope,
    private val environment: NativeAndroidEnvironment,
): CentralManagerImpl(scope, environment) {
    override var logger: Log.Sink<Layer>? = Log.Sink.Null
    override val state = environment.bluetoothState

    private val _bondState =
        MutableSharedFlow<Pair<String, BondState>>(extraBufferCapacity = 1)

    private val bondStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent!!.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent!!.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            } ?: return
            val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE).toBondState()
            val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE).toBondState()
            logger?.info(Layer.SMP) { "Bond state of $device changed: $previousBondState -> $bondState" }
            _bondState.tryEmit(device.address to bondState)
        }
    }

    init {
        val monitorBondState = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        ContextCompat.registerReceiver(environment.applicationContext, bondStateBroadcastReceiver, monitorBondState, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun getPeripheralsById(ids: List<String>): List<Peripheral> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        val adapter = environment.bluetoothManager?.adapter ?: throw BluetoothUnavailableException()
        return ids.map { id ->
            peripheral(id) {
                Peripheral(
                    scope = scope,
                    impl = NativeExecutor(environment.applicationContext, adapter.getRemoteDevice(it), null)
                        .apply {
                            _bondState
                                .filter { (address, _) -> address == id }
                                .onEach { (_, state) -> onBondStateChanged(state) }
                                .launchIn(scope)
                        }
                ).also { p -> p.logger = logger }
            }
        }
    }

    override fun getBondedPeripherals(): List<Peripheral> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        // Verify the BLUETOOTH_CONNECT permission is granted (Android 12+).
        checkConnectPermission()

        val adapter = environment.bluetoothManager?.adapter ?: throw BluetoothUnavailableException()
        val ids = adapter.bondedDevices?.map { it.address } ?: return emptyList()
        return getPeripheralsById(ids)
    }

    override fun scan(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<ScanResult> = callbackFlow {
        // Ensure the central manager has not been closed.
        try {
            ensureOpen()
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        // Ensure the Bluetooth is enabled and Bluetooth LE Scanner is available.
        val scanner = environment.bluetoothManager?.adapter
            ?.takeIf { it.isEnabled }
            ?.bluetoothLeScanner
            ?: run {
                close(BluetoothUnavailableException())
                return@callbackFlow
            }

        // Verify the BLUETOOTH_SCAN permission is granted (Android 12+).
        try {
            checkScanningPermission()
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        // Build the filter based on the provided builder.
        val filters = ConjunctionFilter().apply(filter).filters

        // Use the most optimal scan mode for low latency immediate scanning.
        val settings = NativeScanSettings.Builder()
            .apply {
                // TODO Scan settings could be configurable.
                setScanMode(NativeScanSettings.SCAN_MODE_LOW_LATENCY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setLegacy(false)
                    setPhy(NativeScanSettings.PHY_LE_ALL_SUPPORTED)
                }
            }
            .build()

        // Define the callback that will emit scan results.
        val callback: NativeScanCallback = object : NativeScanCallback() {
            override fun onScanResult(callbackType: Int, result: NativeScanResult) {
                // A result matching the offloaded filter was found, convert it to ScanResult.
                val scanResult = result.toScanResult(
                    peripheral = { device, name ->
                        peripheral(device.address) {
                            Peripheral(
                                scope = scope,
                                impl = NativeExecutor(environment.applicationContext, device, name)
                                    .apply {
                                        _bondState
                                            .filter { (address, _) -> address == device.address }
                                            .onEach { (_, state) -> onBondStateChanged(state) }
                                            .launchIn(scope)
                                    }
                            ).also { p -> p.logger = logger }
                        }
                    }
                ) ?: return

                // Check other filters that cannot be checked by the controller.
                if (filters?.match(scanResult) == false) return

                trySend(scanResult)
            }

            override fun onBatchScanResults(results: List<NativeScanResult>) {
                results.forEach { onScanResult(0, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                with (ScanningFailedToStartException(errorCode.errorCodeToReason())) {
                    logger?.error(Layer.GAP, this)
                    close(this)
                }
            }
        }

        // Finally, start the scan.
        logger?.trace(Layer.GAP) {
            "Starting scanning with ${filters?.let { "filters: $it" } ?: "no filters"}"
        }
        scanner.startScan(filters?.toNative(), settings, callback)

        // Set a timeout to stop the scan.
        if (timeout > 0.milliseconds) {
            launch(Dispatchers.IO) {
                // If the flow is canceled before the timeout, the delay() method will throw
                // a CancellationException, which will be ignored.
                delay(timeout)
                // If we reached the timeout, close the flow manually.
                logger?.trace(Layer.GAP) { "Scanning timed out after $timeout" }
                close()
            }
        }
        awaitClose {
            scanner.stopScan(callback)
            logger?.trace(Layer.GAP) { "Scanning stopped" }
        }
    }

    override fun monitor(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<MonitoringEvent<Peripheral>> {
        TODO("Not yet implemented")
    }

    override fun range(peripheral: Peripheral, timeout: Duration): Flow<RangeEvent<Peripheral>> {
        TODO("Not yet implemented")
    }

    override fun close() {
        // Ignore if already closed.
        if (!isOpen) return
        super.close()

        // Release resources.
        try {
            environment.applicationContext.unregisterReceiver(bondStateBroadcastReceiver)
        } catch (_: Exception) {
            // Ignore
        }
    }
}