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

@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.ble.core.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object BluetoothUuid {
    /*
     * Note:
     *
     * What is "-0x7FFFFF7FA064CB05", you ask?
     *
     * Due to the fact, that the least significant part of the Base Bluetooth UUID
     * (0x800000805f9b34fb) is outside of the range of Long, the value cannot be
     * simply written as UUID(0x0000000000001000, 0x800000805f9B34FB).
     * Instead, we take a 2's complement of the least significant part and invert the sign.
     */
    /**
     * Base UUID for 16-bit and 32-bit UUIDs.
     */
    val BASE_UUID: UUID by lazy { UUID(0x0000000000001000, -0x7FFFFF7FA064CB05) }

    /**
     * Length of bytes for 16 bit UUID.
     */
    const val UUID_BYTES_16_BIT = 2

    /**
     * Length of bytes for 32 bit UUID.
     */
    const val UUID_BYTES_32_BIT = 4

    /**
     * Length of bytes for 128 bit UUID.
     */
    const val UUID_BYTES_128_BIT = 16

    /**
     * Creates a 128-bit UUID from a 16-bit or 32-bit UUID using [Bluetooth Base UUID][BASE_UUID].
     *
     * @param serviceIdentifier The 16-bit or 32-bit UUID.
     * @return The 128-bit UUID.
     */
    fun uuid(serviceIdentifier: Int): UUID = UUID(
        BASE_UUID.mostSignificantBits or (serviceIdentifier.toLong() shl 32),
        BASE_UUID.leastSignificantBits
    )

    /**
     * Extract the 16-bit or 32-bit Service Identifier of the 128-bit UUID.
     *
     * For example, if `0000110B-0000-1000-8000-00805F9B34FB` is the parcel UUID, this
     * function will return `0x110B` as [Int].
     */
    fun getServiceIdentifier(uuid: UUID): Int {
        val value = uuid.mostSignificantBits and -0x100000000L ushr 32
        return value.toInt()
    }

    /**
     * Parse UUID to bytes.
     *
     * The returned value is shortest representation, a 16-bit, 32-bit or 128-bit UUID.
     * Note returned value is little endian (Bluetooth).
     *
     * @param uuid uuid to parse.
     * @return shortest representation of `uuid` as bytes.
     * @throws IllegalArgumentException If the `uuid` is null.
     */
    fun uuidToBytes(uuid: UUID): ByteArray {
        if (is16BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_16_BIT)
            val uuidVal = getServiceIdentifier(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            return uuidBytes
        }
        if (is32BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_32_BIT)
            val uuidVal = getServiceIdentifier(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            uuidBytes[2] = (uuidVal and 0xFF0000 shr 16).toByte()
            uuidBytes[3] = (uuidVal and 0xFF000000.toInt() shr 24).toByte()
            return uuidBytes
        }

        // Construct a 128 bit UUID.
        val uuidBytes = ByteArray(UUID_BYTES_128_BIT)
        val buf: ByteBuffer = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
        buf.putLong(8, uuid.mostSignificantBits)
        buf.putLong(0, uuid.leastSignificantBits)
        return uuidBytes
    }

    /**
     * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
     *
     * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
     */
    fun is16BitUuid(uuid: UUID): Boolean {
        if (uuid.leastSignificantBits != BASE_UUID.leastSignificantBits) {
            return false
        }
        return uuid.mostSignificantBits and -0xffff00000001L == 0x1000L
    }

    /**
     * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
     *
     * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
     */
    fun is32BitUuid(uuid: UUID): Boolean {
        if (uuid.leastSignificantBits != BASE_UUID.leastSignificantBits) {
            return false
        }
        return !is16BitUuid(uuid) && uuid.mostSignificantBits and 0xFFFFFFFFL == 0x1000L
    }
}