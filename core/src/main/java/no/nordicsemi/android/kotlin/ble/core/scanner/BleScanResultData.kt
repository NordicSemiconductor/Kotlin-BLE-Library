package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

sealed interface BleScanResultData : Parcelable {
    val scanRecord: BleScanRecord?
    val rssi: Int
    val timestampNanos: Long
}

@RequiresApi(Build.VERSION_CODES.O)
@Parcelize
data class BleExtendedScanResult(
    val advertisingSid: Int?,
    val primaryPhy: BleScanPrimaryPhy,
    val secondaryPhy: BleGattPhy?,
    val txPower: Int?,
    override val rssi: Int,
    val periodicAdvertisingInterval: Int?,
    override val timestampNanos: Long,
    val isLegacy: Boolean,
    val isConnectable: Boolean,
    val dataStatus: BleScanDataStatus,
    override val scanRecord: BleScanRecord?
) : BleScanResultData

@Parcelize
data class BleLegacyScanResult(
    override val rssi: Int,
    override val timestampNanos: Long,
    override val scanRecord: BleScanRecord?
) : BleScanResultData
