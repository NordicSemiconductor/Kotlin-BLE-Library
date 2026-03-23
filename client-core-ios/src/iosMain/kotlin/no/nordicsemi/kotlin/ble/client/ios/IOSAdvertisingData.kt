package no.nordicsemi.kotlin.ble.client.ios

import no.nordicsemi.kotlin.ble.client.AdvertisingData
import no.nordicsemi.kotlin.ble.core.ios.toByteArray
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataSolicitedServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataTxPowerLevelKey
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// TODO: This class is not tested and may have issues with certain edge cases, such as malformed manufacturer data or service data. Consider adding unit tests to verify its correctness.
@OptIn(ExperimentalUuidApi::class)
class IOSAdvertisingData(
    private val advertisementData: Map<Any?, *>
) : AdvertisingData {

    override val name: String?
        get() = advertisementData[CBAdvertisementDataLocalNameKey] as? String

    override val serviceUuids: List<Uuid>
        get() {
            @Suppress("UNCHECKED_CAST")
            val uuids = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? List<CBUUID>
                ?: return emptyList()
            return uuids.map { it.toKotlinUuid() }
        }

    override val serviceSolicitationUuids: List<Uuid>
        get() {
            @Suppress("UNCHECKED_CAST")
            val uuids = advertisementData[CBAdvertisementDataSolicitedServiceUUIDsKey] as? List<CBUUID>
                ?: return emptyList()
            return uuids.map { it.toKotlinUuid() }
        }

    override val serviceData: Map<Uuid, ByteArray>
        get() {
            @Suppress("UNCHECKED_CAST")
            val data = advertisementData[CBAdvertisementDataServiceDataKey] as? Map<CBUUID, NSData>
                ?: return emptyMap()
            return data.map { (uuid, nsData) ->
                uuid.toKotlinUuid() to nsData.toByteArray()
            }.toMap()
        }

    override val txPowerLevel: Int?
        get() = (advertisementData[CBAdvertisementDataTxPowerLevelKey] as? NSNumber)?.intValue

    override val manufacturerData: Map<Int, ByteArray>
        get() {
            val data = advertisementData[CBAdvertisementDataManufacturerDataKey] as? NSData
                ?: return emptyMap()
            val bytes = data.toByteArray()
            if (bytes.size < 2) return emptyMap()
            val companyId = (bytes[0].toInt() and 0xFF) or ((bytes[1].toInt() and 0xFF) shl 8)
            return mapOf(companyId to bytes.copyOfRange(2, bytes.size))
        }
}
