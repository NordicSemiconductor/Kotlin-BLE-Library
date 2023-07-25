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

package no.nordicsemi.android.kotlin.ble.advertiser.error

import android.bluetooth.le.AdvertiseCallback

/**
 * Error class which is a wrapper around native Android error code returned by
 * [AdvertiseCallback.onStartFailure].
 *
 * @property value Native Android API value.
 */
enum class BleAdvertisingError(internal val value: Int) {

    /**
     * Failed to start advertising as the advertising is already started.
     *
     * @see [AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback#ADVERTISE_FAILED_ALREADY_STARTED)
     */
    ADVERTISE_FAILED_ALREADY_STARTED(AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED),

    /**
     * Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.
     *
     * @see [AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback#ADVERTISE_FAILED_DATA_TOO_LARGE)
     */
    ADVERTISE_FAILED_DATA_TOO_LARGE(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE),

    /**
     * This feature is not supported on this platform.
     *
     * @see [AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback#ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
     */
    ADVERTISE_FAILED_FEATURE_UNSUPPORTED(AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED),

    /**
     * Operation failed due to an internal error.
     *
     * @see [AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback#ADVERTISE_FAILED_INTERNAL_ERROR)
     */
    ADVERTISE_FAILED_INTERNAL_ERROR(AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR),

    /**
     * Failed to start advertising because no advertising instance is available.
     *
     * @see [AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS](https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback#ADVERTISE_FAILED_TOO_MANY_ADVERTISERS)
     */
    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS(AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS);

    companion object {
        fun create(value: Int): BleAdvertisingError {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Can't create an error for value: $value")
        }
    }
}
