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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.kotlin.ble.core.android.AndroidEnvironment
import no.nordicsemi.kotlin.ble.environment.android.compose.LocalEnvironmentOwner

@Composable
fun ScannerScreen() {
    val environment = LocalEnvironmentOwner.current
    val vm = hiltViewModel<ScannerViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val devices by vm.peripherals.collectAsStateWithLifecycle()
    val isScanning by vm.isScanning.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Bluetooth state: $state")

        // Scanning requires BLUETOOTH_SCAN permission, but
        // reading device name or bond state requires BLUETOOTH_CONNECT permission.
        val permissions = arrayOf(
            AndroidEnvironment.Permission.BLUETOOTH_SCAN,
            AndroidEnvironment.Permission.BLUETOOTH_CONNECT,
        )
        var permissionGranted by remember {
            mutableStateOf(environment.isBluetoothScanPermissionGranted && environment.isBluetoothConnectPermissionGranted)
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = {
                // This may not work.
                // permissionGranted = it.values.all { true }
                // Use this instead:
                permissionGranted = environment.isBluetoothScanPermissionGranted && environment.isBluetoothConnectPermissionGranted
            }
        )

        if (permissionGranted) {
            // Both Bluetooth and Location permissions are granted.
            // We can now start scanning.
            ScannerView(
                devices = devices,
                isScanning = isScanning,
                onStartScan = {
                    if (!isScanning)
                        vm.onScanRequested()
                    else
                        vm.onStopScanRequested()
                },
                onPeripheralClicked = vm::onPeripheralSelected,
                onBondRequested = vm::onBondRequested,
                onRemoveBondRequested = vm::onRemoveBondRequested,
                onClearCacheRequested = vm::onClearCacheRequested,
            )
        } else {
            Button(
                onClick = {  launcher.launch(permissions) }
            ) {
                Text("Grant required permissions")
            }
        }
    }
}