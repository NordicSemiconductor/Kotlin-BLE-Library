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

package no.nordicsemi.kotlin.ble.advertiser.android.internal.oreo

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.advertiser.android.NativeBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toNative
import no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper.toReason
import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.advertiser.exception.ValidationException
import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.android.AdvertisingData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * Class responsible for starting advertisements on Android API level >= 26.
 *
 * @constructor Creates an instance of an advertiser.
 *
 * @param context An Application context.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal class BluetoothLeAdvertiserOreo(
    context: Context,
) : NativeBluetoothLeAdvertiser(context) {
    private val logger: Logger = LoggerFactory.getLogger(BluetoothLeAdvertiserOreo::class.java)

    override suspend fun startAdvertising(
        parameters: AdvertisingSetParameters,
        advertisingData: AdvertisingData,
        scanResponse: AdvertisingData?,
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
                val callback = object : AdvertisingSetCallback() {
                    /*
                     * This method is called when the advertising set is started.
                     */
                    override fun onAdvertisingSetStarted(
                        advertisingSet: AdvertisingSet?,
                        txPower: Int,
                        status: Int
                    ) {
                        check(status == ADVERTISE_SUCCESS) {
                            logger.error("Advertising failed to start: $status")
                            continuation.resumeWithReason(status.toReason())
                            return
                        }
                        // Advertising started.
                        //
                        // Note: Method `onAdvertisingEnabled(_, true, SUCCESS)` will NOT be called afterwards.
                        //       It is only called when the advertising stops due to a timeout (with enable = false),
                        //       or when it is restarted using `advertisingSet.enableAdvertising(true, 0, 0)`.
                        logger.info("Advertising started")
                        block?.invoke(txPower)
                    }

                    /*
                     * This method is called when the advertising stops due to a timeout or reaching
                     * required number of advertising events.
                     *
                     * It could be used to modify and restart the advertising set, but we don't support
                     * that yet. It is NOT started after starting advertising initially.
                     */
                    override fun onAdvertisingEnabled(
                        advertisingSet: AdvertisingSet?,
                        enable: Boolean,
                        status: Int
                    ) {
                        if (!enable) {
                            logger.info("Advertising timed out: stopping advertising")
                            // Advertising set is disabled, it also needs to be stopped.
                            bluetoothLeAdvertiser?.stopAdvertisingSet(this)
                        }
                    }

                    /*
                     * This method is called when the advertising set is stopped using
                     * `advertiser.stopAdvertisingSet(callback)` or due to a timeout.
                     */
                    override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                        continuation.resume(Unit)
                    }
                }

                // Start advertising.
                try {
                    val timeoutMillis = when {
                        timeout == ZERO || timeout.isInfinite() -> 0     // no timeout
                        timeout < 10.milliseconds -> 1                   // 1 unit of 10 ms is the minimum
                        else -> timeout.inWholeMilliseconds.toInt() / 10 // convert to 10 ms units
                    }
                    advertiser.startAdvertisingSet(
                        parameters.toNative(),
                        advertisingData.toNative(),
                        scanResponse?.toNative(),
                        null,
                        null,
                        timeoutMillis,
                        maxAdvertisingEvents,
                        callback,
                    )
                    logger.info("Advertising initiated")
                } catch (e: IllegalArgumentException) {
                    logger.error("Illegal advertising set parameters", e)
                    // For some reason, the new advertiser throws bunch of IllegalArgumentExceptions
                    // instead of returning the status code in a callback.
                    // To get the actual reason, we need to check the messages. Seriously...
                    // (that's why we do initial validation above)
                    if (e.message?.contains("too big") == true) {
                        continuation.resumeWithReason(ValidationException.Reason.DATA_TOO_LARGE)
                    } else if (e.message?.contains("PHY") == true) {
                        continuation.resumeWithReason(ValidationException.Reason.PHY_NOT_SUPPORTED)
                    } else if (e.message?.contains("support") == true) {
                        continuation.resumeWithReason(ValidationException.Reason.EXTENDED_ADVERTISING_NOT_SUPPORTED)
                    } else if (e.message?.contains("callback") == true) {
                        continuation.resumeWithReason(AdvertisingNotStartedException.Reason.INTERNAL_ERROR)
                    } else if (e.message?.contains("out of range") == true) {
                        continuation.resumeWithReason(ValidationException.Reason.ILLEGAL_PARAMETERS)
                    } else {
                        continuation.resumeWithReason(AdvertisingNotStartedException.Reason.UNKNOWN)
                    }
                    return@suspendCancellableCoroutine
                } catch (e: IllegalStateException) {
                    logger.error("Advertising failed to start", e)
                    continuation.resumeWithReason(AdvertisingNotStartedException.Reason.BLUETOOTH_NOT_AVAILABLE)
                    return@suspendCancellableCoroutine
                } catch (e: Exception) {
                    logger.error("Failed to build advertising data", e)
                    continuation.resumeWithException(e)
                    return@suspendCancellableCoroutine
                }

                // Cancel the advertising when the coroutine is cancelled.
                continuation.invokeOnCancellation {
                    logger.info("Advertising cancelled: stopping advertising")
                    bluetoothLeAdvertiser?.stopAdvertisingSet(callback)
                }
            }
        } finally {
            logger.info("Advertising stopped")
        }
    }
}