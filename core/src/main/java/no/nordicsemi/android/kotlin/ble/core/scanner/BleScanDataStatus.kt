package no.nordicsemi.android.kotlin.ble.core.scanner

import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
enum class BleScanDataStatus(val value: Int) {
    DATA_COMPLETE(ScanResult.DATA_COMPLETE),
    DATA_TRUNCATED(ScanResult.DATA_TRUNCATED);

    companion object {
        fun create(value: Int): BleScanDataStatus {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create BleScanDataStatus for value: $value")
        }
    }
}
