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
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

/**
 * ScanResult for Bluetooth LE scan.
 *
 * @property rssi Returns the received signal strength in dBm.
 * @property timestampNanos Returns timestamp since boot when the scan record was observed.
 * @property scanRecord Returns the scan record ([BleScanRecord]), which is a combination of advertisement and scan response.
 * @property advertisingSid Returns the advertising set id.
 * @property primaryPhy Returns the primary Physical Layer ([BleGattPrimaryPhy]) on which this advertisment was received.
 * @property secondaryPhy Returns the secondary Physical Layer ([BleGattPhy]) on which this advertisment was received.
 * @property txPower Returns the transmit power in dBm.
 * @property periodicAdvertisingInterval Returns the periodic advertising interval in units of 1.
 * @property isLegacy Returns true if this object represents legacy scan result.
 * @property isConnectable Returns true if this object represents connectable scan result.
 * @property dataStatus Returns the data status ([BleScanDataStatus]) .
 * @see [ScanResult](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult)
 */
@Parcelize
data class BleScanResultData(
    val rssi: Int,
    val timestampNanos: Long,
    val scanRecord: BleScanRecord?,

    @RequiresApi(Build.VERSION_CODES.O)
    val advertisingSid: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val primaryPhy: BleGattPrimaryPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val secondaryPhy: BleGattPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val txPower: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val periodicAdvertisingInterval: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isLegacy: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isConnectable: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val dataStatus: BleScanDataStatus? = null,
) : Parcelable
