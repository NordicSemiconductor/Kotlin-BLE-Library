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

package no.nordicsemi.kotlin.ble.advertiser.android

import kotlinx.coroutines.CancellableContinuation
import no.nordicsemi.kotlin.ble.advertiser.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.internal.AdvertisingParametersValidator
import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.advertiser.exception.ValidationException
import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.LegacyAdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.android.AdvertisingData
import no.nordicsemi.kotlin.ble.core.android.internal.AdvertisingDataScopeImpl
import org.jetbrains.annotations.Range
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

/**
 * Base advertiser interface for Android.
 *
 * This interface extends [BluetoothLeAdvertiser] and adds Android-specific methods.
 * For example, it is not possible to set the local device name on Android. The name of the device
 * is used, and user can only control whether it should be included in the advertising data.
 *
 * Use [name] property to get or set the device name (it will affect all applications).
 */
abstract class BluetoothLeAdvertiser: BluetoothLeAdvertiser<AdvertisingPayload> {

    companion object Factory

    override suspend fun advertise(
        connectable: Boolean,
        payload: AdvertisingPayload,
        timeout: Duration,
        block: ((txPower: Int) -> Unit)?
    ) {
        val parameters = LegacyAdvertisingSetParameters(connectable)
        advertise(parameters, payload, timeout, 0, block)
    }

    /**
     * Starts Bluetooth LE advertising using given parameters.
     *
     * @param parameters Advertising parameters describing how the data are to be advertised.
     * @param payload Advertising data to be broadcast.
     * @param timeout The advertising time limit. May not exceed 180.000 ms on Android 5-7 and
     * 655.350 ms on Android 8+. By default there is no timeout set.
     * @param block A block that will be called when the advertising is started. The block will
     * receive the actual TX power (in dBm) used for advertising.
     * @throws SecurityException If the BLUETOOTH_ADVERTISE permission is denied.
     * @throws AdvertisingNotStartedException If the advertising could not be started.
     * @throws ValidationException If the advertising data is invalid.
     */
    suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        timeout: Duration,
        block: ((txPower: Int) -> Unit)?
    ) {
        advertise(parameters, payload, timeout, 0, block)
    }

    /**
     * Starts Bluetooth LE advertising using given parameters.
     *
     * @param parameters Advertising parameters describing how the data are to be advertised.
     * @param payload Advertising data to be broadcast.
     * @param maxAdvertisingEvents The maximum number of advertising events, in range 1-255.
     * @param block A block that will be called when the advertising is started. The block will
     * receive the actual TX power (in dBm) used for advertising.
     * @throws SecurityException If the BLUETOOTH_ADVERTISE permission is denied.
     * @throws AdvertisingNotStartedException If the advertising could not be started.
     * @throws ValidationException If the advertising data is invalid.
     */
    suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        maxAdvertisingEvents: @Range(from = 1L, to = 255L) Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        var timeout = Duration.INFINITE
        var maxExtendedAdvertisingEvents = maxAdvertisingEvents

        // When user requested max advertising events but extended advertising is not supported,
        // convert it to a timeout using the advertising interval.
        if (!isLeExtendedAdvertisingSupported) {
            timeout = parameters.interval * maxAdvertisingEvents
            maxExtendedAdvertisingEvents = 0
        }

        advertise(parameters, payload, timeout, maxExtendedAdvertisingEvents, block)
    }

    private suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        timeout: Duration,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    ) {
        val advertisingData = payload.advertisingData.let { builder ->
            AdvertisingDataScopeImpl().apply(builder).build()
        }
        val scanResponse = payload.scanResponse?.let { builder ->
            AdvertisingDataScopeImpl().apply(builder).takeIf { !it.isEmpty }?.build()
        }
        // First, let's validate input.
        // On newer Android versions this will also be done by the system when the advertising
        // is started, but this way we may throw an exact reason.
        validator.validate(parameters, advertisingData, scanResponse)
        timeoutValidator.validate(timeout, maxAdvertisingEvents)

        // Check if Bluetooth is enabled and can advertise.
        check(isBluetoothEnabled()) {
            throw AdvertisingNotStartedException(
                reason = AdvertisingNotStartedException.Reason.BLUETOOTH_NOT_AVAILABLE
            )
        }

        // Check if the BLUETOOTH_ADVERTISE permission is granted.
        checkAdvertisePermission()

        startAdvertising(
            parameters = parameters,
            advertisingData = advertisingData,
            scanResponse = scanResponse,
            timeout = timeout,
            maxAdvertisingEvents = maxAdvertisingEvents,
            block = block
        )
    }

    /**
     * This method should start advertising using the given parameters.
     *
     * If Advertising Extension is not supported, the [maxAdvertisingEvents] will be 0
     * and the value already converted to [timeout].
     */
    protected abstract suspend fun startAdvertising(
        parameters: AdvertisingSetParameters,
        advertisingData: AdvertisingData,
        scanResponse: AdvertisingData?,
        timeout: Duration,
        maxAdvertisingEvents: Int,
        block: ((txPower: Int) -> Unit)?
    )

    /**
     * The local Bluetooth adapter name, or null on error.
     *
     * This name that is advertised as local name when included in the advertising data.
     *
     * @throws IllegalArgumentException If the name set is null.
     * @throws SecurityException If the BLUETOOTH_CONNECT permission is denied.
     */
    abstract var name: String?

    /**
     * The local Bluetooth adapter name, or null on error.
     *
     * @see name
     */
    val nameOrNull: String?
        get() = try { name } catch (_: Exception) { null }

    /**
     * The maximum advertising data length supported by the Bluetooth adapter.
     *
     * @param legacy Whether the legacy advertising data length should be returned.
     */
    abstract fun getMaximumAdvertisingDataLength(legacy: Boolean): Int

    /**
     * Validator for the advertising data.
     */
    protected abstract val validator: AdvertisingDataValidator

    /**
     * Validator for the advertising parameters.
     */
    protected abstract val timeoutValidator: AdvertisingParametersValidator

    /**
     * Checks if Bluetooth adapter is enabled.
     */
    protected abstract fun isBluetoothEnabled(): Boolean

    /**
     * Checks if the BLUETOOTH_CONNECT permission is granted.
     *
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    protected abstract fun checkConnectPermission()

    /**
     * Checks if the BLUETOOTH_ADVERTISE permission is granted.
     *
     * @throws SecurityException If BLUETOOTH_ADVERTISE permission is denied.
     */
    protected abstract fun checkAdvertisePermission()

    /**
     * Checks if the LE Extended Advertising is supported.
     */
    protected abstract val isLeExtendedAdvertisingSupported: Boolean

    protected fun CancellableContinuation<Unit>.resumeWithReason(
        reason: AdvertisingNotStartedException.Reason
    ) = resumeWithException(AdvertisingNotStartedException(reason = reason))

    protected fun CancellableContinuation<Unit>.resumeWithReason(
        reason: ValidationException.Reason
    ) = resumeWithException(ValidationException(reason = reason))
}