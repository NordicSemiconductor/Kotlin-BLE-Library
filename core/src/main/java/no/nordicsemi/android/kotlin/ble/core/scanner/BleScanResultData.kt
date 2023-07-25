package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy

/**
 * ScanResult for Bluetooth LE scan.
 *
 * @property rssi Returns the received signal strength in dBm.
 * @property timestampNanos Returns timestamp since boot when the scan record was observed.
 * @property scanRecord Returns the scan record ([BleScanRecord]), which is a combination of advertisement and scan response.
 * @property advertisingSid Returns the advertising set id.
 * @property primaryPhy Returns the primary Physical Layer ([BleGattPrimaryPhy]) on which this advertisment was received.
 * @property secondaryPhy Returns the secondary Physical Layer ([BleGattPhy]) on which this advertisment was received.
 * @property txPower Returns the transmit power in dBm.
 * @property periodicAdvertisingInterval Returns the periodic advertising interval in units of 1.
 * @property isLegacy Returns true if this object represents legacy scan result.
 * @property isConnectable Returns true if this object represents connectable scan result.
 * @property dataStatus Returns the data status ([BleScanDataStatus]) .
 * @see [ScanResult](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult)
 */
@Parcelize
data class BleScanResultData(
    val rssi: Int,
    val timestampNanos: Long,
    val scanRecord: BleScanRecord?,

    @RequiresApi(Build.VERSION_CODES.O)
    val advertisingSid: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val primaryPhy: BleGattPrimaryPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val secondaryPhy: BleGattPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val txPower: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val periodicAdvertisingInterval: Int? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isLegacy: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val isConnectable: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val dataStatus: BleScanDataStatus? = null,
) : Parcelable
