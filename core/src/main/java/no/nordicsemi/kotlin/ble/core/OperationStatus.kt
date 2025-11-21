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
 * GATT operation status.
 *
 * Some of the status codes are defined by the Bluetooth Specification, others are
 * used only in the library to indicate specific conditions.
 *
 * Read more in Bluetooth Specification, Vol 3 (Host), Part F (ATT), 3.4.1.1 ATT_ERROR_RSP:
 * [link](https://www.bluetooth.com/wp-content/uploads/Files/Specification/HTML/Core-62/out/en/host/attribute-protocol--att-.html#UUID-eefd3e8d-9b16-3af8-1fb4-fa90f52262e8).
 */
enum class OperationStatus {
    /** A GATT operation completed successfully. */
    SUCCESS, // 0x00
    /** The attribute handle given was not valid on this server. */
    INVALID_HANDLE, // 0x01
    /**GATT read operation is not permitted. */
    READ_NOT_PERMITTED, // 0x02
    /** GATT write operation is not permitted. */
    WRITE_NOT_PERMITTED, // 0x03
    /** The attribute PDU was invalid. */
    INVALID_PDU, // 0x04
    /** Insufficient authentication for a given operation. */
    INSUFFICIENT_AUTHENTICATION, // 0x05
    /** The given request is not supported. */
    REQUEST_NOT_SUPPORTED, // 0x06
    /** A read or write operation was requested with an invalid offset. */
    INVALID_OFFSET, // 0x07
    /**Insufficient authorization for a given operation. */
    INSUFFICIENT_AUTHORIZATION, // x08
    /** The prepare write queue is full. */
    PREPARE_QUEUE_FULL, // 0x09
    /** No attribute found within the given attribute handle range. */
    ATTRIBUTE_NOT_FOUND, // 0x0A
    /** The attribute cannot be read using the *Long Read* procedure. */
    ATTRIBUTE_NOT_LONG, // 0x0B
    /** The Encryption Key Size used for encrypting this link is too short. */
    ENCRYPTION_KEY_TOO_SHORT, // 0x0C
    /** A write operation exceeds the maximum length of the attribute. */
    INVALID_ATTRIBUTE_LENGTH, // 0x0D
    /** The attribute request that was requested has encountered an error that was unlikely, and therefore could not be completed as requested. */
    UNLIKELY_ERROR, // 0x0E
    /** Insufficient encryption for a given operation. */
    INSUFFICIENT_ENCRYPTION, // 0x0F
    /** Insufficient Resources to complete the request. */
    INSUFFICIENT_RESOURCES, // 0x11
    /** The attribute parameter value was not allowed. */
    VALUE_NOT_ALLOWED, // 0x13
    /** The characteristic does not support subscribing for value change. */
    SUBSCRIBE_NOT_PERMITTED,
    /** A remote device connection is congested. */
    CONNECTION_CONGESTED, // 0x8F
    /** Device is busy. */
    BUSY,
    /** Most generic GATT error code. */
    GATT_ERROR,
    /** Unknown error. */
    UNKNOWN_ERROR;

    val isSuccess
        get() = this == SUCCESS

    override fun toString() = when (this) {
        SUCCESS -> "Success"
        INVALID_HANDLE -> "Invalid handle"
        READ_NOT_PERMITTED -> "Read not permitted"
        WRITE_NOT_PERMITTED -> "Write not permitted"
        INVALID_ATTRIBUTE_LENGTH -> "Invalid attribute length"
        INVALID_OFFSET -> "Invalid offset"
        SUBSCRIBE_NOT_PERMITTED -> "Subscribe not permitted"
        REQUEST_NOT_SUPPORTED -> "Request not supported"
        PREPARE_QUEUE_FULL -> "Prepare queue full"
        INSUFFICIENT_ENCRYPTION -> "Insufficient encryption"
        INSUFFICIENT_AUTHENTICATION -> "Insufficient authentication"
        INSUFFICIENT_AUTHORIZATION -> "Insufficient authorization"
        INSUFFICIENT_RESOURCES -> "Insufficient resources"
        INVALID_PDU -> "Invalid PDU"
        ATTRIBUTE_NOT_FOUND -> "Attribute not found"
        ATTRIBUTE_NOT_LONG -> "Attribute not long"
        ENCRYPTION_KEY_TOO_SHORT -> "Encryption key too short"
        VALUE_NOT_ALLOWED -> "Value not allowed"
        UNLIKELY_ERROR -> "Unlikely error"
        BUSY -> "Busy"
        CONNECTION_CONGESTED -> "Connection congested"
        GATT_ERROR -> "GATT error"
        UNKNOWN_ERROR -> "Unknown error"
    }
}
