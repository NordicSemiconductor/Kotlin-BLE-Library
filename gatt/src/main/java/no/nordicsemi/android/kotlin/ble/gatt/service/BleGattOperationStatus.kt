/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.gatt.service

enum class BleGattOperationStatus(internal val value: Int) {

    GATT_SUCCESS(0),

    GATT_CONNECTION_CONGESTED(143),
    GATT_FAILURE(257),
    GATT_INSUFFICIENT_AUTHENTICATION(5),
    GATT_INSUFFICIENT_AUTHORIZATION(8),
    GATT_INSUFFICIENT_ENCRYPTION(15),
    GATT_INVALID_ATTRIBUTE_LENGTH(13),
    GATT_INVALID_OFFSET(7),
    GATT_READ_NOT_PERMITTED(2),
    GATT_REQUEST_NOT_SUPPORTED(6),
    GATT_WRITE_NOT_PERMITTED(3);

    companion object {
        fun create(value: Int): BleGattOperationStatus {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalStateException("Cannot create status object for value: $value")
        }
    }
}
