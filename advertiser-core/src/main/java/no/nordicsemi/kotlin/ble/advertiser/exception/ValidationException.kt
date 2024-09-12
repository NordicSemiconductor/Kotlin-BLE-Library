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

package no.nordicsemi.kotlin.ble.advertiser.exception

data class ValidationException(
    val reason: Reason
): IllegalStateException("Invalid advertising data, reason: $reason") {

    /**
     * An enum that represents the reason why the advertising data is invalid.
     */
    enum class Reason {
        /** Failed to start advertising as the advertise data to be broadcast is larger than 31 bytes. */
        DATA_TOO_LARGE,
        /** Requested PHY is not supported on this platform. */
        PHY_NOT_SUPPORTED,
        /** Periodic advertising is not supported on this platform. */
        EXTENDED_ADVERTISING_NOT_SUPPORTED,
        /** Failed to start advertising due to illegal parameters. */
        ILLEGAL_PARAMETERS,
        /** Scan response is required for scannable advertisement, but not provided. */
        SCAN_RESPONSE_REQUIRED,
        /** Scan response is not allowed for non-scannable and non-connectable advertisement. */
        SCAN_RESPONSE_NOT_ALLOWED;

        override fun toString() = when (this) {
            DATA_TOO_LARGE -> "Data too large"
            PHY_NOT_SUPPORTED -> "PHY not supported"
            EXTENDED_ADVERTISING_NOT_SUPPORTED -> "Extended advertising not supported"
            ILLEGAL_PARAMETERS -> "Illegal value of maxAdvertisingEvents or timeout parameters"
            SCAN_RESPONSE_REQUIRED -> "Scan response is required for scannable non-legacy advertisement"
            SCAN_RESPONSE_NOT_ALLOWED -> "Scan response is not allowed for non-scannable and non-connectable advertisement"
        }
    }
}