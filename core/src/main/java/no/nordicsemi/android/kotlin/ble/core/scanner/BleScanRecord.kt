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

import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

/**
 * Represents a scan record from Bluetooth LE scan.
 *
 * @property advertiseFlag Returns the advertising flags indicating the discoverable mode and capability of the device.
 * @property serviceUuids Returns a list of service UUIDs within the advertisement that are used to identify the bluetooth GATT services.
 * @property serviceData Returns a map of service UUID and its corresponding service data.
 * @property serviceSolicitationUuids Returns a list of service solicitation UUIDs within the advertisement that are used to identify the Bluetooth GATT services.
 * @property deviceName Returns the local name of the BLE device.
 * @property txPowerLevel Returns the transmission power level of the packet in dBm.
 * @property bytes Returns raw bytes of scan record.
 * @property manufacturerSpecificData Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific data.
 * @see [ScanRecord](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanRecord)
 */
@Parcelize
data class BleScanRecord(
    val advertiseFlag: Int,
    val serviceUuids: List<ParcelUuid>?,
    val serviceData: Map<ParcelUuid, DataByteArray>,
    val serviceSolicitationUuids: List<ParcelUuid>,
    val deviceName: String?,
    val txPowerLevel: Int?,
    val bytes: DataByteArray? = null,
    val manufacturerSpecificData: SparseArray<DataByteArray>,
) : Parcelable
