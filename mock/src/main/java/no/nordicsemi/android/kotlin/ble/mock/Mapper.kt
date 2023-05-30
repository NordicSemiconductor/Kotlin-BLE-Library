package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.scanner.BleLegacyScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

fun BleAdvertiseConfig.toScanResult(): BleScanResultData {
    return BleLegacyScanResult(
        0,
        0L,
        scanRecord = null,
    )
}