package no.nordicsemi.android.kotlin.ble.profile.gls

import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.NumberOfRecordsData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ResponseData
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPOpCode
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPResponseCode

object RecordAccessControlPointParser {
    private const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5
    private const val OP_CODE_RESPONSE_CODE = 6
    private const val OPERATOR_NULL = 0

    fun parse(byteArray: ByteArray): RecordAccessControlPointData? {
        val data = ByteData(byteArray)

        if (data.size() < 3) {
            return null
        }

        val opCode: Int = data.getIntValue(ByteData.FORMAT_UINT8, 0) ?: return null
        if (opCode != OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE && opCode != OP_CODE_RESPONSE_CODE) {
            return null
        }

        val operator: Int = data.getIntValue(ByteData.FORMAT_UINT8, 1) ?: return null
        if (operator != OPERATOR_NULL) {
            return null
        }

        when (opCode) {
            OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE -> {
                // Field size is defined per service
                val numberOfRecords: Int = when (data.size() - 2) {
                    1 -> data.getIntValue(ByteData.FORMAT_UINT8, 2) ?: return null
                    2 -> data.getIntValue(ByteData.FORMAT_UINT16_LE, 2) ?: return null
                    4 -> data.getIntValue(ByteData.FORMAT_UINT32_LE, 2) ?: return null
                    else -> {
                        // Other field sizes are not supported
                        return null
                    }
                }
                return NumberOfRecordsData(numberOfRecords)
            }
            OP_CODE_RESPONSE_CODE -> {
                if (data.size() != 4) {
                    return null
                }
                val requestCode: Int = data.getIntValue(ByteData.FORMAT_UINT8, 2) ?: return null
                val racpOpCode = RACPOpCode.create(requestCode)
                val responseCode: Int = data.getIntValue(ByteData.FORMAT_UINT8, 3) ?: return null
                val racpResponseCode = RACPResponseCode.create(responseCode)
                return ResponseData(racpOpCode, racpResponseCode)
            }
        }
        return null
    }
}
