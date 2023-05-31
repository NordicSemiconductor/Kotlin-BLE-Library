package no.nordicsemi.android.kotlin.ble.mock

import android.os.Build
import android.util.SparseArray
import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.scanner.BleExtendedScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleLegacyScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

fun BleAdvertiseConfig.toScanResult(): BleScanResultData {
    return if (!isLegacy()) {
        this.toExtendedResult()
    } else {
        this.toLegacyResult()
    }
}

private fun BleAdvertiseConfig.isLegacy(): Boolean {
    return this.settings.legacyMode || Build.VERSION.SDK_INT < Build.VERSION_CODES.O
}

private fun BleAdvertiseConfig.toExtendedResult(): BleExtendedScanResult {

}

private fun BleAdvertiseConfig.toLegacyResult(): BleLegacyScanResult {
    return BleLegacyScanResult(
        rssi = 0,
        timestampNanos = System.currentTimeMillis(),
        scanRecord = toScanRecord()
    )
}

private fun BleAdvertiseConfig.toScanRecord(): BleScanRecord {
    val serviceUuids = listOfNotNull(
        advertiseData?.serviceUuid,
        scanResponseData?.serviceUuid
    )
    val serviceData = (advertiseData?.serviceData?.map { it.uuid to it.data }?.toMap()
        ?: mapOf()) + (scanResponseData?.serviceData?.map { it.uuid to it.data }?.toMap()
        ?: mapOf())
    val serviceSolicitationUuids = listOfNotNull(
        advertiseData?.serviceSolicitationUuid,
        scanResponseData?.serviceSolicitationUuid
    )
    val deviceName = settings.deviceName?.takeIf {
        advertiseData?.includeDeviceName == true || scanResponseData?.includeDeviceName == true
    }
    val txPowerLevel = settings.txPowerLevel?.takeIf {
        settings.includeTxPower == true || advertiseData?.includeTxPowerLever == true || scanResponseData?.includeTxPowerLever == true
    }?.let {
        if (isLegacy()) {
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
        advertiseFlag = 0,
        serviceUuids = serviceUuids,
        serviceData = serviceData,
        serviceSolicitationUuids = serviceSolicitationUuids,
        deviceName = deviceName,
        txPowerLevel = txPowerLevel,
        bytes = ,
        manufacturerSpecificData = manufacturerSpecificData
    )
}
