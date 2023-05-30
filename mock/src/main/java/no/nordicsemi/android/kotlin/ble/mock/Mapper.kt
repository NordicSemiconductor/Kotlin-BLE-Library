package no.nordicsemi.android.kotlin.ble.mock

import android.os.Build
import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.scanner.BleExtendedScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleLegacyScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

fun BleAdvertiseConfig.toScanResult(): BleScanResultData {
    return if (this.settings.legacyMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.toExtendedResult()
    } else {
        this.toLegacyResult()
    }
}

private fun BleAdvertiseConfig.toExtendedResult(): BleExtendedScanResult {

}

private fun BleAdvertiseConfig.toLegacyResult(): BleLegacyScanResult {
    return BleLegacyScanResult(
        rssi = 0,
        timestampNanos = System.currentTimeMillis(),

        )
}

private fun BleAdvertiseConfig.toScanResult(): BleScanRecord {

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
    }
    return BleScanRecord(
        advertiseFlags = TODO(),
        serviceUuids = serviceUuids,
        serviceData = serviceData,
        serviceSolicitationUuids = serviceSolicitationUuids,
        deviceName = deviceName,
        txPowerLevel = txPowerLevel?.toNative() ?: 0,
        bytes =,
        manufacturerSpecificData =
    )
}
