package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import kotlinx.parcelize.Parcelize

@Parcelize
data class BleScanRecord(
    val advertiseFlag: Int,
    val serviceUuids: List<ParcelUuid>?,
    val serviceData: Map<ParcelUuid, ByteArray>,
    val serviceSolicitationUuids: List<ParcelUuid>,
    val deviceName: String?,
    val txPowerLevel: Int,
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
        byteArrayOf(),
        manufacturerSpecificData
    )

    companion object {
        fun rawData(): ByteArray {
            return byteArrayOf()
        }
    }
}
