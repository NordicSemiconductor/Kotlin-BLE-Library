/*
 * Copyright (c) 2024, Nordic Semiconductor
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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.core

/**
 * Gatt operation status.
 *
 * It contains values presented in documentation and additional helper
 * values observed and needed during real device operation.
 */
enum class OperationStatus {
    /** A GATT operation completed successfully. */
    SUCCESS,
    /** Unknown error. */
    UNKNOWN_ERROR,
    /** Most generic GATT error code. */
    GATT_ERROR,
    /** A remote device connection is congested. */
    CONNECTION_CONGESTED,
    /** A write operation exceeds the maximum length of the attribute. */
    INVALID_ATTRIBUTE_LENGTH,
    /** A read or write operation was requested with an invalid offset. */
    INVALID_OFFSET,
    /**GATT read operation is not permitted. */
    READ_NOT_PERMITTED,
    /** GATT write operation is not permitted. */
    WRITE_NOT_PERMITTED,
    /** The characteristic does not support subscribing for value change. */
    SUBSCRIBE_NOT_PERMITTED,
    /** The given request is not supported. */
    REQUEST_NOT_SUPPORTED,
    /** Insufficient encryption for a given operation. */
    INSUFFICIENT_ENCRYPTION,
    /** Insufficient authentication for a given operation. */
    INSUFFICIENT_AUTHENTICATION,
    /**Insufficient authorization for a given operation. */
    INSUFFICIENT_AUTHORIZATION,
    /** Device is busy. */
    BUSY;

    val isSuccess
        get() = this == SUCCESS

    override fun toString() = when (this) {
        SUCCESS -> "Success"
        UNKNOWN_ERROR -> "Unknown error"
        GATT_ERROR -> "GATT error"
        CONNECTION_CONGESTED -> "Connection congested"
        INVALID_ATTRIBUTE_LENGTH -> "Invalid attribute length"
        INVALID_OFFSET -> "Invalid offset"
        READ_NOT_PERMITTED -> "Read not permitted"
        WRITE_NOT_PERMITTED -> "Write not permitted"
        SUBSCRIBE_NOT_PERMITTED -> "Subscribe not permitted"
        REQUEST_NOT_SUPPORTED -> "Request not supported"
        INSUFFICIENT_ENCRYPTION -> "Insufficient encryption"
        INSUFFICIENT_AUTHENTICATION -> "Insufficient authentication"
        INSUFFICIENT_AUTHORIZATION -> "Insufficient authorization"
        BUSY -> "Busy"
    }
}
