/*
 * Copyright (c) 2023, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.kotlin.ble.profile.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.gls.data.NumberOfRecordsData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordAccessControlPointData
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ResponseData
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPOpCode
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPResponseCode

object RecordAccessControlPointParser {
    private const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5
    private const val OP_CODE_RESPONSE_CODE = 6
    private const val OPERATOR_NULL = 0

    fun parse(bytes: DataByteArray): RecordAccessControlPointData? {

        if (bytes.size < 3) {
            return null
        }

        val opCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 0) ?: return null
        if (opCode != OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE && opCode != OP_CODE_RESPONSE_CODE) {
            return null
        }

        val operator: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 1) ?: return null
        if (operator != OPERATOR_NULL) {
            return null
        }

        when (opCode) {
            OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE -> {
                // Field size is defined per service
                val numberOfRecords: Int = when (bytes.size - 2) {
                    1 -> bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
                    2 -> bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 2) ?: return null
                    4 -> bytes.getIntValue(IntFormat.FORMAT_UINT32_LE, 2) ?: return null
                    else -> {
                        // Other field sizes are not supported
                        return null
                    }
                }
                return NumberOfRecordsData(numberOfRecords)
            }
            OP_CODE_RESPONSE_CODE -> {
                if (bytes.size != 4) {
                    return null
                }
                val requestCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
                val racpOpCode = RACPOpCode.create(requestCode)
                val responseCode: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
                val racpResponseCode = RACPResponseCode.create(responseCode)
                return ResponseData(racpOpCode, racpResponseCode)
            }
        }
        return null
    }
}
