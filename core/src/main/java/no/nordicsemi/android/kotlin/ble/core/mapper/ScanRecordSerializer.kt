package no.nordicsemi.android.kotlin.ble.core.mapper

import android.annotation.SuppressLint
import android.bluetooth.le.TransportDiscoveryData
import android.os.ParcelUuid
import android.util.SparseArray
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_FLAGS
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_LOCAL_NAME_COMPLETE
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_LOCAL_NAME_SHORT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_MANUFACTURER_SPECIFIC_DATA
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_DATA_128_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_DATA_16_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_DATA_32_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL
import no.nordicsemi.android.kotlin.ble.core.mapper.BleType.DATA_TYPE_TX_POWER_LEVEL
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord
import java.nio.ByteBuffer
import java.util.UUID

/**
 * Parser serializing scan record to and from bytes.
 *
 * @see [ScanRecord](https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/framework/java/android/bluetooth/le/ScanRecord.java)
 */
@Suppress("unused")
@SuppressLint("AndroidFrameworkBluetoothPermission")
object ScanRecordSerializer {

    /**
     * Parse scan record bytes to [BleScanRecord].
     *
     *
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     *
     *
     * All numerical multi-byte entities and values shall use little-endian **byte**
     * order.
     *
     * @hide
     */
    fun parseToBytes(record: BleScanRecord): ByteArray {
        return parseToBytes(
            record.advertiseFlag,
            record.serviceUuids,
            record.serviceData,
            record.serviceSolicitationUuids,
            record.deviceName,
            record.txPowerLevel,
            record.manufacturerSpecificData
        )
    }

    /**
     * Parse [BleScanRecord] fields to byte.
     *
     *
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     *
     *
     * All numerical multi-byte entities and values shall use little-endian **byte**
     * order.
     *
     * @hide
     */
    fun parseToBytes(
        advertiseFlag: Int,
        serviceUuids: List<ParcelUuid>?,
        serviceData: Map<ParcelUuid, DataByteArray>,
        serviceSolicitationUuids: List<ParcelUuid>,
        deviceName: String?,
        txPowerLevel: Int?,
        manufacturerSpecificData: SparseArray<DataByteArray>,
    ): ByteArray {
        var result = byteArrayOf()

        result += byteArrayOf(0x02, DATA_TYPE_FLAGS.toByte(), advertiseFlag.toByte())

        serviceUuids?.forEach {
            val data = it.toByteArray()
            result += (data.size+1).toByte()
            result += DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE.toByte()
            result += data
        }

        for (key in serviceData.keys) {
            val data = serviceData[key]!!
            val serializedUuid = key.toByteArray()
            result += (data.value.size+serializedUuid.size+1).toByte()
            result += DATA_TYPE_SERVICE_DATA_128_BIT.toByte()
            result += serializedUuid
            result += data.value
        }

        txPowerLevel?.let {
            result += 2.toByte()
            result += DATA_TYPE_TX_POWER_LEVEL.toByte()
            result += it.toByte()
        }

        if (!deviceName.isNullOrBlank()) {
            val data = deviceName.toByteArray()
            result += (data.size+1).toByte()
            result += DATA_TYPE_LOCAL_NAME_SHORT.toByte()
            result += data
        }

        serviceSolicitationUuids.forEach {
            val data = it.toByteArray()
            result += (data.size+1).toByte()
            result += DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT.toByte()
            result += data
        }

        for (i in 0 until manufacturerSpecificData.size()) {
            val key: Int = manufacturerSpecificData.keyAt(i)
            // get the object by the key.
            val data = manufacturerSpecificData.get(key)
            result += (data.value.size+3).toByte()
            result += DATA_TYPE_MANUFACTURER_SPECIFIC_DATA.toByte()
            result += byteArrayOf((key and 0xFF).toByte(), ((key shr 8) and 0xFF).toByte())
            result += data.value
        }

        return result
    }

    /**
     * Parse scan record bytes to [ScanRecord].
     *
     *
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     *
     *
     * All numerical multi-byte entities and values shall use little-endian **byte**
     * order.
     *
     * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
     * @hide
     */
    fun parseFromBytes(scanRecord: ByteArray): BleScanRecord? {
        var currentPos = 0
        var advertiseFlag = -1
        val serviceUuids: MutableList<ParcelUuid> = mutableListOf()
        val serviceSolicitationUuids: MutableList<ParcelUuid> = mutableListOf()
        var localName: String? = null
        var txPowerLevel: Int? = null
        val manufacturerData = SparseArray<DataByteArray>()
        val serviceData: MutableMap<ParcelUuid, DataByteArray> = mutableMapOf()
        val advertisingDataMap = HashMap<Int, DataByteArray>()
//        var transportDiscoveryData: TransportDiscoveryData? = null
        return try {
            while (currentPos < scanRecord.size) {
                // length is unsigned int.
                val length = scanRecord[currentPos++].toInt() and 0xFF
                if (length == 0) {
                    break
                }
                // Note the length includes the length of the field type itself.
                val dataLength = length - 1
                // fieldType is unsigned int.
                val fieldType = scanRecord[currentPos++].toInt() and 0xFF
                val advertisingData = extractBytes(scanRecord, currentPos, dataLength)
                advertisingDataMap[fieldType] = DataByteArray(advertisingData)
                when (fieldType) {
                    DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toInt() and 0xFF
                    DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL,
                    DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE,
                    -> parseServiceUuid(
                        scanRecord, currentPos,
                        dataLength,
                        BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids
                    )

                    DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL,
                    DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE,
                    -> parseServiceUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids
                    )

                    DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL,
                    DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE,
                    -> parseServiceUuid(
                        scanRecord,
                        currentPos,
                        dataLength,
                        16,
                        serviceUuids
                    )

                    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT -> parseServiceSolicitationUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_16_BIT, serviceSolicitationUuids
                    )

                    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT -> parseServiceSolicitationUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_32_BIT, serviceSolicitationUuids
                    )

                    DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT -> parseServiceSolicitationUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_128_BIT, serviceSolicitationUuids
                    )

                    DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName =
                        String(
                            extractBytes(scanRecord, currentPos, dataLength)
                        )

                    DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                    DATA_TYPE_SERVICE_DATA_16_BIT,
                    DATA_TYPE_SERVICE_DATA_32_BIT,
                    DATA_TYPE_SERVICE_DATA_128_BIT,
                    -> {
                        var serviceUuidLength: Int =
                            BluetoothUuid.UUID_BYTES_16_BIT
                        if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                            serviceUuidLength =
                                BluetoothUuid.UUID_BYTES_32_BIT
                        } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                            serviceUuidLength =
                                BluetoothUuid.UUID_BYTES_128_BIT
                        }
                        val serviceDataUuidBytes = extractBytes(
                            scanRecord, currentPos,
                            serviceUuidLength
                        )
                        val serviceDataUuid: ParcelUuid =
                            BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes
                            )
                        val serviceDataArray = extractBytes(
                            scanRecord,
                            currentPos + serviceUuidLength, dataLength - serviceUuidLength
                        )
                        serviceData[serviceDataUuid] = DataByteArray(serviceDataArray)
                    }

                    DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        val manufacturerId =
                            ((scanRecord[currentPos + 1].toInt() and 0xFF shl 8)
                                    + (scanRecord[currentPos].toInt() and 0xFF))
                        val manufacturerDataBytes = extractBytes(
                            scanRecord, currentPos + 2,
                            dataLength - 2
                        )
                        manufacturerData.put(manufacturerId, DataByteArray(manufacturerDataBytes))
                    }

//                        DATA_TYPE_TRANSPORT_DISCOVERY_DATA -> {
//                            // -1 / +1 to include the type in the extract
//                            val transportDiscoveryDataBytes =
//                                extractBytes(scanRecord, currentPos - 1, dataLength + 1)
//                            transportDiscoveryData = TransportDiscoveryData(transportDiscoveryDataBytes)
//                        }

                    else -> {}
                }
                currentPos += dataLength
            }

            BleScanRecord(
                advertiseFlag = advertiseFlag,
                serviceUuids = serviceUuids.takeIf { it.isNotEmpty() },
                serviceData = serviceData,
                serviceSolicitationUuids = serviceSolicitationUuids,
                deviceName = localName,
                txPowerLevel = txPowerLevel,
                bytes = DataByteArray(scanRecord),
                manufacturerSpecificData = manufacturerData
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Parse service UUIDs.
     */
    private fun parseServiceUuid(
        scanRecord: ByteArray,
        currentPos: Int,
        dataLength: Int,
        uuidLength: Int,
        serviceUuids: MutableList<ParcelUuid>,
    ): Int {
        var currentPos = currentPos
        var dataLength = dataLength
        while (dataLength > 0) {
            val uuidBytes = extractBytes(scanRecord, currentPos, uuidLength)
            serviceUuids.add(
                BluetoothUuid.parseUuidFrom(
                    uuidBytes
                )
            )
            dataLength -= uuidLength
            currentPos += uuidLength
        }
        return currentPos
    }

    /**
     * Parse service Solicitation UUIDs.
     */
    private fun parseServiceSolicitationUuid(
        scanRecord: ByteArray,
        currentPos: Int,
        dataLength: Int,
        uuidLength: Int,
        serviceSolicitationUuids: MutableList<ParcelUuid>,
    ): Int {
        var currentPos = currentPos
        var dataLength = dataLength
        while (dataLength > 0) {
            val uuidBytes = extractBytes(scanRecord, currentPos, uuidLength)
            serviceSolicitationUuids.add(
                BluetoothUuid.parseUuidFrom(
                    uuidBytes
                )
            )
            dataLength -= uuidLength
            currentPos += uuidLength
        }
        return currentPos
    }

    // Helper method to extract bytes from byte array.
    private fun extractBytes(scanRecord: ByteArray, start: Int, length: Int): ByteArray {
        val bytes = ByteArray(length)
        System.arraycopy(scanRecord, start, bytes, 0, length)
        return bytes
    }
}

private fun ParcelUuid.toByteArray(): ByteArray = this.uuid.asBytes().reversed().toByteArray()

private fun UUID.asBytes(): ByteArray {
    val b = ByteBuffer.wrap(ByteArray(16))
    b.putLong(mostSignificantBits)
    b.putLong(leastSignificantBits)
    return b.array()
}
