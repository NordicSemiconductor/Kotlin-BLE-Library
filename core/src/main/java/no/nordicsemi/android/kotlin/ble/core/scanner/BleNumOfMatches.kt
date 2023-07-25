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
 * Sets the number of matches for Bluetooth LE scan filters hardware match.
 *
 * @property value Native Android API value.
 * @see [ScanSettings.Builder](https://developer.android.com/reference/android/bluetooth/le/ScanSettings.Builder#setNumOfMatches(int))
 */
@RequiresApi(Build.VERSION_CODES.M)
enum class BleNumOfMatches(val value: Int) {
    /**
     * Match one advertisement per filter.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    MATCH_NUM_ONE_ADVERTISEMENT(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT),

    /**
     * Match few advertisement per filter, depends on current capability and availability of
     * the resources in HW.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    MATCH_NUM_FEW_ADVERTISEMENT(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT),

    /**
     * Match as many advertisement per filter as HW could allow, depends on current capability and
     * availability of the resources in HW.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    MATCH_NUM_MAX_ADVERTISEMENT(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
}
