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

import no.nordicsemi.kotlin.ble.advertiser.InvalidAdvertisingDataException
import no.nordicsemi.kotlin.ble.advertiser.InvalidAdvertisingDataException.Reason
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.milliseconds

/**
 * A class that validates advertising parameters for given environment.
 *
 * @property androidSdkVersion The Android SDK version.
 * @property isLeExtendedAdvertisingSupported True if extended advertising is supported.
 * @throws InvalidAdvertisingDataException If the advertising parameters are invalid.
 */
class AdvertisingParametersValidator(
    private val androidSdkVersion: Int,
    private val isLeExtendedAdvertisingSupported: Boolean,
) {

    /**
     * Validates timeout and maximum number of advertising events.
     *
     * @param timeout The advertising timeout.
     * @param maxAdvertisingEvents The maximum number of advertising events. Available range is 0-255.
     */
    fun validate(timeout: Duration, maxAdvertisingEvents: Int) {
        if (maxAdvertisingEvents < 0 || maxAdvertisingEvents > 255) {
            throw InvalidAdvertisingDataException(Reason.ILLEGAL_PARAMETERS)
        }

        if (maxAdvertisingEvents != 0 && !isLeExtendedAdvertisingSupported) {
            throw InvalidAdvertisingDataException(Reason.EXTENDED_ADVERTISING_NOT_SUPPORTED)
        }

        // Infinite timeout is mapped to 0 (no timeout) in Mapper.
        if (timeout == INFINITE)
            return

        val maxTimeout = if (androidSdkVersion >= 26) 655_350.milliseconds else 180_000.milliseconds
        if (timeout.isNegative() || timeout > maxTimeout) {
            throw InvalidAdvertisingDataException(Reason.ILLEGAL_PARAMETERS)
        }
    }
}