package no.nordicsemi.android.kotlin.ble.mock.parsers

import android.annotation.SuppressLint
import android.bluetooth.le.TransportDiscoveryData
import android.os.ParcelUuid
import android.util.ArrayMap
import android.util.SparseArray
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord

/**
 * Represents a scan record from Bluetooth LE scan.
 */
@Suppress("unused")
@SuppressLint("AndroidFrameworkBluetoothPermission")
internal object ScanRecord {
    /**
     * Data type is not set for the filter. Will not filter advertising data type.
     */
    const val DATA_TYPE_NONE = -1

    /**
     * Data type is Flags, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_FLAGS = 0x01

    /**
     * Data type is Incomplete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02

    /**
     * Data type is Complete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03

    /**
     * Data type is Incomplete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04

    /**
     * Data type is Complete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05

    /**
     * Data type is Incomplete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06

    /**
     * Data type is Complete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07

    /**
     * Data type is Shortened Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_LOCAL_NAME_SHORT = 0x08

    /**
     * Data type is Complete Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09

    /**
     * Data type is Tx Power Level, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_TX_POWER_LEVEL = 0x0A

    /**
     * Data type is Class of Device, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_CLASS_OF_DEVICE = 0x0D

    /**
     * Data type is Simple Pairing Hash C, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SIMPLE_PAIRING_HASH_C = 0x0E

    /**
     * Data type is Simple Pairing Randomizer R, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SIMPLE_PAIRING_RANDOMIZER_R = 0x0F

    /**
     * Data type is Device ID, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_DEVICE_ID = 0x10

    /**
     * Data type is Security Manager Out of Band Flags, see the Bluetooth Generic Access Profile for
     * more details.
     */
    const val DATA_TYPE_SECURITY_MANAGER_OUT_OF_BAND_FLAGS = 0x11

    /**
     * Data type is Slave Connection Interval Range, see the Bluetooth Generic Access Profile for
     * more details.
     */
    const val DATA_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE = 0x12

    /**
     * Data type is List of 16-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT = 0x14

    /**
     * Data type is List of 128-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT = 0x15

    /**
     * Data type is Service Data - 16-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SERVICE_DATA_16_BIT = 0x16

    /**
     * Data type is Public Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_PUBLIC_TARGET_ADDRESS = 0x17

    /**
     * Data type is Random Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_RANDOM_TARGET_ADDRESS = 0x18

    /**
     * Data type is Appearance, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_APPEARANCE = 0x19

    /**
     * Data type is Advertising Interval, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_ADVERTISING_INTERVAL = 0x1A

    /**
     * Data type is LE Bluetooth Device Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_LE_BLUETOOTH_DEVICE_ADDRESS = 0x1B

    /**
     * Data type is LE Role, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_LE_ROLE = 0x1C

    /**
     * Data type is Simple Pairing Hash C-256, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SIMPLE_PAIRING_HASH_C_256 = 0x1D

    /**
     * Data type is Simple Pairing Randomizer R-256, see the Bluetooth Generic Access Profile for
     * more details.
     */
    const val DATA_TYPE_SIMPLE_PAIRING_RANDOMIZER_R_256 = 0x1E

    /**
     * Data type is List of 32-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT = 0x1F

    /**
     * Data type is Service Data - 32-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SERVICE_DATA_32_BIT = 0x20

    /**
     * Data type is Service Data - 128-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_SERVICE_DATA_128_BIT = 0x21

    /**
     * Data type is LE Secure Connections Confirmation Value, see the Bluetooth Generic Access
     * Profile for more details.
     */
    const val DATA_TYPE_LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE = 0x22

    /**
     * Data type is LE Secure Connections Random Value, see the Bluetooth Generic Access Profile for
     * more details.
     */
    const val DATA_TYPE_LE_SECURE_CONNECTIONS_RANDOM_VALUE = 0x23

    /**
     * Data type is URI, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_URI = 0x24

    /**
     * Data type is Indoor Positioning, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_INDOOR_POSITIONING = 0x25

    /**
     * Data type is Transport Discovery Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_TRANSPORT_DISCOVERY_DATA = 0x26

    /**
     * Data type is LE Supported Features, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_LE_SUPPORTED_FEATURES = 0x27

    /**
     * Data type is Channel Map Update Indication, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_CHANNEL_MAP_UPDATE_INDICATION = 0x28

    /**
     * Data type is PB-ADV, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_PB_ADV = 0x29

    /**
     * Data type is Mesh Message, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_MESH_MESSAGE = 0x2A

    /**
     * Data type is Mesh Beacon, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_MESH_BEACON = 0x2B

    /**
     * Data type is BIGInfo, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_BIG_INFO = 0x2C

    /**
     * Data type is Broadcast_Code, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_BROADCAST_CODE = 0x2D

    /**
     * Data type is Resolvable Set Identifier, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_RESOLVABLE_SET_IDENTIFIER = 0x2E

    /**
     * Data type is Advertising Interval - long, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_ADVERTISING_INTERVAL_LONG = 0x2F

    /**
     * Data type is 3D Information Data, see the Bluetooth Generic Access Profile for more details.
     */
    const val DATA_TYPE_3D_INFORMATION_DATA = 0x3D

    /**
     * Data type is Manufacturer Specific Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    const val DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF

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
    fun parseFromBytes(scanRecord: ByteArray?): BleScanRecord? {
        if (scanRecord == null) {
            return null
        }
        var currentPos = 0
        var advertiseFlag = -1
        var serviceUuids: MutableList<ParcelUuid>? = ArrayList()
        val serviceSolicitationUuids: MutableList<ParcelUuid> = ArrayList()
        var localName: String? = null
        var txPowerLevel = Int.MIN_VALUE
        val manufacturerData = SparseArray<ByteArray>()
        val serviceData: MutableMap<ParcelUuid, ByteArray> = ArrayMap()
        val advertisingDataMap = HashMap<Int, ByteArray>()
        var transportDiscoveryData: TransportDiscoveryData? = null
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
                advertisingDataMap[fieldType] = advertisingData
                when (fieldType) {
                    DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toInt() and 0xFF
                    DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos,
                        dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids
                    )

                    DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids
                    )

                    DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos, dataLength,
                        BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids
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
                    DATA_TYPE_SERVICE_DATA_16_BIT, DATA_TYPE_SERVICE_DATA_32_BIT, DATA_TYPE_SERVICE_DATA_128_BIT -> {
                        var serviceUuidLength: Int = BluetoothUuid.UUID_BYTES_16_BIT
                        if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                            serviceUuidLength = BluetoothUuid.UUID_BYTES_32_BIT
                        } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                            serviceUuidLength = BluetoothUuid.UUID_BYTES_128_BIT
                        }
                        val serviceDataUuidBytes = extractBytes(
                            scanRecord, currentPos,
                            serviceUuidLength
                        )
                        val serviceDataUuid: ParcelUuid = BluetoothUuid.parseUuidFrom(
                            serviceDataUuidBytes
                        )
                        val serviceDataArray = extractBytes(
                            scanRecord,
                            currentPos + serviceUuidLength, dataLength - serviceUuidLength
                        )
                        serviceData[serviceDataUuid] = serviceDataArray
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
                        manufacturerData.put(manufacturerId, manufacturerDataBytes)
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
            if (serviceUuids!!.isEmpty()) {
                serviceUuids = null
            }
            BleScanRecord(
                advertiseFlag = advertiseFlag,
                serviceUuids = serviceUuids,
                serviceData = serviceData,
                serviceSolicitationUuids = serviceSolicitationUuids,
                deviceName = localName,
                txPowerLevel = txPowerLevel,
                bytes = scanRecord,
                manufacturerSpecificData = manufacturerData
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Parse service UUIDs.
    private fun parseServiceUuid(
        scanRecord: ByteArray, currentPos: Int, dataLength: Int,
        uuidLength: Int, serviceUuids: MutableList<ParcelUuid>?,
    ): Int {
        var currentPos = currentPos
        var dataLength = dataLength
        while (dataLength > 0) {
            val uuidBytes = extractBytes(
                scanRecord, currentPos,
                uuidLength
            )
            serviceUuids!!.add(BluetoothUuid.parseUuidFrom(uuidBytes))
            dataLength -= uuidLength
            currentPos += uuidLength
        }
        return currentPos
    }

    /**
     * Parse service Solicitation UUIDs.
     */
    private fun parseServiceSolicitationUuid(
        scanRecord: ByteArray, currentPos: Int,
        dataLength: Int, uuidLength: Int, serviceSolicitationUuids: MutableList<ParcelUuid>,
    ): Int {
        var currentPos = currentPos
        var dataLength = dataLength
        while (dataLength > 0) {
            val uuidBytes = extractBytes(scanRecord, currentPos, uuidLength)
            serviceSolicitationUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes))
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
