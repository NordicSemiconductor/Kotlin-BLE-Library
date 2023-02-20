package no.nordicsemi.android.kotlin.ble.scanner.errors

import android.bluetooth.le.ScanCallback
import android.os.Build
import androidx.annotation.RequiresApi

enum class ScanFailedError(internal val value: Int) {
    UNKNOWN(0),
    SCAN_FAILED_ALREADY_STARTED(ScanCallback.SCAN_FAILED_ALREADY_STARTED),
    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED(ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED),
    SCAN_FAILED_FEATURE_UNSUPPORTED(ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED),
    SCAN_FAILED_INTERNAL_ERROR(ScanCallback.SCAN_FAILED_INTERNAL_ERROR),
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES(ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES),
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    SCAN_FAILED_SCANNING_TOO_FREQUENTLY(ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY);

    companion object {
        fun create(value: Int): ScanFailedError {
            return values().firstOrNull { it.value == value } ?: UNKNOWN
        }
    }
}
