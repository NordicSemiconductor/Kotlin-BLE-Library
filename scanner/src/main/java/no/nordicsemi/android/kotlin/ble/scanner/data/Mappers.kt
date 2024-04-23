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

package no.nordicsemi.android.kotlin.ble.scanner.data

import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.ParcelUuid
import android.util.SparseArray
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.map
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanDataStatus
import no.nordicsemi.android.kotlin.ble.core.scanner.BleGattPrimaryPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

internal fun ScanRecord.toDomain(): BleScanRecord {
    return BleScanRecord(
        this.advertiseFlags,
        this.serviceUuids ?: emptyList(),
        this.serviceData?.mapValues { DataByteArray(it.value ?: byteArrayOf()) } ?: emptyMap(),
        getSolicitationUuids(this),
        this.deviceName ?: "",
        this.txPowerLevel,
        DataByteArray(this.bytes ?: byteArrayOf()),
        this.manufacturerSpecificData?.map { DataByteArray(it ?: byteArrayOf()) } ?: SparseArray()
    )
}

private fun getSolicitationUuids(scanRecord: ScanRecord): List<ParcelUuid> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        scanRecord.serviceSolicitationUuids ?: emptyList() //Don't delete elvis operator.
    } else {
        emptyList()
    }
}

internal fun ScanResult.toDomain(): BleScanResultData {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        BleScanResultData(
            this.rssi,
            this.timestampNanos,
            this.scanRecord?.toDomain(),
            getAdvertisingSid(this),
            BleGattPrimaryPhy.createOrNull(this.primaryPhy),
            getSecondaryPhy(this),
            getTxPower(this),
            getPeriodicAdvertisingInterval(this),
            this.isLegacy,
            this.isConnectable,
            BleScanDataStatus.create(this.dataStatus),
        )
    } else {
        BleScanResultData(
            this.rssi,
            this.timestampNanos,
            this.scanRecord?.toDomain()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getAdvertisingSid(scanResult: ScanResult): Int? {
    return if (scanResult.advertisingSid != ScanResult.SID_NOT_PRESENT) {
        scanResult.advertisingSid
    } else {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getPeriodicAdvertisingInterval(scanResult: ScanResult): Int? {
    return if (scanResult.periodicAdvertisingInterval != ScanResult.PERIODIC_INTERVAL_NOT_PRESENT) {
        scanResult.periodicAdvertisingInterval
    } else {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getSecondaryPhy(scanResult: ScanResult): BleGattPhy? {
    return if (scanResult.secondaryPhy != ScanResult.PHY_UNUSED) {
        BleGattPhy.create(scanResult.secondaryPhy)
    } else {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getTxPower(scanResult: ScanResult): Int? {
    return if (scanResult.txPower != ScanResult.TX_POWER_NOT_PRESENT) {
        scanResult.txPower
    } else {
        null
    }
}
