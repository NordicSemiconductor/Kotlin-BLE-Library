package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.common.core.DataByteArray

/**
 * Represents a scan record from Bluetooth LE scan.
 *
 * @property advertiseFlag Returns the advertising flags indicating the discoverable mode and capability of the device.
 * @property serviceUuids Returns a list of service UUIDs within the advertisement that are used to identify the bluetooth GATT services.
 * @property serviceData Returns a map of service UUID and its corresponding service data.
 * @property serviceSolicitationUuids Returns a list of service solicitation UUIDs within the advertisement that are used to identify the Bluetooth GATT services.
 * @property deviceName Returns the local name of the BLE device.
 * @property txPowerLevel Returns the transmission power level of the packet in dBm.
 * @property bytes Returns raw bytes of scan record.
 * @property manufacturerSpecificData Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific data.
 * @see [ScanRecord](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanRecord)
 */
@Parcelize
data class BleScanRecord(
    val advertiseFlag: Int,
    val serviceUuids: List<ParcelUuid>?,
    val serviceData: Map<ParcelUuid, ByteArray>,
    val serviceSolicitationUuids: List<ParcelUuid>,
    val deviceName: String?,
    val txPowerLevel: Int?,
    val bytes: DataByteArray? = null,
    val manufacturerSpecificData: SparseArray<ByteArray>,
) : Parcelable {
}
