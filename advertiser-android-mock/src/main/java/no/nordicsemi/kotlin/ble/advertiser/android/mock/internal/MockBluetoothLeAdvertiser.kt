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

package no.nordicsemi.kotlin.ble.advertiser.android.mock.internal

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingDataValidator
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.AdvertisingParametersValidator
import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.android.AdvertisingDataDefinition
import no.nordicsemi.kotlin.ble.core.log.Layer
import no.nordicsemi.kotlin.ble.environment.android.mock.MockAndroidEnvironment
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * A mock implementation of Bluetooth LE advertiser.
 *
 * Use this implementation to emulate Bluetooth LE advertising in tests.
 *
 * @property environment The mock environment. When advertising is requested,
 * the [MockAndroidEnvironment.advertiser] callback will be invoked to obtain the TX power level.
 * To emulate a failure, the callback should return a [Result.failure] with [AdvertisingNotStartedException].
 */
internal class MockBluetoothLeAdvertiser(
    private val environment: MockAndroidEnvironment,
): BluetoothLeAdvertiser(environment) {

    override suspend fun startAdvertising(
        parameters: AdvertisingSetParameters,
        advertisingData: AdvertisingDataDefinition,
        scanResponse: AdvertisingDataDefinition?,
        timeout: Duration,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        val result = environment.advertiser(parameters.txPowerLevel, advertisingData, scanResponse)
        val mockedTxPower = result.getOrThrow()
        logger?.info(Layer.GAP) { "Advertising initiated" }

        try {
            suspendCancellableCoroutine { continuation ->
                // Mocking advertising has no impact on other features.
                // Local advertising is not visible on scanner nor can be used to connect.
                // Let's just pretend advertising has started.
                logger?.info(Layer.GAP) { "Advertising started" }

                val duration = when {
                    timeout > Duration.ZERO && timeout != Duration.INFINITE -> timeout
                    maxAdvertisingEvents > 0 -> parameters.interval * maxAdvertisingEvents
                    else -> Duration.INFINITE
                }

                var job: Job? = null
                if (duration > Duration.ZERO) {
                    @OptIn(DelicateCoroutinesApi::class)
                    job = GlobalScope.launch {
                        delay(duration)
                        logger?.info(Layer.GAP) { "Advertising timed out: stopping advertising" }
                        continuation.resume(Unit)
                    }
                }

                // The TX power used for advertising depends on the controller.
                block?.invoke(mockedTxPower)

                continuation.invokeOnCancellation {
                    logger?.info(Layer.GAP) { "Advertising cancelled: stopping advertising" }
                    job?.cancel()
                }
            }
        } finally {
            logger?.info(Layer.GAP) { "Advertising stopped" }
        }
    }

    override val validator: AdvertisingDataValidator
        get() = AdvertisingDataValidator(
                    deviceName = environment.deviceNameOrNull ?: "",
                    isLe2MPhySupported = environment.isLe2MPhySupported,
                    isLeCodedPhySupported = environment.isLeCodedPhySupported,
                    isLeExtendedAdvertisingSupported = environment.isLeExtendedAdvertisingSupported,
                    leMaximumAdvertisingDataLength = environment.leMaximumAdvertisingDataLength,
                )

    override val timeoutValidator: AdvertisingParametersValidator
        get() = AdvertisingParametersValidator(
                    androidSdkVersion = environment.androidSdkVersion,
                )
}