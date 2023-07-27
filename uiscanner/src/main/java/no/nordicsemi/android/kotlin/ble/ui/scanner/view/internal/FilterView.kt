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

package no.nordicsemi.android.kotlin.ble.ui.scanner.view.internal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.scanner.R
import no.nordicsemi.android.kotlin.ble.ui.scanner.repository.DevicesScanFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterView(
    config: DevicesScanFilter,
    onChanged: (DevicesScanFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier,
    ) {
        config.filterUuidRequired.let {
            ElevatedFilterChip(
                selected = !it,
                onClick = { onChanged(config.copy(filterUuidRequired = !it)) },
                label = { Text(text = stringResource(id = R.string.filter_uuid),) },
                modifier = Modifier.padding(end = 8.dp),
                leadingIcon = {
                    if (!it) {
                        Icon(Icons.Default.Done, contentDescription = "")
                    } else {
                        Icon(Icons.Default.Widgets, contentDescription = "")
                    }
                },
            )
        }
        config.filterNearbyOnly.let {
            ElevatedFilterChip(
                selected = it,
                onClick = { onChanged(config.copy(filterNearbyOnly = !it)) },
                label = { Text(text = stringResource(id = R.string.filter_nearby),) },
                modifier = Modifier.padding(end = 8.dp),
                leadingIcon = {
                    if (it) {
                        Icon(Icons.Default.Done, contentDescription = "")
                    } else {
                        Icon(Icons.Default.Wifi, contentDescription = "")
                    }
                },
            )
        }
        config.filterWithNames.let {
            ElevatedFilterChip(
                selected = it,
                onClick = { onChanged(config.copy(filterWithNames = !it)) },
                label = { Text(text = stringResource(id = R.string.filter_name),) },
                modifier = Modifier.padding(end = 8.dp),
                leadingIcon = {
                    if (it) {
                        Icon(Icons.Default.Done, contentDescription = "")
                    } else {
                        Icon(Icons.Default.Label, contentDescription = "")
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun FilterViewPreview() {
    NordicTheme {
        FilterView(
            config = DevicesScanFilter(
                filterUuidRequired = true,
                filterNearbyOnly = true,
                filterWithNames = true,
            ),
            onChanged = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}