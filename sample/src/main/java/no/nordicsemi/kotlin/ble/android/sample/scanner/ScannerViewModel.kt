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

package no.nordicsemi.kotlin.ble.android.sample.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val centralManager: CentralManager,
    // We're not using ViewModelScope. For test purposes it's better to create a custom Scope,
    // also connected to the ViewModel lifecycle, but which can be replaced in tests.
    private val scope: CoroutineScope,
): ViewModel() {
    val state = centralManager.state

    private val _devices: MutableStateFlow<List<Peripheral>> = MutableStateFlow(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var connectionScope = mutableMapOf<Peripheral, CoroutineScope>()

    fun startScan() {
        // Add a preview peripheral. Normally you may add it only in composable previews.
        _devices.update {
            listOf(
                PreviewPeripheral(scope, phy = PhyInUse(txPhy = Phy.PHY_LE_1M, rxPhy = Phy.PHY_LE_2M))
                    .apply {
                        state
                            .onEach {
                                Timber.i("State: $it")
                                // Cancel connection scope, so that previously launched jobs are cancelled.
                                if (it is ConnectionState.Disconnected) {
                                    connectionScope.remove(this)?.cancel()
                                }
                            }
                            .onCompletion {
                                Timber.d("State collection completed")
                            }
                            .launchIn(scope)
                        bondState
                            .onEach {
                                Timber.i("Bond state: $it")
                            }
                            .onCompletion {
                                Timber.d("Bond state collection completed")
                            }
                            .launchIn(scope)
                    }
            )
        }

        _isScanning.update { true }
        centralManager
            .scan(1250.milliseconds) {
                Any {
                    Name("Pixel 5")
                    Name("Pixel 7")
                    Name("Nordic_LBS")
                    Name("DFU2A16")
                    Name("Mesh Light")
                    // TODO Filtering by Regex and other runtime filters
                }
            }
            .distinctByPeripheral()
            .map {
                it.peripheral
            }
            //.distinct()
            .onEach { newPeripheral ->
                Timber.i("Found new device: ${newPeripheral.name} (${newPeripheral.address})")
                _devices.update { devices.value + newPeripheral }
            }
            .onEach { peripheral ->
                // Track state of each peripheral.
                peripheral.state
                    .onEach {
                        Timber.i("State: $it")
                        // Cancel connection scope, so that previously launched jobs are cancelled.
                        if (it is ConnectionState.Disconnected) {
                            connectionScope.remove(peripheral)?.cancel()
                        }
                    }
                    .onCompletion {
                        Timber.d("State collection completed")
                    }
                    .launchIn(scope)
                // Track bond state of each peripheral.
                peripheral.bondState
                    .onEach {
                        Timber.i("Bond state: $it")
                    }
                    .onCompletion {
                        Timber.d("Bond state collection completed")
                    }
                    .launchIn(scope)
            }
            .catch { t ->
                Timber.e("Scan failed: $t")
            }
            .onCompletion {
                _isScanning.update { false }
            }
            .launchIn(scope)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun connect(peripheral: Peripheral, autoConnect: Boolean) {
        if (!peripheral.isDisconnected) {
            return
        }
        connectionScope[peripheral] = CoroutineScope(context = Dispatchers.IO).apply {
            launch {
                try {
                    withTimeout(5000) {
                        Timber.v("Connecting to ${peripheral.name}...")
                        centralManager.connect(
                            peripheral = peripheral,
                            options = if (autoConnect) {
                                CentralManager.ConnectionOptions.AutoConnect
                            } else {
                                CentralManager.ConnectionOptions.Direct(
                                    timeout = 24.seconds,
                                    retry = 2,
                                    retryDelay = 1.seconds,
                                    Phy.PHY_LE_2M,
                                )
                            },
                        )
                    }
                    Timber.i("Connected to ${peripheral.name}!")

                    // Observe PHY
                    peripheral.phy
                        .onEach {
                            Timber.i("PHY changed to: $it")
                        }
                        .onEmpty {
                            Timber.w("PHY didn't change")
                        }
                        .onCompletion {
                            Timber.d("PHY collection completed")
                        }
                        .launchIn(this)

                    // Observe connection parameters
                    peripheral.connectionParameters
                        .onEach {
                            Timber.i("Connection parameters changed to: $it")
                        }
                        .onEmpty {
                            Timber.w("Connection parameters didn't change")
                        }
                        .onCompletion {
                            Timber.d("Connection parameters collection completed")
                        }
                        .launchIn(this)

                    // Request MTU
                    peripheral.requestHighestValueLength()

                    // Check maximum write length
                    val writeType = WriteType.WITHOUT_RESPONSE
                    val length = peripheral.maximumWriteValueLength(writeType)
                    Timber.i("Maximum write length for $writeType: $length")

                    // Read RSSI
                    val rssi = peripheral.readRssi()
                    Timber.i("RSSI: $rssi dBm")

                    // Read PHY
                    val phyInUse = peripheral.readPhy()
                    Timber.i("PHY in use: $phyInUse")

                    // Request connection priority
                    peripheral.requestConnectionPriority(ConnectionPriority.HIGH)
                    Timber.i("Connection priority changed to HIGH")

                    // Discover services and do some GATT operations.
                    peripheral.services()
                        .onEach {
                            Timber.i("Services changed: $it")

                            // Read values of all characteristics.
                            it.forEach { remoteService ->
                                remoteService.characteristics.forEach { remoteCharacteristic ->
                                    try {
                                        val value = remoteCharacteristic.read()
                                        Timber.i("Value of ${remoteCharacteristic.uuid}: 0x${value.toHexString()}")
                                    } catch (e: Exception) {
                                        Timber.e("Failed to read ${remoteCharacteristic.uuid}: ${e.message}")
                                    }
                                }
                            }

                            it.forEach { remoteService ->
                                remoteService.characteristics.forEach { remoteCharacteristic ->
                                    try {
                                        Timber.w("Subscribing to ${remoteCharacteristic.uuid}...")
                                        remoteCharacteristic.subscribe()
                                            .onEach { newValue ->
                                                Timber.i("Value of ${remoteCharacteristic.uuid} changed: 0x${newValue.toHexString()}")
                                            }
                                            .onEmpty {
                                                Timber.w("No updates from ${remoteCharacteristic.uuid}")
                                            }
                                            .onCompletion {
                                                Timber.d("Stopped observing updates from ${remoteCharacteristic.uuid}")
                                            }
                                            .launchIn(scope)
                                        Timber.d("Subscribing to ${remoteCharacteristic.uuid} complete")
                                    } catch (e: Exception) {
                                        Timber.e("Failed to subscribe to ${remoteCharacteristic.uuid}: ${e.message}")
                                    }
                                }
                            }
                        }
                        .onEmpty {
                            Timber.w("No services found")
                        }
                        .onCompletion {
                            Timber.d("Service collection completed")
                        }
                        .launchIn(this)

                    // When the above operations are in progress, do some other operations in parallel.
                    // This time service discovery won't be initiated and we're just subscribing to the
                    // service flow.
                    peripheral.services()
                        .onEach {
                            Timber.i("-- Services changed: $it")

                            // Read values of all characteristics.
                            it.forEach { remoteService ->
                                remoteService.characteristics.forEach { remoteCharacteristic ->
                                    try {
                                        val value = remoteCharacteristic.read()
                                        Timber.i("-- Value of ${remoteCharacteristic.uuid}: 0x${value.toHexString()}")
                                    } catch (e: Exception) {
                                        Timber.e("-- Failed to read ${remoteCharacteristic.uuid}: ${e.message}")
                                    }
                                }
                            }
                        }
                        .onEmpty {
                            Timber.w("-- No services found")
                        }
                        .onCompletion {
                            Timber.d("-- Service collection completed")
                        }
                        .launchIn(this)
                } catch (e: Exception) {
                    Timber.e(e, "OMG!")
                }
                Timber.v("Finishing job")
            }
        }
    }

    fun disconnect(peripheral: Peripheral) {
        if (peripheral.isDisconnected) { return }
        scope.launch {
            Timber.v("Disconnecting from ${peripheral.name}...")
            try {
                peripheral.disconnect()
                Timber.i("Disconnected from ${peripheral.name}!")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        centralManager.close()
    }
}
