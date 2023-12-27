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

package no.nordicsemi.android.kotlin.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.mock.MockDevices
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanFailedError
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanningFailedException
import no.nordicsemi.android.kotlin.ble.scanner.settings.toNative

/**
 * Wrapper class for the native Android API.
 * It has a single function that returns a flow that continuously emits single scan items.
 *
 * @constructor Initialize scanner's components.
 *
 * @param context Android context required to initialize native Android API
 */
class BleScanner(
    context: Context,
) {

    private val bluetoothManager: BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner: BluetoothLeScanner

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    /**
     * Starts scanning and emit results in the [Flow].
     * Automatically stops scanning when [CoroutineScope] of the [Flow] is closed.
     *
     * Returns [MockServerDevice] if any mock server has been registered.
     *
     * @param settings for scanning configuration
     * @return [Flow] which emits scan findings ([BleScanResult]) in chronological order
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun scan(
        filters: List<BleScanFilter> = emptyList(),
        settings: BleScannerSettings = BleScannerSettings(),
    ): Flow<BleScanResult> = callbackFlow {
        launch {
            MockDevices.devices.collect {
                it.filterKeys { it.isIncluded(filters) }
                    .filterValues { it.isIncluded(filters) }
                    .forEach { trySend(BleScanResult(it.key, it.value)) }
            }
        }

        if (settings.includeStoredBondedDevices) {
            bluetoothAdapter.bondedDevices
                .map { RealServerDevice(it) }
                .applyFilters(filters)
                .forEach { trySend(BleScanResult(it)) }
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.toScanItem()?.let { trySend(it) }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.map { it.toScanItem() }?.forEach { trySend(it) }
            }

            override fun onScanFailed(errorCode: Int) {
                close(ScanningFailedException(ScanFailedError.create(errorCode)))
            }
        }

        bluetoothLeScanner.startScan(filters.toNative(), settings.toNative(), scanCallback)

        awaitClose {
            bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    private fun List<ServerDevice>.applyFilters(filters: List<BleScanFilter>): List<ServerDevice> {
        return filter { it.isIncluded(filters) }
    }

    private fun ServerDevice.isIncluded(filters: List<BleScanFilter>): Boolean {
        return filters.isEmpty() || filters.any { isIncluded(it) }
    }

    private fun ServerDevice.isIncluded(filter: BleScanFilter): Boolean {
        val deviceName = name
        val isNameIncluded =
            deviceName == null || filter.deviceName?.let { deviceName.contains(it) } ?: true
        val isAddressIncluded = filter.deviceAddress?.let { address.contains(it) } ?: true

        return isNameIncluded || isAddressIncluded
    }

    private fun BleScanResultData.isIncluded(filters: List<BleScanFilter>): Boolean {
        return filters.isEmpty() || filters.any { isIncluded(it) }
    }

    private fun BleScanResultData.isIncluded(filter: BleScanFilter): Boolean {
        //TODO filter by remaining parameters
        val uuid = filter.serviceUuid?.uuid ?: return true
        return this.scanRecord?.serviceUuids?.contains(uuid) == true
    }
}
