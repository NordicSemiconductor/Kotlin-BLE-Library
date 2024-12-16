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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.client.mock.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.kotlin.ble.client.mock.MockScanResult
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.client.mock.Proximity
import no.nordicsemi.kotlin.ble.core.Bluetooth5AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.mock.MockEnvironment

class MockBluetoothLeAdvertiser<ID: Any>(
    private val scope: CoroutineScope,
    private val environment: MockEnvironment,
) {
    private val _advertisingEvents = MutableSharedFlow<MockScanResult<ID>>()
    val events = _advertisingEvents.asSharedFlow()

    private var jobs = mutableListOf<Job>()

    fun simulateAdvertising(peripherals: List<PeripheralSpec<ID>>) {
        peripherals.forEach { peripheralSpec ->
            // If a peripheral has defined advertising data, begin mock advertising.
            peripheralSpec.advertisement?.let { list ->
                list.forEach { advertisementConfig ->
                    // Build the advertising parameters. We do this here once, as it doesn't change over time.
                    val rawAdvertisingData = advertisementConfig.advertisingData.raw
                    val bluetooth5AdvertisingSetParameters =
                        advertisementConfig.parameters as? Bluetooth5AdvertisingSetParameters
                    val txPowerLevel = advertisementConfig.parameters.txPowerLevel
                        .takeIf { bluetooth5AdvertisingSetParameters?.includeTxPowerLevel == true }
                    val primaryPhy =
                        bluetooth5AdvertisingSetParameters?.primaryPhy ?: PrimaryPhy.PHY_LE_1M
                    val secondaryPhy = bluetooth5AdvertisingSetParameters?.secondaryPhy

                    // With the parameters ready, we can start mock advertising.
                    // Advertising will continue until:
                    // * the timeout is reached,
                    // * the maximum number of advertising events is reached,
                    // * the simulation is tear down
                    // * the scope is closed
                    val job = scope.launch {
                        // First, delay the advertising if needed.
                        delay(advertisementConfig.delay)

                        // Set up the timer to cancel advertising after the timeout.
                        // The default timeout is set to INFINITE.
                        withTimeoutOrNull(advertisementConfig.timeout) {
                            // Set up event counter.
                            repeat(advertisementConfig.maxAdvertisingEvents) {
                                // The device is advertising only when disconnected or when connected and
                                // the "isAdvertisingWhenConnected" flag is set.
                                if (!peripheralSpec.isConnected || advertisementConfig.isAdvertisingWhenConnected) {
                                    // Make sure the device is in range.
                                    if (peripheralSpec.proximity != Proximity.OUT_OF_RANGE) {
                                        // Emit the advertising event.
                                        val scanResult = MockScanResult(
                                            identifier = peripheralSpec.identifier,
                                            isConnectable = advertisementConfig.parameters.connectable,
                                            advertisingData = rawAdvertisingData,
                                            rssi = peripheralSpec.proximity.randomRssi(),
                                            txPowerLevel = txPowerLevel,
                                            primaryPhy = primaryPhy,
                                            secondaryPhy = secondaryPhy,
                                            timestamp = System.currentTimeMillis() // TODO different time
                                        )
                                        _advertisingEvents.emit(scanResult)
                                    }
                                }

                                // Wait the advertising interval for the next advertising event.
                                delay(advertisementConfig.parameters.interval)
                            }
                        }
                    }
                    jobs.add(job)
                }
            }
        }
    }

    fun cancel() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

}