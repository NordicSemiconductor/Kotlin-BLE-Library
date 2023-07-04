package no.nordicsemi.android.kotlin.ble.mock.parsers

class AdvertiseToScanRecord {
    // Each fields need one byte for field length and another byte for field type.
    private val OVERHEAD_BYTES_PER_FIELD = 2

    // Flags field will be set by system.
    private val FLAGS_FIELaD_BYTES = 3

    private val MANUFACTURER_SPECIFIC_DATA_LENGTH = 2

    private val flags: Int = 0b00000110

//    private fun BleAdvertiseData.toRawBytes(): ByteArray {
//        val result = ByteBuffer.allocate(totalBytes(this, true))
//    }

//    private fun totalBytes(data: BleAdvertiseData?, isFlagsIncluded: Boolean): Int {
//        if (data == null) return 0
//        // Flags field is omitted if the advertising is not connectable.
//        var size = if (isFlagsIncluded) FLAGS_FIELD_BYTES else 0
//        if (data.serviceUuids != null) {
//            var num16BitUuids = 0
//            var num32BitUuids = 0
//            var num128BitUuids = 0
//            for (uuid in data.serviceUuids) {
//                if (is16BitUuid(uuid)) {
//                    ++num16BitUuids
//                } else if (is32BitUuid(uuid)) {
//                    ++num32BitUuids
//                } else {
//                    ++num128BitUuids
//                }
//            }
//            // 16 bit service uuids are grouped into one field when doing advertising.
//            if (num16BitUuids != 0) {
//                size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * BluetoothUuid.UUID_BYTES_16_BIT
//            }
//            // 32 bit service uuids are grouped into one field when doing advertising.
//            if (num32BitUuids != 0) {
//                size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * BluetoothUuid.UUID_BYTES_32_BIT
//            }
//            // 128 bit service uuids are grouped into one field when doing advertising.
//            if (num128BitUuids != 0) {
//                size += (OVERHEAD_BYTES_PER_FIELD
//                        + num128BitUuids * BluetoothUuid.UUID_BYTES_128_BIT)
//            }
//        }
//        if (data.serviceSolicitationUuids != null) {
//            var num16BitUuids = 0
//            var num32BitUuids = 0
//            var num128BitUuids = 0
//            for (uuid in data.serviceSolicitationUuids) {
//                if (is16BitUuid(uuid)) {
//                    ++num16BitUuids
//                } else if (is32BitUuid(uuid)) {
//                    ++num32BitUuids
//                } else {
//                    ++num128BitUuids
//                }
//            }
//            // 16 bit service uuids are grouped into one field when doing advertising.
//            if (num16BitUuids != 0) {
//                size += OVERHEAD_BYTES_PER_FIELD + num16BitUuids * BluetoothUuid.UUID_BYTES_16_BIT
//            }
//            // 32 bit service uuids are grouped into one field when doing advertising.
//            if (num32BitUuids != 0) {
//                size += OVERHEAD_BYTES_PER_FIELD + num32BitUuids * BluetoothUuid.UUID_BYTES_32_BIT
//            }
//            // 128 bit service uuids are grouped into one field when doing advertising.
//            if (num128BitUuids != 0) {
//                size += (OVERHEAD_BYTES_PER_FIELD
//                        + num128BitUuids * BluetoothUuid.UUID_BYTES_128_BIT)
//            }
//        }
////        for (transportDiscoveryData in data.transportDiscoveryData) {
////            size += OVERHEAD_BYTES_PER_FIELD + transportDiscoveryData.totalBytes()
////        }
//        for (uuid in data.serviceData.keys) {
//            val uuidLen = uuidToBytes(uuid).size
//            size += (OVERHEAD_BYTES_PER_FIELD + uuidLen
//                    + byteLength(data.serviceData[uuid]))
//        }
//        for (i in 0 until data.manufacturerSpecificData.size()) {
//            size += (OVERHEAD_BYTES_PER_FIELD + MANUFACTURER_SPECIFIC_DATA_LENGTH
//                    + byteLength(data.manufacturerSpecificData[i]))
//        }
//        if (data.includeTxPowerLevel) {
//            size += OVERHEAD_BYTES_PER_FIELD + 1 // tx power level value is one byte.
//        }
//        if (data.includeDeviceName) {
//            val length: Int = mBluetoothAdapter.getNameLengthForAdvertise()
//            if (length >= 0) {
//                size += OVERHEAD_BYTES_PER_FIELD + length
//            }
//        }
//        return size
//    }

    private fun byteLength(array: ByteArray?): Int {
        return array?.size ?: 0
    }
}
