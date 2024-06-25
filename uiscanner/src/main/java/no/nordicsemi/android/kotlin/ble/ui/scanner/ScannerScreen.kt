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
package no.nordicsemi.android.kotlin.ble.ui.scanner

import android.os.ParcelUuid
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.ScannerAppBar

/**
 * Based scanner filter class.
 * @property title The title to be shown on the chip.
 * @property initiallySelected Initial state of the filter.
 * @property filter The predicate applied to scanned devices.
 */
sealed class Filter(
    open val title: String,
    open val initiallySelected: Boolean,
    open val filter: (isFilterSelected: Boolean, result: BleScanResults) -> Boolean,
)

/**
 * Filter by RSSI.
 * The filter hides all devices with highest scanned RSSI lower than the given value.
 * @property rssi The minimum RSSI value.
 */
data class OnlyNearby(
    override val title: String = "Nearby",
    val rssi: Int = -50, // dBm
    override val initiallySelected: Boolean =  true,
): Filter(
    title = title,
    initiallySelected = initiallySelected,
    filter = { isFilterSelected, result ->
        !isFilterSelected || result.highestRssi >= rssi
    }
)

/**
 * Filter by device name.
 * The filter hides all devices that do not have a name.
 */
data class OnlyWithNames(
    override val title: String = "Named",
    override val initiallySelected: Boolean = true,
) : Filter(
    title = title,
    initiallySelected = initiallySelected,
    filter = { isFilterSelected, result ->
        !isFilterSelected ||  result.device.hasName || result.advertisedName?.isNotEmpty() == true
    }
)

/**
 * Filter by service UUID.
 * The filter hides all devices that do not have the given service UUID in the advertisement.
 * @property uuid The service UUID to filter by.
 */
data class WithServiceUuid(
    override val title: String,
    val uuid: ParcelUuid,
    override val initiallySelected: Boolean = true,
): Filter(
    title = title,
    initiallySelected = initiallySelected,
    filter = { isFilterSelected, result ->
        !isFilterSelected || result.device.isBonded || result.lastScanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
    }
)

/**
 * Custom filter.
 *
 * The filter shows only devices that match the given predicate.
 */
data class CustomFilter(
    override val title: String,
    override val initiallySelected: Boolean,
    override val filter: (isFilterSelected: Boolean, result: BleScanResults) -> Boolean,
): Filter(title, initiallySelected, filter)

@Composable
fun ScannerScreen(
    title: @Composable () -> Unit = { Text(stringResource(id = R.string.scanner_screen)) },
    filters: List<Filter> = emptyList(),
    cancellable: Boolean = true,
    onResult: (ScannerScreenResult) -> Unit,
    filterShape: Shape = MaterialTheme.shapes.small,
    deviceItem: @Composable (BleScanResults) -> Unit = {
        DeviceListItem(it.device.name ?: it.advertisedName, it.device.address)
    },
) {
    var isScanning by rememberSaveable { mutableStateOf(false) }

    Column {
        if (cancellable) {
            ScannerAppBar(title, isScanning) { onResult(ScanningCancelled) }
        } else {
            ScannerAppBar(title, isScanning)
        }
        ScannerView(
            filters = filters,
            onScanningStateChanged = { isScanning = it },
            onResult = { onResult(DeviceSelected(it)) },
            deviceItem = deviceItem,
            filterShape = filterShape,
        )
    }
}

@Deprecated(
    message = "Use filters with ScanFilter list instead."
)
@Composable
fun ScannerScreen(
    title: String = stringResource(id = R.string.scanner_screen),
    uuid: ParcelUuid?,
    cancellable: Boolean = true,
    onResult: (ScannerScreenResult) -> Unit,
    filterShape: Shape = MaterialTheme.shapes.small,
    deviceItem: @Composable (BleScanResults) -> Unit = {
        DeviceListItem(it.device.name ?: it.advertisedName, it.device.address)
    },
) {
    // Show the "All" filter only when UUID was set.
    val uuidFilter = if (uuid != null ) listOf(
        CustomFilter(
            title = stringResource(id = R.string.filter_uuid),
            initiallySelected = false,
            filter = { isFilterSelected, result ->
                isFilterSelected || result.device.isBonded || result.lastScanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
            }
        ),
    ) else emptyList()

    // Nearby and Name filters were always visible.
    val otherFilters = listOf(
        OnlyNearby(rssi = -50 /* dBm */, initiallySelected = false),
        OnlyWithNames(initiallySelected = true),
    )

    ScannerScreen(
        title = {  Text(text = title) },
        filters = uuidFilter + otherFilters,
        cancellable = cancellable,
        onResult = onResult,
        deviceItem = deviceItem,
        filterShape = filterShape,
    )
}