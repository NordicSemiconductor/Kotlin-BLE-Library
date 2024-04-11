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

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.os.Build

/**
 * Merges two [ScanFilter]s into one, with the receiver having higher priority.
 *
 * That means, that if the receiver's filter has a property set, it will be used, otherwise
 * the property from the [other] filter will be used.
 */
internal fun ScanFilter.merged(other: ScanFilter): ScanFilter {
    val builder = ScanFilter.Builder()
    builder.setDeviceName(deviceName ?: other.deviceName)
    builder.setDeviceAddress(deviceAddress ?: other.deviceAddress)
    if (serviceUuid != null) {
        builder.setServiceUuid(serviceUuid, serviceUuidMask)
    } else if (other.serviceUuid != null) {
        builder.setServiceUuid(other.serviceUuid, other.serviceUuidMask)
    }
    if (serviceDataUuid != null) {
        builder.setServiceData(serviceDataUuid, serviceData, serviceDataMask)
    } else if (other.serviceDataUuid != null) {
        builder.setServiceData(other.serviceDataUuid, other.serviceData, other.serviceDataMask)
    }
    if (manufacturerId != -1) {
        builder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask)
    } else if (other.manufacturerId != -1) {
        builder.setManufacturerData(
            other.manufacturerId,
            other.manufacturerData,
            other.manufacturerDataMask
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (serviceSolicitationUuid != null) {
            builder.setServiceSolicitationUuid(serviceSolicitationUuid, serviceSolicitationUuidMask)
        } else if (other.serviceSolicitationUuid != null) {
            builder.setServiceSolicitationUuid(other.serviceSolicitationUuid, other.serviceSolicitationUuidMask)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (advertisingDataType != ScanRecord.DATA_TYPE_NONE) {
            if (advertisingData == null) {
                builder.setAdvertisingDataType(advertisingDataType)
            } else {
                builder.setAdvertisingDataTypeWithData(advertisingDataType, advertisingData!!, advertisingDataMask!!)
            }
        } else if (other.advertisingDataType != ScanRecord.DATA_TYPE_NONE) {
            if (other.advertisingData == null) {
                builder.setAdvertisingDataType(other.advertisingDataType)
            } else {
                builder.setAdvertisingDataTypeWithData(other.advertisingDataType, other.advertisingData!!, other.advertisingDataMask!!)
            }
        }
    }
    return builder.build()
}