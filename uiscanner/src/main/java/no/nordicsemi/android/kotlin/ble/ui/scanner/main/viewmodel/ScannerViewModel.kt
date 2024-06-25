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
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanFailedError
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanningFailedException
import no.nordicsemi.android.kotlin.ble.ui.scanner.Filter
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScannerRepository
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.internal.ScanFilterState
import javax.inject.Inject

@HiltViewModel
internal class ScannerViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
) : ViewModel() {
    private var filters: List<Filter> = emptyList()

    private val _filterConfig = MutableStateFlow<List<ScanFilterState>>(emptyList())
    val filterConfig = _filterConfig.asStateFlow()

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
                // To prevent lags on the device list only refresh the list when
                // it has changed. This simple implementation just checks if
                // any new device was found, which isn't the best, as devices may change
                // advertising data and this won't be shown until some new device is found.
                val shouldRefresh = when (val list = _state.value) {
                    is ScanningState.DevicesDiscovered -> list.devices.size != it.size
                    else -> true
                }
                if (shouldRefresh) {
                    _state.value = ScanningState.DevicesDiscovered(it)
                }
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
    private fun List<BleScanResults>.applyFilters(config: List<ScanFilterState>) =
            filter { result ->
                config.all {
                    it.predicate(it.selected, result)
                }
            }

    fun setFilters(filters: List<Filter>) {
        this.filters = filters
        this._filterConfig.update {
            filters.map {
                ScanFilterState(it.title, it.initiallySelected, it.filter)
            }
        }
    }

    fun toggleFilter(index: Int) {
        this._filterConfig.value = mutableListOf<ScanFilterState>()
                .apply { addAll(_filterConfig.value) }
                .apply {
                    this[index] = this[index].copy(selected = !this[index].selected)
                }
    }

    fun refresh() {
        relaunchScanning()
    }
}
