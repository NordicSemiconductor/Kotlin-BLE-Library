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

package no.nordicsemi.kotlin.ble.client.android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.GenericCentralManager
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import no.nordicsemi.kotlin.ble.client.android.internal.ConjunctionFilter
import no.nordicsemi.kotlin.ble.client.android.internal.NativeExecutor
import no.nordicsemi.kotlin.ble.client.android.internal.errorCodeToReason
import no.nordicsemi.kotlin.ble.client.android.internal.toScanResult
import no.nordicsemi.kotlin.ble.client.android.internal.toState
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Manager.State.POWERED_OFF
import no.nordicsemi.kotlin.ble.core.Manager.State.POWERED_ON
import no.nordicsemi.kotlin.ble.core.Manager.State.RESETTING
import no.nordicsemi.kotlin.ble.core.Manager.State.UNKNOWN
import no.nordicsemi.kotlin.ble.core.Manager.State.UNSUPPORTED
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import android.bluetooth.le.ScanCallback as NativeScanCallback
import android.bluetooth.le.ScanResult as NativeScanResult
import android.bluetooth.le.ScanSettings as NativeScanSettings

/**
 * Creates an implementation of the [CentralManager] which is using native Android API to
 * scan and connect to physical Bluetooth LE devices.
 *
 * @param context Android context, needed to connect to peripherals and listen to system events.
 * @param scope The coroutine scope.
 */
fun CentralManager.Factory.native(context: Context, scope: CoroutineScope) =
    CentralManager(NativeCentralManagerEngine(context, scope))

/**
 * An implementation of [GenericCentralManager] for Android.
 *
 * @param context Android context, needed to connect to peripherals and listen to system events.
 * @param scope The coroutine scope.
 */
open class NativeCentralManagerEngine(
    context: Context,
    scope: CoroutineScope,
): CentralManagerEngine<Context>(scope, context.applicationContext) {
    private val logger = LoggerFactory.getLogger(NativeCentralManagerEngine::class.java)

    /**
     * Application context.
     */
    private val applicationContext: Context = context.applicationContext

    /**
     * Bluetooth manager.
     */
    private val manager = ContextCompat.getSystemService(applicationContext, BluetoothManager::class.java)

    /**
     * A list of TODO
     */
    private val connectedPeripherals: List<NativeExecutor> = emptyList()

    /**
     * State of the Bluetooth adapter.
     *
     * This is a [MutableStateFlow] that emits the current state of the Bluetooth adapter.
     * The state is updated when the adapter state changes.
     */
    private val _state = MutableStateFlow(getState())
    override val state = _state.asStateFlow()

    /**
     * Broadcast receiver that listens for Bluetooth state changes and emits [state].
     */
    private val stateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val oldState = _state.value
            val newState = getState()
            if (oldState != newState) {
                logger.info("Bluetooth state changed: $oldState -> $newState")
                _state.update { newState }
            }
        }
    }

    init {
        // Register a broadcast receiver to monitor Bluetooth state changes.
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(applicationContext, stateBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun checkConnectPermission() {
        check(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("BLUETOOTH_CONNECT permission not granted")
        }
    }

    override fun checkScanningPermission() {
        check(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("BLUETOOTH_SCAN permission not granted")
        }
    }

    override fun getPeripheralsById(ids: List<String>): List<Peripheral> {
        check(isOpen) { throw ManagerClosedException() }

        val adapter = manager?.adapter ?: return emptyList()
        return ids
            .map { adapter.getRemoteDevice(it) }
            .map { bluetoothDevice ->
                Peripheral(
                    scope = scope,
                    impl = NativeExecutor(applicationContext, bluetoothDevice)
                )
            }
        // TODO Return Peripherals that are already being handled by the CentralManager.
    }

    override fun getBondedPeripherals(): List<Peripheral> {
        check(isOpen) { throw ManagerClosedException() }

        val adapter = manager?.adapter ?: return emptyList()
        return adapter.bondedDevices
            .map { bluetoothDevice ->
                Peripheral(
                    scope = scope,
                    impl = NativeExecutor(applicationContext, bluetoothDevice)
                )
            }
        // TODO Return Peripherals that are already being handled by the CentralManager.
    }

    override suspend fun connect(
        peripheral: Peripheral,
        options: CentralManager.ConnectionOptions
    ) {
        // Ensure the peripheral is a PhysicalPeripheral.
        // TODO: Uncomment this and fix?
//        require(peripheral is PhysicalPeripheral) {
//            "Cannot connect to ${peripheral.javaClass.simpleName}"
//        }

        super.connect(peripheral, options)
    }

    override fun scan(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<ScanResult> = callbackFlow {
        // Ensure the central manager has not been closed.
        if (!isOpen) {
            close(ManagerClosedException())
            return@callbackFlow
        }

        // Ensure the Bluetooth is enabled and Bluetooth LE Scanner is available.
        val scanner = manager?.adapter
            ?.takeIf { it.isEnabled }
            ?.bluetoothLeScanner ?: run {
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

        // Build the filter based on the provided builder
        val filters = ConjunctionFilter().apply(filter)

        // Use the most optimal scan mode for low latency immediate scanning.
        val settings = NativeScanSettings.Builder()
            .apply {
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
                val scanResult = result.toScanResult(scope, applicationContext) ?: return
                // TODO apply runtime filters
                trySend(scanResult)
            }

            override fun onBatchScanResults(results: List<NativeScanResult>) {
                results.forEach { onScanResult(0, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                logger.error("Scan failed, error code: $errorCode")
                close(ScanningFailedToStartException(errorCode.errorCodeToReason()))
            }
        }

        // Finally, start the scan.
        val scanFilters = filters.filters
        scanFilters?.let {
            logger.trace("Starting scanning with filters: {}", it)
        } ?: logger.trace("Starting scanning with no filters")
        scanner.startScan(scanFilters, settings, callback)

        // Set a timeout to stop the scan.
        if (timeout > 0.milliseconds) {
            launch(Dispatchers.IO) {
                // If the flow is cancelled before the timeout, the delay() method will throw
                // a CancellationException, which will be ignored.
                delay(timeout)
                // If we reached the timeout, close the flow manually.
                logger.trace("Scanning timed out after {}", timeout)
                close()
            }
        }
        awaitClose {
            logger.trace("Stopping scanning")
            scanner.stopScan(callback)
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

        // Release resources.
        applicationContext.unregisterReceiver(stateBroadcastReceiver)

        // Set the state to unknown.
        _state.update { UNKNOWN }
        super.close()
    }

    // ---- Private implementation ----

    /**
     * Returns the current state of the Bluetooth adapter.
     *
     * This states are mapped as follows:
     * - [UNSUPPORTED] if Bluetooth is not supported.
     * - [POWERED_OFF] if the Bluetooth adapter is disabled.
     * - [POWERED_ON] if the Bluetooth adapter is enabled.
     * - [RESETTING] if the Bluetooth adapter is turning off or on.
     * - [UNKNOWN] if the state is unknown.
     */
    private fun getState(): Manager.State {
        // If the central manager was closed, return unknown state.
        if (!isOpen) return UNKNOWN

        // Get the current state.
        return manager?.adapter?.state?.toState() ?: UNSUPPORTED
    }

}