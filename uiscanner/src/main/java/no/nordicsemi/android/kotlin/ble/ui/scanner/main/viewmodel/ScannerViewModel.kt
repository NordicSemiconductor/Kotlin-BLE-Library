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

package no.nordicsemi.android.kotlin.ble.ui.scanner.main.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.DevicesScanFilter
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScannerRepository
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanFailedError
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanningFailedException
import javax.inject.Inject

private const val FILTER_RSSI = -50 // [dBm]

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
) : ViewModel() {
    private var uuid: ParcelUuid? = null

    val filterConfig = MutableStateFlow(
        DevicesScanFilter(
            filterUuidRequired = true,
            filterNearbyOnly = false,
            filterWithNames = true
        )
    )

    private var currentJob: Job? = null

    private val _state = MutableStateFlow<ScanningState>(ScanningState.Loading)
    val state = _state.asStateFlow()

    init {
        relaunchScanning()
    }

    private fun relaunchScanning() {
        currentJob?.cancel()
        val aggregator = BleScanResultAggregator()
        currentJob = scannerRepository.getScannerState()
            .map { aggregator.aggregate(it) }
            .filter { it.isNotEmpty() }
            .combine(filterConfig) { result, config  ->
                result.applyFilters(config)
            }
            .onStart { _state.value = ScanningState.Loading }
            .cancellable()
            .onEach {
                _state.value = ScanningState.DevicesDiscovered(it)
            }
            .catch { e ->
                _state.value = (e as? ScanningFailedException)?.let {
                    ScanningState.Error(it.errorCode.value)
                } ?: ScanningState.Error(ScanFailedError.UNKNOWN.value)
            }
            .launchIn(viewModelScope)
    }

    // This can't be observed in View Model Scope, as it can exist even when the
    // scanner is not visible. Scanner state stops scanning when it is not observed.
    // .stateIn(viewModelScope, SharingStarted.Lazily, ScanningState.Loading)
    private fun List<BleScanResults>.applyFilters(config: DevicesScanFilter) =
            filter {
                uuid == null ||
                config.filterUuidRequired == false ||
                it.lastScanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
            }
           .filter { !config.filterNearbyOnly || it.highestRssi >= FILTER_RSSI }
           .filter { !config.filterWithNames || it.device.hasName }

    fun setFilterUuid(uuid: ParcelUuid?) {
        this.uuid = uuid
        if (uuid == null) {
            filterConfig.value = filterConfig.value.copy(filterUuidRequired = null)
        }
    }

    fun setFilter(config: DevicesScanFilter) {
        this.filterConfig.value = config
    }

    fun refresh() {
        relaunchScanning()
    }
}
