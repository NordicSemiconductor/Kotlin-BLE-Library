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

package no.nordicsemi.kotlin.ble.advertiser

data class AdvertisingNotStartedException(
    val reason: Reason
): IllegalStateException("Advertising failed to start, reason: $reason") {

    /**
     * An enum that represents the reason why the advertising failed to start.
     */
    enum class Reason {
        /** Failed to start advertising due to an unknown error. */
        UNKNOWN,
        /** Failed to start advertising as the advertising is already started. */
        ALREADY_STARTED,
        /** Failed to start advertising as the advertise data to be broadcast is larger than 31 bytes. */
        DATA_TOO_LARGE,
        /** This feature is not supported on this platform. */
        FEATURE_UNSUPPORTED,
        /** Operation failed due to an internal error. */
        INTERNAL_ERROR,
        /** Failed to start advertising because no advertising instance is available. */
        TOO_MANY_ADVERTISERS,
        /** Failed to start advertising due to illegal parameters. */
        ILLEGAL_PARAMETERS,
        /** Failed to start advertising as Bluetooth adapter is disabled of not available. */
        BLUETOOTH_NOT_AVAILABLE,
        /** At least one of permissions required for advertising is denied. */
        PERMISSION_DENIED;

        override fun toString() = when (this) {
            UNKNOWN -> "Unknown error"
            ALREADY_STARTED -> "Advertising already started"
            DATA_TOO_LARGE -> "Data too large"
            FEATURE_UNSUPPORTED -> "Feature unsupported"
            INTERNAL_ERROR -> "Internal error"
            TOO_MANY_ADVERTISERS -> "Too many advertisers"
            ILLEGAL_PARAMETERS -> "Illegal parameters"
            BLUETOOTH_NOT_AVAILABLE -> "Bluetooth not available"
            PERMISSION_DENIED -> "Permission denied"
        }
    }
}