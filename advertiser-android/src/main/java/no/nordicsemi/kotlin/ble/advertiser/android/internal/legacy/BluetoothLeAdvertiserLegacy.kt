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

package no.nordicsemi.kotlin.ble.advertiser.android.internal.legacy

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.advertiser.android.internal.NativeBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toLegacy
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toNative
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toReason
import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.android.AdvertisingDataDefinition
import no.nordicsemi.kotlin.ble.core.log.Layer
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Class responsible for starting advertisements on Android API level < 26.
 *
 * @constructor Creates an instance of an advertiser.
 * @param environment The Android-specific environment to use for the advertiser.
 */
internal class BluetoothLeAdvertiserLegacy(
    environment: NativeAndroidEnvironment,
) : NativeBluetoothLeAdvertiser(environment) {

    override suspend fun startAdvertising(
        parameters: AdvertisingSetParameters,
        advertisingData: AdvertisingDataDefinition,
        scanResponse: AdvertisingDataDefinition?,
        timeout: Duration,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        val advertiser = bluetoothLeAdvertiser
        check(advertiser != null) {
            throw AdvertisingNotStartedException(
                reason = AdvertisingNotStartedException.Reason.FEATURE_UNSUPPORTED
            )
        }

        // If all is fine, let's start advertising.
        try {
            suspendCancellableCoroutine { continuation ->
                var timeoutJob: Job? = null

                val callback = object : AdvertiseCallback() {
                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                        logger?.info(Layer.GAP) { "Advertising started" }

                        // Legacy advertising doesn't have any callback for the timeout, so we need to
                        // start a coroutine that will resume the method. Advertising should stop on
                        // its own.
                        // The max number of advertising events is converted to timeout if set,
                        // so it's enough to check only the timeout.
                        if (settingsInEffect.timeout > 0) {
                            val callback = this

                            @OptIn(DelicateCoroutinesApi::class)
                            timeoutJob = GlobalScope.launch {
                                delay(settingsInEffect.timeout.milliseconds)
                                logger?.info(Layer.GAP) { "Advertising timed out: stopping advertising" }
                                bluetoothLeAdvertiser?.stopAdvertising(callback)
                                continuation.resume(Unit)
                            }
                        }

                        // Notify the caller that the advertising has started.
                        block?.invoke(settingsInEffect.txPowerLevel)
                    }

                    override fun onStartFailure(errorCode: Int) {
                        logger?.error(Layer.GAP) { "Advertising failed to start (error: $errorCode)" }
                        continuation.resumeWithReason(errorCode.toReason())
                    }
                }

                // Start advertising.
                try {
                    advertiser.startAdvertising(
                        parameters.toLegacy(timeout),
                        advertisingData.toNative(),
                        scanResponse?.toNative(),
                        callback,
                    )
                    logger?.info(Layer.GAP) { "Advertising initiated" }
                } catch (e: IllegalArgumentException) {
                    logger?.error(Layer.GAP, e) { "Illegal advertising set parameters" }
                    continuation.resumeWithReason(
                        reason = AdvertisingNotStartedException.Reason.UNKNOWN
                    )
                    return@suspendCancellableCoroutine
                } catch (e: IllegalStateException) {
                    logger?.error(Layer.GAP, e) { "Advertising failed to start" }
                    continuation.resumeWithReason(
                        reason = AdvertisingNotStartedException.Reason.BLUETOOTH_NOT_AVAILABLE
                    )
                    return@suspendCancellableCoroutine
                } catch (e: Exception) {
                    logger?.error(Layer.GAP, e) { "Failed to build advertising data" }
                    continuation.resumeWithException(e)
                    return@suspendCancellableCoroutine
                }

                // Cancel the advertising when the coroutine is canceled.
                continuation.invokeOnCancellation {
                    logger?.info(Layer.GAP) { "Advertising cancelled: stopping advertising" }
                    timeoutJob?.cancel()
                    bluetoothLeAdvertiser?.stopAdvertising(callback)
                }
            }
        } finally {
            logger?.info(Layer.GAP) { "Advertising stopped" }
        }
    }
}
