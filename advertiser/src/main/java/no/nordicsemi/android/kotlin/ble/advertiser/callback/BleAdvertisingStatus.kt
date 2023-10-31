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

package no.nordicsemi.android.kotlin.ble.advertiser.callback

import android.bluetooth.le.AdvertisingSetCallback

/**
 * An advertise process status.
 *
 * @property value Native Android API value.
 *
 * @see [AdvertisingSetCallback](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback)
 */
enum class BleAdvertisingStatus(internal val value: Int) {

    /**
     * Some manufactures adds their custom codes. This value means that status code couldn't be
     * parsed using standard values.
     */
    UNKNOWN(99),

    /**
     * Failed to start advertising as the advertising is already started.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED]
     */
    ADVERTISE_FAILED_ALREADY_STARTED(3),

    /**
     * Failed to start advertising as the advertise data to be broadcasted is too large.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE]
     */
    ADVERTISE_FAILED_DATA_TOO_LARGE(1),

    /**
     * This feature is not supported on this platform.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED]
     */
    ADVERTISE_FAILED_FEATURE_UNSUPPORTED(5),

    /**
     * Operation failed due to an internal error.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR]
     */
    ADVERTISE_FAILED_INTERNAL_ERROR(4),

    /**
     * Failed to start advertising because no advertising instance is available.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS]
     */
    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS(2),

    /**
     * The requested operation was successful.
     *
     * @see [AdvertisingSetCallback.ADVERTISE_SUCCESS]
     */
    ADVERTISE_SUCCESS(0);

    companion object {
        fun create(value: Int): BleAdvertisingStatus {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
