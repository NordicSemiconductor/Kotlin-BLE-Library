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

package no.nordicsemi.android.kotlin.ble.profile.gls.data

import no.nordicsemi.android.kotlin.ble.profile.racp.RACPOpCode
import no.nordicsemi.android.kotlin.ble.profile.racp.RACPResponseCode

sealed interface RecordAccessControlPointData {
    val operationCompleted: Boolean
}

data class NumberOfRecordsData(
    val numberOfRecords: Int
) : RecordAccessControlPointData {

    override val operationCompleted: Boolean = true
}

data class ResponseData(
    val requestCode: RACPOpCode,
    val responseCode: RACPResponseCode
) : RecordAccessControlPointData {

    override val operationCompleted: Boolean = when (responseCode) {
        RACPResponseCode.RACP_RESPONSE_SUCCESS,
        RACPResponseCode.RACP_ERROR_NO_RECORDS_FOUND -> true
        RACPResponseCode.RACP_ERROR_OP_CODE_NOT_SUPPORTED,
        RACPResponseCode.RACP_ERROR_INVALID_OPERATOR,
        RACPResponseCode.RACP_ERROR_OPERATOR_NOT_SUPPORTED,
        RACPResponseCode.RACP_ERROR_INVALID_OPERAND,
        RACPResponseCode.RACP_ERROR_ABORT_UNSUCCESSFUL,
        RACPResponseCode.RACP_ERROR_PROCEDURE_NOT_COMPLETED,
        RACPResponseCode.RACP_ERROR_OPERAND_NOT_SUPPORTED -> false
    }
}
