package no.nordicsemi.android.kotlin.ble.mock

import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.SparseArray
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.scanner.BleExtendedScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleGattPrimaryPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleLegacyScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanDataStatus
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

fun BleAdvertiseConfig.toScanResult(): BleScanResultData {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.toExtendedResult()
    } else {
        this.toLegacyResult()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun BleAdvertiseConfig.toExtendedResult(): BleExtendedScanResult {
    return BleExtendedScanResult(
        advertisingSid = ScanResult.SID_NOT_PRESENT,
        primaryPhy = settings.primaryPhy ?: if (settings.legacyMode) {
            BleGattPrimaryPhy.PHY_LE_1M
        } else {
            BleGattPrimaryPhy.PHY_LE_CODED
        },
        secondaryPhy = settings.secondaryPhy,
        txPower = settings.txPowerLevel?.value(),
        rssi = 0,
        periodicAdvertisingInterval = null,
        timestampNanos = System.currentTimeMillis(),
        settings.legacyMode,
        isConnectable = settings.connectable,
        dataStatus = BleScanDataStatus.DATA_COMPLETE,
        scanRecord = toScanRecord()
    )
}

private fun BleAdvertiseConfig.toLegacyResult(): BleLegacyScanResult {
    return BleLegacyScanResult(
        rssi = 0,
        timestampNanos = System.currentTimeMillis(),
        scanRecord = toScanRecord()
    )
}

private fun BleAdvertiseConfig.toScanRecord(): BleScanRecord {
    val flags: Int = 0b00000110
    val serviceUuids = listOfNotNull(
        advertiseData?.serviceUuid,
        scanResponseData?.serviceUuid
    )
    val serviceData = (advertiseData?.serviceData?.associate { it.uuid to it.data }
        ?: mapOf()) + (scanResponseData?.serviceData?.associate { it.uuid to it.data }
        ?: mapOf())
    val serviceSolicitationUuids = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        listOfNotNull(
            advertiseData?.serviceSolicitationUuid,
            scanResponseData?.serviceSolicitationUuid
        )
    } else {
        emptyList()
    }
    val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        settings.deviceName?.takeIf {
            advertiseData?.includeDeviceName == true || scanResponseData?.includeDeviceName == true
        }
    } else {
        null
    }

    val txPowerLevel = settings.txPowerLevel
        ?.takeIf {
            settings.includeTxPower == true || advertiseData?.includeTxPowerLever == true || scanResponseData?.includeTxPowerLever == true
        }?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                it.toLegacy()
            } else {
                it.toNative()
            }
        } ?: 0
    val manufacturerSpecificData = SparseArray<ByteArray>().also { array ->
        advertiseData?.manufacturerData?.forEach {
            array.put(it.id, it.data)
        }
        scanResponseData?.manufacturerData?.forEach {
            array.put(it.id, it.data)
        }
    }
    return BleScanRecord(
        advertiseFlag = flags,
        serviceUuids = serviceUuids,
        serviceData = serviceData,
        serviceSolicitationUuids = serviceSolicitationUuids,
        deviceName = deviceName,
        txPowerLevel = txPowerLevel,
        manufacturerSpecificData = manufacturerSpecificData
    )
}
