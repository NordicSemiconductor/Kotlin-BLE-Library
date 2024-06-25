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

package no.nordicsemi.android.kotlin.ble.ui.scanner.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.ScanningState
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.internal.ScanEmptyView
import no.nordicsemi.android.kotlin.ble.ui.scanner.view.internal.ScanErrorView
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults

@Composable
fun DevicesListView(
    isLocationRequiredAndDisabled: Boolean,
    state: ScanningState,
    onClick: (BleScanResults) -> Unit,
    modifier: Modifier = Modifier,
    deviceItem: @Composable (BleScanResults) -> Unit = {
        DeviceListItem(it.advertisedName ?: it.device.name, it.device.address)
    },
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        when (state) {
            is ScanningState.Loading -> item { ScanEmptyView(isLocationRequiredAndDisabled) }
            is ScanningState.DevicesDiscovered -> {
                if (state.isEmpty()) {
                    item { ScanEmptyView(isLocationRequiredAndDisabled) }
                } else {
                    DeviceListItems(state, onClick, deviceItem)
                }
            }
            is ScanningState.Error -> item { ScanErrorView(state.errorCode) }
        }
    }
}

@Preview(showBackground = true, name = "Location required")
@Composable
private fun DeviceListView_Preview_LocationRequired() {
    MaterialTheme {
        DevicesListView(
            isLocationRequiredAndDisabled = true,
            state = ScanningState.Loading,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListView_Preview_LocationNotRequired() {
    MaterialTheme {
        DevicesListView(
            isLocationRequiredAndDisabled = false,
            state = ScanningState.Loading,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListView_Preview_Error() {
    MaterialTheme {
        DevicesListView(
            isLocationRequiredAndDisabled = true,
            state = ScanningState.Error(1),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceListView_Preview_Empty() {
    MaterialTheme {
        DevicesListView(
            isLocationRequiredAndDisabled = true,
            state = ScanningState.DevicesDiscovered(emptyList()),
            onClick = {}
        )
    }
}
