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

package no.nordicsemi.android.kotlin.ble.core.scanner

import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * The data status.
 *
 * @property value Native Android API value.
 * @see [ScanResult.getDataStatus](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult?hl=en#getdatastatus)
 */
@RequiresApi(Build.VERSION_CODES.O)
enum class BleScanDataStatus(val value: Int) {

    /**
     * For chained advertisements, indicates tha the data contained in this scan result is complete.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    DATA_COMPLETE(ScanResult.DATA_COMPLETE),

    /**
     * For chained advertisements, indicates that the controller was unable to receive all chained packets and the scan result contains incomplete truncated data.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    DATA_TRUNCATED(ScanResult.DATA_TRUNCATED);

    companion object {
        fun create(value: Int): BleScanDataStatus {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleScanDataStatus for value: $value")
        }
    }
}
