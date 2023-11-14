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

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Data class that offers configuration parameters for a scanner.
 *
 * @property scanMode Set scan mode ([BleScanMode]) for Bluetooth LE scan.
 * @property reportDelay Set report delay timestamp for Bluetooth LE scan.
 * @property includeStoredBondedDevices Flag indicating weather to include stored bonded devices in scan result.
 * @property callbackType Set callback type ([BleScannerCallbackType]) for Bluetooth LE scan.
 * @property numOfMatches Set the number of matches ([BleNumOfMatches]) for Bluetooth LE scan filters hardware match.
 * @property matchMode Set match mode ([BleScannerMatchMode]) for Bluetooth LE scan filters hardware match.
 * @property legacy Set whether only legacy advertisements should be returned in scan results.
 * @property phy Set the Physical Layer ([BleScannerPhy]) to use during this scan.
 */
data class BleScannerSettings(

    val scanMode: BleScanMode = BleScanMode.SCAN_MODE_LOW_POWER,

    val reportDelay: Long = 0L,

    val includeStoredBondedDevices: Boolean = true,

    @RequiresApi(Build.VERSION_CODES.M)
    val callbackType: BleScannerCallbackType = BleScannerCallbackType.CALLBACK_TYPE_ALL_MATCHES,

    @RequiresApi(Build.VERSION_CODES.M)
    val numOfMatches: BleNumOfMatches? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        BleNumOfMatches.MATCH_NUM_MAX_ADVERTISEMENT
    } else {
        null
    },

    @RequiresApi(Build.VERSION_CODES.M)
    val matchMode: BleScannerMatchMode? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        BleScannerMatchMode.MATCH_MODE_AGGRESSIVE
    } else {
        null
    },

    @RequiresApi(Build.VERSION_CODES.O)
    val legacy: Boolean = false,

    @RequiresApi(Build.VERSION_CODES.O)
    val phy: BleScannerPhy? = null,
)
