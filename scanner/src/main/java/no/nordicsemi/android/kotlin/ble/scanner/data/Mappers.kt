package no.nordicsemi.android.kotlin.ble.scanner.data

import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleExtendedScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleLegacyScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanDataStatus
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanPrimaryPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

fun ScanRecord.toDomain(): BleScanRecord {
    return BleScanRecord(
        this.advertiseFlags,
        this.serviceUuids,
        this.serviceData,
        getSolicitationUuids(this),
        this.deviceName ?: "",
        this.txPowerLevel,
        this.bytes,
        this.manufacturerSpecificData
    )
}

private fun getSolicitationUuids(scanRecord: ScanRecord): List<ParcelUuid> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        scanRecord.serviceSolicitationUuids
    } else {
        emptyList()
    }
}

fun ScanResult.toDomain(): BleScanResultData {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        BleExtendedScanResult(
            getAdvertisingSid(this),
            BleScanPrimaryPhy.create(this.primaryPhy),
            getSecondaryPhy(this),
            getTxPower(this),
            this.rssi,
            getPeriodicAdvertisingInterval(this),
            this.timestampNanos,
            this.isLegacy,
            this.isConnectable,
            BleScanDataStatus.create(this.dataStatus),
            this.scanRecord?.toDomain()
        )
    } else {
        BleLegacyScanResult(
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
