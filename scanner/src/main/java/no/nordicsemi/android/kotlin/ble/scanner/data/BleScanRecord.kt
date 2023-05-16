package no.nordicsemi.android.kotlin.ble.scanner.data

import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import kotlinx.parcelize.Parcelize

@Parcelize
data class BleScanRecord(
    val advertiseFlags: Int,
    val serviceUuids: List<ParcelUuid>?,
    val serviceData: Map<ParcelUuid, ByteArray>,
    val serviceSolicitationUuids: List<ParcelUuid>,
    val deviceName: String?,
    val txPowerLevel: Int,
    val bytes: ByteArray,
    val manufacturerSpecificData: SparseArray<ByteArray>
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleScanRecord

        if (advertiseFlags != other.advertiseFlags) return false
        if (serviceUuids != other.serviceUuids) return false
        if (serviceData != other.serviceData) return false
        if (serviceSolicitationUuids != other.serviceSolicitationUuids) return false
        if (deviceName != other.deviceName) return false
        if (txPowerLevel != other.txPowerLevel) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (manufacturerSpecificData != other.manufacturerSpecificData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = advertiseFlags
        result = 31 * result + (serviceUuids?.hashCode() ?: 0)
        result = 31 * result + serviceData.hashCode()
        result = 31 * result + serviceSolicitationUuids.hashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        result = 31 * result + txPowerLevel
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + manufacturerSpecificData.hashCode()
        return result
    }
}
