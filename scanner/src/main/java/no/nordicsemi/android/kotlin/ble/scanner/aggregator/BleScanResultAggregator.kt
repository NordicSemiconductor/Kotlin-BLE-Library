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

package no.nordicsemi.android.kotlin.ble.scanner.aggregator

import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

/**
 * Class responsible for aggregating scan results with a single server device.
 * By default [BleScanner] emits one [BleScanResult] at time.
 * Grouping data is a responsibility of this class.
 */
class BleScanResultAggregator {
    private val devices = mutableMapOf<ServerDevice, List<BleScanResultData>?>()
    val results
        get() = devices.map { BleScanResults(it.key, it.value ?: emptyList()) }

    /**
     * Adds new scan item to [List] and returns aggregated values.
     *
     * @param scanItem New scan item.
     * @return Aggregated values.
     */
    fun aggregate(scanItem: BleScanResult): List<BleScanResults> {
        val data = scanItem.data
        if (data != null) {
            devices[scanItem.device] = (devices[scanItem.device] ?: emptyList()) + data
        } else {
            devices[scanItem.device] = devices[scanItem.device]
        }
        return results
    }

    /**
     * Adds new scan item to [List] and returns all [ServerDevice] which advertised something.
     * Can be used in a scenario when scan record data are not important and one want only to
     * display list of devices.
     *
     * @param scanItem New scan item.
     * @return [List] of all devices which advertised something.
     */
    fun aggregateDevices(scanItem: BleScanResult): List<ServerDevice> {
        return aggregate(scanItem).map { it.device }
    }
}
