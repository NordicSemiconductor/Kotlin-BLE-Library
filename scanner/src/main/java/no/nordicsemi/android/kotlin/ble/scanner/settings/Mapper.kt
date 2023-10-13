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

package no.nordicsemi.android.kotlin.ble.scanner.settings

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings

/**
 * Maps library [BleScannerSettings] to Android native API.
 *
 * @return Android native scan settings.
 */
internal fun BleScannerSettings.toNative(): ScanSettings {
    return ScanSettings.Builder().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCallbackType(callbackType.value)
            matchMode?.let { setMatchMode(it.value) }
            numOfMatches?.let { setNumOfMatches(it.value) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setLegacy(legacy)
            phy?.value?.let { setPhy(it) }
        }
        setReportDelay(reportDelay)
        setScanMode(scanMode.value)
    }.build()
}


internal fun List<BleScanFilter>.toNative(): List<ScanFilter> {
    return this.map {
        val builder = ScanFilter.Builder()

        it.deviceName?.let { builder.setDeviceName(it) }
        it.deviceAddress?.let { builder.setDeviceAddress(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.type?.let { builder.setAdvertisingDataType(it.value) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.typeWithData?.let {
                builder.setAdvertisingDataTypeWithData(
                    it.type.value,
                    it.advertisingData.value,
                    it.mask.value
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            it.serviceSolicitationUuid?.let {
                if (it.mask != null) {
                    builder.setServiceSolicitationUuid(it.uuid, it.mask)
                } else {
                    builder.setServiceSolicitationUuid(it.uuid)
                }
            }
        }

        it.manufacturerData?.let {
            if (it.mask != null) {
                builder.setManufacturerData(it.id, it.data.value, it.mask!!.value)
            } else {
                builder.setManufacturerData(it.id, it.data.value)
            }
        }

        it.serviceData?.let {
            if (it.mask != null) {
                builder.setServiceData(it.uuid, it.data.value, it.mask!!.value)
            } else {
                builder.setServiceData(it.uuid, it.data.value)
            }
        }

        it.serviceUuid?.let {
            if (it.mask != null) {
                builder.setServiceUuid(it.uuid, it.mask)
            } else {
                builder.setServiceUuid(it.uuid)
            }
        }

        builder.build()
    }
}
