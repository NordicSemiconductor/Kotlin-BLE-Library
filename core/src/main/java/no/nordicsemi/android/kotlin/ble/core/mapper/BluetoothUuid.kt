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

package no.nordicsemi.android.kotlin.ble.core.mapper

import android.annotation.SuppressLint
import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.UUID

/**
 * Static helper methods and constants to decode the ParcelUuid of remote devices. Bluetooth service
 * UUIDs are defined in the SDP section of the Bluetooth Assigned Numbers document. The constant
 * 128 bit values in this class are calculated as: uuid * 2^96 + [.BASE_UUID].
 *
 * @see [BluetoothUuid](https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothUuid.java)
 *
 * @hide
 */
@SuppressLint("AndroidFrameworkBluetoothPermission")
internal object BluetoothUuid {
    /**
     * UUID corresponding to the Audio sink role (also referred to as the A2DP sink role).
     *
     * @hide
     */
    val A2DP_SINK = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Audio source role (also referred to as the A2DP source role).
     *
     * @hide
     */
    val A2DP_SOURCE = ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Advanced Audio Distribution Profile (A2DP).
     *
     * @hide
     */
    val ADV_AUDIO_DIST = ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Headset Profile (HSP).
     *
     * @hide
     */
    val HSP = ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Headset Profile (HSP) Audio Gateway role.
     *
     * @hide
     */
    val HSP_AG = ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Hands-Free Profile (HFP).
     *
     * @hide
     */
    val HFP = ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Hands-Free Profile (HFP) Audio Gateway role.
     *
     * @hide
     */
    val HFP_AG = ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Audio Video Remote Control Profile (AVRCP).
     *
     * @hide
     */
    val AVRCP = ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Audio Video Remote Control Profile (AVRCP) controller role.
     *
     * @hide
     */
    val AVRCP_CONTROLLER = ParcelUuid.fromString("0000110F-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Audio Video Remote Control Profile (AVRCP) target role.
     *
     * @hide
     */
    val AVRCP_TARGET = ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the OBject EXchange (OBEX) Object Push Profile (OPP).
     *
     * @hide
     */
    val OBEX_OBJECT_PUSH = ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb")

    /**
     * UUID corresponding to the Human Interface Device (HID) profile.
     *
     * @hide
     */
    val HID = ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb")

    /**
     * UUID corresponding to the Human Interface Device over GATT Profile (HOGP).
     *
     * @hide
     */
    val HOGP = ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb")

    /**
     * UUID corresponding to the Personal Area Network User (PANU) role.
     *
     * @hide
     */
    val PANU = ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Network Access Point (NAP) role.
     *
     * @hide
     */
    val NAP = ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Bluetooth Network Encapsulation Protocol (BNEP).
     *
     * @hide
     */
    val BNEP = ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Phonebook Access Profile (PBAP) client role.
     *
     * @hide
     */
    val PBAP_PCE = ParcelUuid.fromString("0000112e-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Phonebook Access Profile (PBAP) server role.
     *
     * @hide
     */
    val PBAP_PSE = ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Message Access Profile (MAP).
     *
     * @hide
     */
    val MAP = ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Message Notification Server (MNS) role.
     *
     * @hide
     */
    val MNS = ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Message Access Server (MAS) role.
     *
     * @hide
     */
    val MAS = ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Sim Access Profile (SAP).
     *
     * @hide
     */
    val SAP = ParcelUuid.fromString("0000112D-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Hearing Aid Profile.
     *
     * @hide
     */
    val HEARING_AID = ParcelUuid.fromString("0000FDF0-0000-1000-8000-00805f9b34fb")

    /**
     * UUID corresponding to the Hearing Access Service (HAS).
     *
     * @hide
     */
    val HAS = ParcelUuid.fromString("00001854-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to Audio Stream Control (also known as Bluetooth Low Energy Audio).
     *
     * @hide
     */
    val LE_AUDIO = ParcelUuid.fromString("0000184E-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Device Identification Profile (DIP).
     *
     * @hide
     */
    val DIP = ParcelUuid.fromString("00001200-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Volume Control Service.
     *
     * @hide
     */
    val VOLUME_CONTROL = ParcelUuid.fromString("00001844-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Generic Media Control Service.
     *
     * @hide
     */
    val GENERIC_MEDIA_CONTROL = ParcelUuid.fromString("00001849-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Media Control Service.
     *
     * @hide
     */
    val MEDIA_CONTROL = ParcelUuid.fromString("00001848-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Coordinated Set Identification Service.
     *
     * @hide
     */
    val COORDINATED_SET = ParcelUuid.fromString("00001846-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Common Audio Service.
     *
     * @hide
     */
    val CAP = ParcelUuid.fromString("00001853-0000-1000-8000-00805F9B34FB")

    /**
     * UUID corresponding to the Broadcast Audio Scan Service (also known as LE Audio Broadcast
     * Assistant).
     *
     * @hide
     */
    val BATTERY = ParcelUuid.fromString("0000180F-0000-1000-8000-00805F9B34FB")

    /** @hide
     */
    val BASS = ParcelUuid.fromString("0000184F-0000-1000-8000-00805F9B34FB")

    /**
     * Telephony and Media Audio Profile (TMAP) UUID
     * @hide
     */
    val TMAP = ParcelUuid.fromString("00001855-0000-1000-8000-00805F9B34FB")

    /** @hide
     */
    val BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")

    /**
     * Length of bytes for 16 bit UUID
     *
     * @hide
     */
    val UUID_BYTES_16_BIT = 2

    /**
     * Length of bytes for 32 bit UUID
     *
     * @hide
     */
    val UUID_BYTES_32_BIT = 4

    /**
     * Length of bytes for 128 bit UUID
     *
     * @hide
     */
    val UUID_BYTES_128_BIT = 16

    /**
     * Returns true if there any common ParcelUuids in uuidA and uuidB.
     *
     * @param uuidA - List of ParcelUuids
     * @param uuidB - List of ParcelUuids
     *
     * @hide
     */
    fun containsAnyUuid(
        uuidA: Array<ParcelUuid?>?,
        uuidB: Array<ParcelUuid>?,
    ): Boolean {
        if (uuidA == null && uuidB == null) return true
        if (uuidA == null) {
            return uuidB!!.size == 0
        }
        if (uuidB == null) {
            return uuidA.size == 0
        }
        val uuidSet = HashSet(Arrays.asList(*uuidA))
        for (uuid in uuidB) {
            if (uuidSet.contains(uuid)) return true
        }
        return false
    }

    /**
     * Extract the Service Identifier or the actual uuid from the Parcel Uuid.
     * For example, if 0000110B-0000-1000-8000-00805F9B34FB is the parcel Uuid,
     * this function will return 110B
     *
     * @param parcelUuid
     * @return the service identifier.
     */
    private fun getServiceIdentifierFromParcelUuid(parcelUuid: ParcelUuid): Int {
        val uuid = parcelUuid.uuid
        val value = uuid.mostSignificantBits and -0x100000000L ushr 32
        return value.toInt()
    }

    /**
     * Parse UUID from bytes. The `uuidBytes` can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return [ParcelUuid] parsed from bytes.
     * @throws IllegalArgumentException If the `uuidBytes` cannot be parsed.
     *
     * @hide
     */
    fun parseUuidFrom(uuidBytes: ByteArray?): ParcelUuid {
        requireNotNull(uuidBytes) { "uuidBytes cannot be null" }
        val length = uuidBytes.size
        require(length == UUID_BYTES_16_BIT || length == UUID_BYTES_32_BIT || length == UUID_BYTES_128_BIT) { "uuidBytes length invalid - $length" }
        // Construct a 128 bit UUID.
        if (length == UUID_BYTES_128_BIT) {
            val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
            val msb = buf.getLong(8)
            val lsb = buf.getLong(0)
            return ParcelUuid(UUID(msb, lsb))
        }
        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        var shortUuid: Long
        if (length == UUID_BYTES_16_BIT) {
            shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
            shortUuid += (uuidBytes[1].toInt() and 0xFF shl 8).toLong()
        } else {
            shortUuid = (uuidBytes[0].toInt() and 0xFF).toLong()
            shortUuid += (uuidBytes[1].toInt() and 0xFF shl 8).toLong()
            shortUuid += (uuidBytes[2].toInt() and 0xFF shl 16).toLong()
            shortUuid += (uuidBytes[3].toInt() and 0xFF shl 24).toLong()
        }
        val msb = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
        val lsb = BASE_UUID.uuid.leastSignificantBits
        return ParcelUuid(UUID(msb, lsb))
    }

    /**
     * Parse UUID to bytes. The returned value is shortest representation, a 16-bit, 32-bit or
     * 128-bit UUID, Note returned value is little endian (Bluetooth).
     *
     * @param uuid uuid to parse.
     * @return shortest representation of `uuid` as bytes.
     * @throws IllegalArgumentException If the `uuid` is null.
     *
     * @hide
     */
    fun uuidToBytes(uuid: ParcelUuid?): ByteArray {
        requireNotNull(uuid) { "uuid cannot be null" }
        if (is16BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_16_BIT)
            val uuidVal = getServiceIdentifierFromParcelUuid(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            return uuidBytes
        }
        if (is32BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_32_BIT)
            val uuidVal = getServiceIdentifierFromParcelUuid(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            uuidBytes[2] = (uuidVal and 0xFF0000 shr 16).toByte()
            uuidBytes[3] = (uuidVal and -0x1000000 shr 24).toByte()
            return uuidBytes
        }
        // Construct a 128 bit UUID.
        val msb = uuid.uuid.mostSignificantBits
        val lsb = uuid.uuid.leastSignificantBits
        val uuidBytes = ByteArray(UUID_BYTES_128_BIT)
        val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
        buf.putLong(8, msb)
        buf.putLong(0, lsb)
        return uuidBytes
    }

    /**
     * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
     *
     * @param parcelUuid
     * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
     *
     * @hide
     */
    fun is16BitUuid(parcelUuid: ParcelUuid): Boolean {
        val uuid = parcelUuid.uuid
        return if (uuid.leastSignificantBits != BASE_UUID.uuid.leastSignificantBits) {
            false
        } else uuid.mostSignificantBits and -0xffff00000001L == 0x1000L
    }

    /**
     * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
     *
     * @param parcelUuid
     * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
     *
     * @hide
     */
    fun is32BitUuid(parcelUuid: ParcelUuid): Boolean {
        val uuid = parcelUuid.uuid
        if (uuid.leastSignificantBits != BASE_UUID.uuid.leastSignificantBits) {
            return false
        }
        return if (is16BitUuid(parcelUuid)) {
            false
        } else uuid.mostSignificantBits and 0xFFFFFFFFL == 0x1000L
    }
}