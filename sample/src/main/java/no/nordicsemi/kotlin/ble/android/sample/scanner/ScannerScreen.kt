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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.kotlin.ble.android.sample.common.DeviceList
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewPeripheral

@Composable
fun ScannerScreen() {
    val vm = hiltViewModel<ScannerViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val devices by vm.devices.collectAsStateWithLifecycle()
    val isScanning by vm.isScanning.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Bluetooth state: $state")

        RequireBluetooth {
            RequireLocation { isLocationRequiredAndDisabled ->
                // Both Bluetooth and Location permissions are granted.
                // We can now start scanning.
                ScannerView(
                    devices = devices,
                    isScanning = isScanning,
                    onStartScan = { vm.startScan() },
                    onPeripheralClicked = {  device ->
                        if (device.isConnected) {
                            vm.disconnect(device)
                        } else {
                            vm.connect(device, autoConnect = false)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ScannerView(
    devices: List<Peripheral>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onPeripheralClicked: (Peripheral) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onStartScan,
                enabled = !isScanning,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Start scan")
            }

            AnimatedVisibility(visible = isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DeviceList(
            modifier = Modifier.fillMaxWidth(),
            devices = devices,
            onItemClick = { onPeripheralClicked(it) },
        )
    }
}

@Preview
@Composable
fun ScannerScreenPreview() {
    NordicTheme {
        val scope = rememberCoroutineScope()
        ScannerView(
            devices = listOf(
                PreviewPeripheral(scope, "00:11:22:33:44:55", "Device 1"),
                PreviewPeripheral(scope, "11:22:33:44:55:66", "Device 2"),
                PreviewPeripheral(scope, "22:33:44:55:66:77", "Device 3"),
            ),
            isScanning = true,
            onStartScan = {},
            onPeripheralClicked = {},
        )
    }
}