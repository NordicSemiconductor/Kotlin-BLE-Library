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

package no.nordicsemi.android.kotlin.ble.scanner.errors

import android.bluetooth.le.ScanCallback
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Reason for failed scan.
 *
 * @property value Native Android API value.
 */
enum class ScanFailedError(val value: Int) {

    /**
     * Helper value representing an unknown error.
     */
    UNKNOWN(0),

    /**
     * Fails to start scan as BLE scan with the same settings is already started by the app.
     */
    SCAN_FAILED_ALREADY_STARTED(ScanCallback.SCAN_FAILED_ALREADY_STARTED),

    /**
     * Fails to start scan as app cannot be registered.
     */
    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED(ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED),

    /**
     * Fails to start power optimized scan as this feature is not supported.
     */
    SCAN_FAILED_FEATURE_UNSUPPORTED(ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED),

    /**
     * Fails to start scan due an internal error.
     */
    SCAN_FAILED_INTERNAL_ERROR(ScanCallback.SCAN_FAILED_INTERNAL_ERROR),

    /**
     * Fails to start scan as it is out of hardware resources.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES(ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES),

    /**
     * Fails to start scan as application tries to scan too frequently.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    SCAN_FAILED_SCANNING_TOO_FREQUENTLY(ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY);

    companion object {
        fun create(value: Int): ScanFailedError {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
