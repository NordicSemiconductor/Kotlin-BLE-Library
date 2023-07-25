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

import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Set match mode ([BleScannerMatchMode]) for Bluetooth LE scan filters hardware match.
 *
 * @property value Native Android API value.
 * @see [ScanSettings.Builder](https://developer.android.com/reference/android/bluetooth/le/ScanSettings.Builder#setMatchMode(int))
 */
@RequiresApi(Build.VERSION_CODES.M)
enum class BleScannerMatchMode(val value: Int){
    /**
     * In Aggressive mode, hw will determine a match sooner even with feeble signal strength and
     * few number of sightings/match in a duration.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    MATCH_MODE_AGGRESSIVE(ScanSettings.MATCH_MODE_AGGRESSIVE),

    /**
     * For sticky mode, higher threshold of signal strength and sightings is required before
     * reporting by hw.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    MATCH_MODE_STICKY(ScanSettings.MATCH_MODE_STICKY);
}
