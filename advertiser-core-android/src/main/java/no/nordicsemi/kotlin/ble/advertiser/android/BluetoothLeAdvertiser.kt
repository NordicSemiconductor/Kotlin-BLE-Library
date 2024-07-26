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

import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.advertiser.exception.GenericBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.exception.InvalidAdvertisingDataException
import org.jetbrains.annotations.Range
import kotlin.time.Duration

/**
 * Base advertiser interface for Android.
 *
 * This interface extends [GenericBluetoothLeAdvertiser] and adds Android-specific methods.
 * For example, it is not possible to set the local device name on Android. The name of the device
 * is used, and user can only control whether it should be included in the advertising data.
 *
 * Use [name] property to get or set the device name (it will affect all applications).
 */
interface BluetoothLeAdvertiser:
    GenericBluetoothLeAdvertiser<AdvertisingSetParameters, AdvertisingPayload> {

    /**
     * Starts Bluetooth LE advertising using given parameters.
     *
     * @param parameters Advertising parameters describing how the data are to be advertised.
     * @param payload Advertising data to be broadcast.
     * @param timeout The advertising time limit. May not exceed 180.000 ms on Android 5-7 and
     * 655.350 ms on Android 8+. By default there is no timeout set.
     * @param block A block that will be called when the advertising is started. The block will
     * receive the actual TX power (in dBm) used for advertising.
     * @throws AdvertisingNotStartedException If the advertising could not be started.
     * @throws InvalidAdvertisingDataException If the advertising data is invalid.
     */
    override suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        timeout: Duration,
        block: ((txPower: Int) -> Unit)?
    )

    /**
     * Starts Bluetooth LE advertising using given parameters.
     *
     * @param parameters Advertising parameters describing how the data are to be advertised.
     * @param payload Advertising data to be broadcast.
     * @param maxAdvertisingEvents The maximum number of advertising events, in range 1-255.
     * @param block A block that will be called when the advertising is started. The block will
     * receive the actual TX power (in dBm) used for advertising.
     * @throws AdvertisingNotStartedException If the advertising could not be started.
     * @throws InvalidAdvertisingDataException If the advertising data is invalid.
     */
    suspend fun advertise(
        parameters: AdvertisingSetParameters,
        payload: AdvertisingPayload,
        maxAdvertisingEvents: @Range(from = 1L, to = 255L) Int,
        block: ((txPower: Int) -> Unit)?
    )

    /**
     * The local Bluetooth adapter name, or null on error.
     *
     * On Android, it is this name that is advertised when
     * [AdvertisingPayload.AdvertisingData.includeDeviceName] is enabled.
     */
    var name: String?

    /**
     * The maximum advertising data length supported by the Bluetooth adapter.
     *
     * @param legacy Whether the legacy advertising data length should be returned.
     */
    fun getMaximumAdvertisingDataLength(legacy: Boolean): Int

    companion object Factory
}