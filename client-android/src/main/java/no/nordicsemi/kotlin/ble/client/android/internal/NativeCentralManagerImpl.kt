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

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Manager.State.POWERED_OFF
import no.nordicsemi.kotlin.ble.core.Manager.State.POWERED_ON
import no.nordicsemi.kotlin.ble.core.Manager.State.RESETTING
import no.nordicsemi.kotlin.ble.core.Manager.State.UNKNOWN
import no.nordicsemi.kotlin.ble.core.Manager.State.UNSUPPORTED
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import android.bluetooth.le.ScanCallback as NativeScanCallback
import android.bluetooth.le.ScanResult as NativeScanResult
import android.bluetooth.le.ScanSettings as NativeScanSettings

/**
 * A native implementation of [CentralManager] for Android.
 *
 * @param context Android context, needed to connect to peripherals and listen to system events.
 * @param scope The coroutine scope.
 */
internal class NativeCentralManagerImpl(
    context: Context,
    scope: CoroutineScope,
): CentralManagerImpl(scope) {
    private val logger = LoggerFactory.getLogger(NativeCentralManagerImpl::class.java)

    /**
     * Application context.
     */
    private val applicationContext: Context = context.applicationContext

    /**
     * Bluetooth manager.
     */
    private val manager = ContextCompat.getSystemService(applicationContext, BluetoothManager::class.java)

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

    private val _bondState =
        MutableSharedFlow<Pair<String, BondState>>(extraBufferCapacity = 1)

    private val bondStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            } ?: return
            val previousBondState = intent!!.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE).toBondState()
            val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE).toBondState()
            logger.info("Bond state of $device changed: $previousBondState -> $bondState")
            _bondState.tryEmit(device.address to bondState)
        }
    }

    init {
        // Register a broadcast receiver to monitor Bluetooth state changes.
        val monitorBluetoothState = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(applicationContext, stateBroadcastReceiver, monitorBluetoothState, ContextCompat.RECEIVER_EXPORTED)

        val monitorBondState = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        ContextCompat.registerReceiver(applicationContext, bondStateBroadcastReceiver, monitorBondState, ContextCompat.RECEIVER_EXPORTED)
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
        // Ensure the central manager has not been closed.
        ensureOpen()

        val adapter = manager?.adapter ?: throw BluetoothUnavailableException()
        return ids.map { id ->
            peripheral(id) {
                Peripheral(
                    scope = scope,
                    impl = NativeExecutor(applicationContext, adapter.getRemoteDevice(it), null)
                        .apply {
                            _bondState
                                .filter { (address, _) -> address == id }
                                .onEach { (_, state) -> onBondStateChanged(state) }
                                .launchIn(scope)
                        }
                )
            }
        }
    }

    override fun getBondedPeripherals(): List<Peripheral> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        // Verify the BLUETOOTH_CONNECT permission is granted (Android 12+).
        checkConnectPermission()

        val adapter = manager?.adapter ?: throw BluetoothUnavailableException()
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
        val scanner = manager?.adapter
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
                                impl = NativeExecutor(applicationContext, device, name)
                                    .apply {
                                        _bondState
                                            .filter { (address, _) -> address == device.address }
                                            .onEach { (_, state) -> onBondStateChanged(state) }
                                            .launchIn(scope)
                                    }
                            )
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
                    logger.error(message)
                    close(this)
                }
            }
        }

        // Finally, start the scan.
        filters?.let {
            logger.trace("Starting scanning with filters: {}", it)
        } ?: logger.trace("Starting scanning with no filters")
        scanner.startScan(filters?.toNative(), settings, callback)

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
            scanner.stopScan(callback)
            logger.trace("Scanning stopped")
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
        applicationContext.unregisterReceiver(bondStateBroadcastReceiver)

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