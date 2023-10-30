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

package no.nordicsemi.android.kotlin.ble.core.advertiser

import android.bluetooth.le.AdvertiseData
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi

/**
 * Advertise data packet container for Bluetooth LE advertising. This represents the data to be
 * advertised as well as the scan response data for active scans.
 *
 * @property serviceUuid A service UUID to advertise data.
 * @property includeDeviceName Whether the device name should be included in advertise packet.
 * @property includeTxPowerLever Whether the transmission power level should be included in the
 * advertise packet.
 * @property manufacturerData Manufacturer specific data ([ManufacturerData]).
 * @property serviceData Service data ([ServiceData]) to advertise data.
 * @property serviceSolicitationUuid Service solicitation UUID to advertise data.
 *
 * @see [AdvertiseData](https://developer.android.com/reference/android/bluetooth/le/AdvertiseData)
 */
data class BleAdvertisingData(
    val serviceUuid: ParcelUuid? = null,
    val includeDeviceName: Boolean? = null,
    val includeTxPowerLever: Boolean? = null,
    val manufacturerData: List<ManufacturerData> = emptyList(),
    val serviceData: List<ServiceData> = emptyList(),

    @RequiresApi(Build.VERSION_CODES.M)
    val serviceSolicitationUuid: ParcelUuid? = null,
)
