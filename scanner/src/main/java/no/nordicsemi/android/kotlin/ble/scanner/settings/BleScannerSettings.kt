/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.scanner.settings

import android.os.Build
import androidx.annotation.RequiresApi

data class BleScannerSettings(
    val scanMode: BleScanMode = BleScanMode.SCAN_MODE_LOW_POWER,
    @RequiresApi(Build.VERSION_CODES.M)
    val callbackType: BleScannerCallbackType = BleScannerCallbackType.CALLBACK_TYPE_ALL_MATCHES,
    val reportDelay: Long = 0L,
    @RequiresApi(Build.VERSION_CODES.M)
    val numOfMatches: BleNumOfMatches = BleNumOfMatches.MATCH_NUM_MAX_ADVERTISEMENT,
    @RequiresApi(Build.VERSION_CODES.M)
    val matchMode: BleScannerMatchMode = BleScannerMatchMode.MATCH_MODE_AGGRESSIVE,
    @RequiresApi(Build.VERSION_CODES.O)
    val legacy: Boolean,
    @RequiresApi(Build.VERSION_CODES.O)
    val phy: BleScannerPhy = BleScannerPhy.PHY_LE_ALL_SUPPORTED,
    val useHardwareFilteringIfSupported: Boolean = true,
    val useHardwareBatchingIfSupported: Boolean = true,
    val useHardwareCallbackTypesIfSupported: Boolean = true,
    val matchOptions: MatchOptions = MatchOptions()
)
