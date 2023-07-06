package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.mapper.ScanRecordSerializer

@Parcelize
data class BleScanRecord(
    val advertiseFlag: Int,
    val serviceUuids: List<ParcelUuid>?,
    val serviceData: Map<ParcelUuid, ByteArray>,
    val serviceSolicitationUuids: List<ParcelUuid>,
    val deviceName: String?,
    val txPowerLevel: Int?,
    val bytes: ByteArray? = null,
    val manufacturerSpecificData: SparseArray<ByteArray>,
) : Parcelable {

    constructor(
        advertiseFlag: Int,
        serviceUuids: List<ParcelUuid>?,
        serviceData: Map<ParcelUuid, ByteArray>,
        serviceSolicitationUuids: List<ParcelUuid>,
        deviceName: String?,
        txPowerLevel: Int,
        manufacturerSpecificData: SparseArray<ByteArray>,
    ) : this(
        advertiseFlag,
        serviceUuids,
        serviceData,
        serviceSolicitationUuids,
        deviceName,
        txPowerLevel,
        ScanRecordSerializer.parseToBytes(
            advertiseFlag,
            serviceUuids,
            serviceData,
            serviceSolicitationUuids,
            deviceName,
            txPowerLevel,
            manufacturerSpecificData
        ),
        manufacturerSpecificData
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleScanRecord

        if (advertiseFlag != other.advertiseFlag) return false
        if (serviceUuids != other.serviceUuids) return false
        if (serviceData != other.serviceData) return false
        if (serviceSolicitationUuids != other.serviceSolicitationUuids) return false
        if (deviceName != other.deviceName) return false
        if (txPowerLevel != other.txPowerLevel) return false
        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false
        if (manufacturerSpecificData != other.manufacturerSpecificData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = advertiseFlag
        result = 31 * result + (serviceUuids?.hashCode() ?: 0)
        result = 31 * result + serviceData.hashCode()
        result = 31 * result + serviceSolicitationUuids.hashCode()
        result = 31 * result + (deviceName?.hashCode() ?: 0)
        result = 31 * result + (txPowerLevel ?: 0)
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        result = 31 * result + manufacturerSpecificData.hashCode()
        return result
    }
}
