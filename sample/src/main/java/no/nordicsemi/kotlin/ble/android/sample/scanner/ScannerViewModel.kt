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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.WriteType
import timber.log.Timber
import javax.inject.Inject
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

    fun startScan() {
        _devices.update { listOf(PreviewPeripheral(scope)) }
        _isScanning.update { true }
        centralManager
            .scan(1250) {
                Any {
                    Name("Pixel 5")
                    Name("Pixel 7")
                    Name("Nordic_LBS")
                    Name("DFU2A16")
                    // TODO Filtering by Regex and other runtime filters
                }
            }
            .distinctByPeripheral()
            .map {
                it.peripheral
            }
            //.distinct()
            .onEach { newPeripheral ->
                Timber.w("Found new device: ${newPeripheral.name} (${newPeripheral.address})")
                _devices.update { devices.value + newPeripheral }
            }
            .onEach { peripheral ->
                // Track state of each peripheral.
                peripheral.state
                    .onEach {
                        Timber.w("State: $it")
                    }
                    .launchIn(scope)
            }
            .catch { t ->
                Timber.e("Scan failed: $t")
            }
            .onCompletion {
                _isScanning.update { false }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun connect(peripheral: Peripheral, autoConnect: Boolean) {
        if (!peripheral.isDisconnected) { return }
        scope.launch {
            try {
                withTimeout(5000) {
                    Timber.w("Connecting to ${peripheral.name}...")
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
                Timber.w("Connected to ${peripheral.name}!")

                // Observe PHY
                peripheral.phy
                    .onEach {
                        Timber.w("PHY changed to: $it")
                    }
                    .stateIn(scope)

                // Observe connection parameters
                peripheral.connectionParameters
                    .onEach {
                        Timber.w("Connection parameters changed to: $it")
                    }
                    .stateIn(scope)

                // Request MTU
                peripheral.requestHighestValueLength()

                // Check maximum write length
                val writeType = WriteType.WITHOUT_RESPONSE
                val length = peripheral.maximumWriteValueLength(writeType)
                Timber.w("Maximum write length for $writeType: $length")

                // Read RSSI
                val rssi = peripheral.readRssi()
                Timber.w("RSSI: $rssi dBm")

                peripheral.requestConnectionPriority(ConnectionPriority.HIGH)
                Timber.w("Connection priority changed to HIGH")

                peripheral.services()
                    .onEach {
                        Timber.w("Services changed: $it")

                        // Read characteristic
                        it.forEach { remoteService ->
                            remoteService.characteristics.forEach { remoteCharacteristic ->
                                try {
                                    val value = remoteCharacteristic.read()
                                    Timber.w("Value of ${remoteCharacteristic.uuid}: 0x${value.toHexString()}")
                                } catch (e: Exception) {
                                    Timber.e("Failed to read ${remoteCharacteristic.uuid}: ${e.message}")
                                }
                            }
                        }
                    }
                    .stateIn(scope)
            } catch (e: Exception) {
                Timber.e(e, "OMG!")
            }
        }
    }

    fun disconnect(peripheral: Peripheral) {
        if (peripheral.isDisconnected) { return }
        scope.launch {
            Timber.w("Disconnecting from ${peripheral.name}...")
            try {
                peripheral.disconnect()
                Timber.w("Disconnected from ${peripheral.name}!")
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
