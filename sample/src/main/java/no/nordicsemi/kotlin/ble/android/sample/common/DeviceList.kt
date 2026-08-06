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

package no.nordicsemi.kotlin.ble.android.sample.common

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.kotlin.ble.android.sample.theme.AppTheme
import no.nordicsemi.kotlin.ble.android.sample.theme.Nordic
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceList(
    devices: List<Peripheral>,
    onItemClick: (Peripheral) -> Unit,
    onBondRequested: (Peripheral) -> Unit,
    onRemoveBondRequested: (Peripheral) -> Unit,
    onClearCacheRequested: (Peripheral) -> Unit,
    onReadRssi: (Peripheral) -> Unit,
    onReadPhy: (Peripheral) -> Unit,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        contentPadding = contentPadding,
    ) {
        items(devices) { peripheral ->
            DeviceItem(
                peripheral = peripheral,
                onClick = { onItemClick(peripheral) },
                onBondRequested = { onBondRequested(peripheral) },
                onRemoveBondRequested = { onRemoveBondRequested(peripheral) },
                onClearCacheRequested = { onClearCacheRequested(peripheral) },
                onReadRssi = { onReadRssi(peripheral) },
                onReadPhy = { onReadPhy(peripheral) },
            )
        }
    }
}

@Composable
fun DeviceItem(
    peripheral: Peripheral,
    onClick: () -> Unit,
    onBondRequested: () -> Unit,
    onRemoveBondRequested: () -> Unit,
    onClearCacheRequested: () -> Unit,
    onReadRssi: () -> Unit,
    onReadPhy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        val state by peripheral.state.collectAsStateWithLifecycle()
        val animatedColor by animateColorAsState(
            targetValue = when (state) {
                is ConnectionState.Connected -> Nordic.Color.Blue
                is ConnectionState.Connecting -> Nordic.Color.Sky
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            label = "background color animation"
        )
        ElevatedCard(
            onClick = onClick,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                headlineContent = { Text(text = peripheral.name ?: "Unknown device") },
                supportingContent = { Text(text = peripheral.address) },
                leadingContent = {
                    Icon(
                        imageVector = Nordic.Icons.Bluetooth,
                        contentDescription = "Device Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(animatedColor)
                            .padding(4.dp),
                    )
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bondState by peripheral.bondState.collectAsStateWithLifecycle()
                        val isBonded = bondState == BondState.BONDED
                        if (isBonded) {
                            Icon(
                                imageVector = Nordic.Icons.Encrypted,
                                contentDescription = "Bonded",
                            )
                        }
                        var menuOpen by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { menuOpen = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                            )
                        }
                        DeviceActions(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            isBonded = isBonded,
                            onBondRequested = onBondRequested,
                            onRemoveBondRequested = onRemoveBondRequested,
                            onClearCacheRequested = onClearCacheRequested,
                            onReadRssi = onReadRssi,
                            onReadPhy = onReadPhy,
                        )
                    }
                }
            )
        }
        if (state.isConnected) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    val services by peripheral.services().collectAsStateWithLifecycle()
                    DeviceServices(services = services)
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    AppTheme {
        val scope = rememberCoroutineScope()
        DeviceList(
            modifier = Modifier.fillMaxWidth(),
            devices = listOf(
                PreviewPeripheral(
                    scope = scope,
                    address = "AA:BB:CC:DD:EE:FF",
                    name = "Mock device 1",
                    state = ConnectionState.Connected,
                    hasBondInformation = true,
                ),
                PreviewPeripheral(
                    scope = scope,
                    address = "00:11:22:33:44:55",
                    name = "Mock device 2",
                    state = ConnectionState.Connecting
                ),
                PreviewPeripheral(
                    scope = scope,
                    address = "AA:BB:CC:DD:EE:00",
                    name = "Mock device 3"
                ),
            ),
            onItemClick = {},
            onBondRequested = {},
            onRemoveBondRequested = {},
            onClearCacheRequested = {},
            onReadRssi = {},
            onReadPhy = {},
            contentPadding = PaddingValues(16.dp),
        )
    }
}