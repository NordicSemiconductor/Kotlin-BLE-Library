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

import android.Manifest
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.advertiser.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingPayload
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.advertiser.android.NativeBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toLegacy
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toNative
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Class responsible for starting advertisements on Android API level < 26.
 *
 * @constructor Creates an instance of an advertiser.
 *
 * @param context An Application context.
 */
internal class BluetoothLeAdvertiserLegacy(
    context: Context,
) : NativeBluetoothLeAdvertiser(context) {
    private val logger: Logger = LoggerFactory.getLogger(BluetoothLeAdvertiserLegacy::class.java)

    @RequiresPermission(value = Manifest.permission.BLUETOOTH_ADMIN)
    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        timeout: Duration,
        block: ((txPower: Int) -> Unit)?
    ) {
        // First, let's validate the advertising data.
        // This will be done later, when the advertising is started, but this way we may throw
        // an exact reason.
        validator.validate(parameters, payload)
        timeoutValidator.validate(timeout, 0)

        // Check if Bluetooth is enabled and can advertise.
        if (!isBluetoothEnabled()) {
            logger.error("Advertising failed to start: Bluetooth is disabled or not available")
            throw AdvertisingNotStartedException(
                reason = AdvertisingNotStartedException.Reason.BLUETOOTH_NOT_AVAILABLE
            )
        }
        val advertiser = bluetoothLeAdvertiser
        if (advertiser == null) {
            logger.error("Advertising failed to start: Bluetooth LE advertiser is null")
            throw AdvertisingNotStartedException(
                reason = AdvertisingNotStartedException.Reason.FEATURE_UNSUPPORTED
            )
        }

        // If all is fine, let's start advertising.
        suspendCancellableCoroutine { continuation ->
            var timeoutJob: Job? = null

            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    logger.info("Advertising started")

                    // Legacy advertising doesn't have any callback for the timeout, so we need to
                    // start a coroutine that will resume the method. Advertising should stop on
                    // its own.
                    // The max number of advertising events is converted to timeout if set,
                    // so it's enough to check only the timeout.
                    if (settingsInEffect.timeout > 0) {
                        @OptIn(DelicateCoroutinesApi::class)
                        timeoutJob = GlobalScope.launch {
                            delay(settingsInEffect.timeout.toLong())
                            logger.info("Advertising timed out")
                            continuation.resume(Unit)
                        }
                    }

                    // Notify the caller that the advertising has started.
                    block?.invoke(settingsInEffect.txPowerLevel)
                }

                override fun onStartFailure(errorCode: Int) {
                    logger.error("Advertising failed to start: error $errorCode")
                    continuation.resumeWithException(
                        AdvertisingNotStartedException(reason = errorCode.toReason())
                    )
                }
            }

            // Start advertising.
            try {
                advertiser.startAdvertising(
                    parameters.toLegacy(timeout),
                    payload.advertisingData.toNative(),
                    payload.scanResponse?.toNative(),
                    callback,
                )
                logger.info("Advertising initiated")
            } catch (e: IllegalArgumentException) {
                logger.error("Illegal advertising set parameters", e)
                continuation.resumeWithException(
                    AdvertisingNotStartedException(
                        reason = AdvertisingNotStartedException.Reason.UNKNOWN
                    )
                )
                return@suspendCancellableCoroutine
            } catch (e: IllegalStateException) {
                logger.error("Failed to start advertising", e)
                continuation.resumeWithException(
                    AdvertisingNotStartedException(
                        reason = AdvertisingNotStartedException.Reason.BLUETOOTH_NOT_AVAILABLE
                    )
                )
                return@suspendCancellableCoroutine
            }

            // Cancel the advertising when the coroutine is cancelled.
            continuation.invokeOnCancellation {
                logger.info("Advertising cancelled: stopping advertising")
                timeoutJob?.cancel()
                bluetoothLeAdvertiser?.stopAdvertising(callback)
            }
        }
    }

    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        timeoutValidator.validate(Duration.INFINITE, maxAdvertisingEvents)

        val timeout: Duration = when {
            // If maxAdvertisingEvents is not set, there is no timeout.
            // This should not be possible, as maxAdvertisingEvents is in range 1..255.
            maxAdvertisingEvents <= 0 -> Duration.INFINITE
            // If maxAdvertisingEvents is set, convert it to timeout using the advertising interval.
            else -> parameters.interval.millis.milliseconds * maxAdvertisingEvents
        }
        advertise(parameters, payload, timeout, block)
    }
}
