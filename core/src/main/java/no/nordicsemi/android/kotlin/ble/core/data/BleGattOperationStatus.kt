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

package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothGatt
import android.os.Build
import android.util.Log

/**
 * Gatt operation status. It contains values presented in documentation and additional helper
 * values observed and needed during real device operation.
 *
 * @property value Native Android value.
 */
enum class BleGattOperationStatus(val value: Int) {

    /**
     * Unknown error.
     */
    GATT_UNKNOWN(-1),

    /**
     * Most generic GATT error code.
     */
    GATT_ERROR(133),

    /**
     * A GATT operation completed successfully.
     */
    GATT_SUCCESS(BluetoothGatt.GATT_SUCCESS),

    /**
     * A remote device connection is congested.
     */
    GATT_CONNECTION_CONGESTED(BluetoothGatt.GATT_CONNECTION_CONGESTED),

    /**
     * A GATT operation failed, errors other than the above.
     */
    GATT_FAILURE(BluetoothGatt.GATT_FAILURE),

    /**
     * Insufficient authentication for a given operation.
     */
    GATT_INSUFFICIENT_AUTHENTICATION(BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION),

    /**
     * Insufficient encryption for a given operation.
     */
    GATT_INSUFFICIENT_ENCRYPTION(BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION),

    /**
     * A write operation exceeds the maximum length of the attribute.
     */
    GATT_INVALID_ATTRIBUTE_LENGTH(BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH),

    /**
     * A read or write operation was requested with an invalid offset.
     */
    GATT_INVALID_OFFSET(BluetoothGatt.GATT_INVALID_OFFSET),

    /**
     * GATT read operation is not permitted.
     */
    GATT_READ_NOT_PERMITTED(BluetoothGatt.GATT_READ_NOT_PERMITTED),

    /**
     * The given request is not supported.
     */
    GATT_REQUEST_NOT_SUPPORTED(BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED),

    /**
     * GATT write operation is not permitted.
     */
    GATT_WRITE_NOT_PERMITTED(BluetoothGatt.GATT_WRITE_NOT_PERMITTED),

    /**
     * Insufficient authorization for a given operation.
     */
    GATT_INSUFFICIENT_AUTHORIZATION(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION
        } else {
            8
        }
    );

    val isSuccess
        get() = this == GATT_SUCCESS

    companion object {
        fun create(value: Int): BleGattOperationStatus {
            return values().firstOrNull { it.value == value } ?: GATT_UNKNOWN
        }
    }
}

fun BleGattOperationStatus.toLogLevel(): Int {
    return if(isSuccess) {
        Log.DEBUG
    } else {
        Log.ERROR
    }
}
