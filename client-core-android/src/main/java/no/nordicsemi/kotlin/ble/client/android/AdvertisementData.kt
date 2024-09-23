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

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.ble.client.android

import no.nordicsemi.kotlin.ble.client.AdvertisementData
import no.nordicsemi.kotlin.ble.core.util.fromBytes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Numbers are taken from Assigned Numbers: 2.3 Common Data Types:
// https://www.bluetooth.com/specifications/assigned-numbers/
private const val AD_TYPE_FLAGS = 0x01
private const val AD_TYPE_INCOMPLETE_LIST_16_BIT_SERVICE_UUIDS = 0x02
private const val AD_TYPE_COMPLETE_LIST_16_BIT_SERVICE_UUIDS = 0x03
private const val AD_TYPE_INCOMPLETE_LIST_32_BIT_SERVICE_UUIDS = 0x04
private const val AD_TYPE_COMPLETE_LIST_32_BIT_SERVICE_UUIDS = 0x05
private const val AD_TYPE_INCOMPLETE_LIST_128_BIT_SERVICE_UUIDS = 0x06
private const val AD_TYPE_COMPLETE_LIST_128_BIT_SERVICE_UUIDS = 0x07
private const val AD_TYPE_SHORT_LOCAL_NAME = 0x08
private const val AD_TYPE_COMPLETE_LOCAL_NAME = 0x09
private const val AD_TYPE_TX_POWER_LEVEL = 0x0A
private const val AD_TYPE_LIST_16_BIT_SOLICITATION_SERVICE_UUIDS = 0x14
private const val AD_TYPE_LIST_32_BIT_SOLICITATION_SERVICE_UUIDS = 0x1F
private const val AD_TYPE_LIST_128_BIT_SOLICITATION_SERVICE_UUIDS = 0x15
private const val AD_TYPE_SERVICE_DATA_16_BITS = 0x16
private const val AD_TYPE_SERVICE_DATA_32_BITS = 0x20
private const val AD_TYPE_SERVICE_DATA_128_BITS = 0x21
private const val AD_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF
private const val AD_TYPE_MESH_PB_ADV  = 0x29
private const val AD_TYPE_MESH_MESSAGE = 0x2A
private const val AD_TYPE_MESH_BEACON  = 0x2B

/**
 * Android-specific class of Bluetooth LE advertisement data.
 *
 * @property raw The raw advertisement data.
 * @property meshPbAdv The Bluetooth Mesh Provisioning PDU, if present.
 * @property meshMessage The Bluetooth Mesh Network PDU, if present.
 * @property meshBeacon The Bluetooth Mesh Beacon, if present.
 */
@OptIn(ExperimentalUuidApi::class)
class AdvertisementData(
    val raw: ByteArray
) : AdvertisementData {

    enum class Flag {
        LIMITED_DISCOVERABLE_MODE,
        GENERAL_DISCOVERABLE_MODE,
        BR_EDR_NOT_SUPPORTED,
        SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_CONTROLLER,
        @Deprecated("Deprecated in Bluetooth 6")
        SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_HOST;

        @Suppress("DEPRECATION")
        override fun toString(): String = when (this) {
            LIMITED_DISCOVERABLE_MODE -> "Limited Discoverable Mode"
            GENERAL_DISCOVERABLE_MODE -> "General Discoverable Mode"
            BR_EDR_NOT_SUPPORTED -> "BR/EDR Not Supported"
            SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_CONTROLLER -> "Simultaneous LE and BR/EDR to Same Device Capable (Controller)"
            SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_HOST -> "Simultaneous LE and BR/EDR to Same Device Capable (Host)"
        }
    }

    val flags: Set<Flag>?
    override val name: String?
    override val serviceUuids: List<Uuid>
    override val serviceSolicitationUuids: List<Uuid>
    override val serviceData: Map<Uuid, ByteArray>
    override val txPowerLevel: Int?
    override val manufacturerData: Map<Int, ByteArray>
    val meshPbAdv: ByteArray?
    val meshMessage: ByteArray?
    val meshBeacon: ByteArray?

    init {
        var flags: MutableSet<Flag>? = null
        var name: String? = null
        var serviceUuids: MutableList<Uuid>? = null
        var serviceSolicitationUuids: MutableList<Uuid>? = null
        var serviceData: MutableMap<Uuid, ByteArray>? = null
        var txPowerLevel: Int? = null
        var manufacturerData: MutableMap<Int, ByteArray>? = null
        var meshPbAdv: ByteArray? = null
        var meshMessage: ByteArray? = null
        var meshBeacon: ByteArray? = null

        // Advertisement data is a list of AD structures.
        var i = 0
        while (raw.size > i + 3) {
            // The first byte is the length of the AD structure.
            val length = (raw[i++].toInt() and 0xFF) - 1 // deduct 1 for the type byte

            // The data must be at least 1 byte and must not be longer than the remaining data.
            if (length < 1 || length >= raw.size - i) break

            // The second byte is the AD type, as specified in Assigned Numbers: 2.3 Common Data Types
            // https://www.bluetooth.com/specifications/assigned-numbers/
            val type = raw[i++].toInt() and 0xFF

            when (type) {
                AD_TYPE_FLAGS -> {
                    flags = (flags ?: mutableSetOf()).apply {
                        val value = raw[i].toInt()
                        if (value and 0x01 != 0) add(Flag.LIMITED_DISCOVERABLE_MODE)
                        if (value and 0x02 != 0) add(Flag.GENERAL_DISCOVERABLE_MODE)
                        if (value and 0x04 != 0) add(Flag.BR_EDR_NOT_SUPPORTED)
                        if (value and 0x08 != 0) add(Flag.SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_CONTROLLER)
                        @Suppress("DEPRECATION")
                        if (value and 0x10 != 0) add(Flag.SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_HOST)
                    }
                }
                AD_TYPE_SHORT_LOCAL_NAME,
                AD_TYPE_COMPLETE_LOCAL_NAME -> {
                    name = String(raw, i, length)
                }
                AD_TYPE_INCOMPLETE_LIST_16_BIT_SERVICE_UUIDS,
                AD_TYPE_COMPLETE_LIST_16_BIT_SERVICE_UUIDS -> {
                    serviceUuids = (serviceUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 2) {
                            add(Uuid.fromBytes(raw, j, 2))
                        }
                    }
                }
                AD_TYPE_INCOMPLETE_LIST_32_BIT_SERVICE_UUIDS,
                AD_TYPE_COMPLETE_LIST_32_BIT_SERVICE_UUIDS -> {
                    serviceUuids = (serviceUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 4) {
                            add(Uuid.fromBytes(raw, j, 4))
                        }
                    }
                }
                AD_TYPE_INCOMPLETE_LIST_128_BIT_SERVICE_UUIDS,
                AD_TYPE_COMPLETE_LIST_128_BIT_SERVICE_UUIDS -> {
                    serviceUuids = (serviceUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 16) {
                            add(Uuid.fromBytes(raw, j, 16))
                        }
                    }
                }
                AD_TYPE_SERVICE_DATA_16_BITS -> {
                    val uuid = Uuid.fromBytes(raw, i, 2)
                    val data = raw.copyOfRange(i + 2, i + length)
                    serviceData = (serviceData ?: mutableMapOf()).apply {
                        put(uuid, data)
                    }
                }
                AD_TYPE_SERVICE_DATA_32_BITS -> {
                    val uuid = Uuid.fromBytes(raw, i, 4)
                    val data = raw.copyOfRange(i + 4, i + length)
                    serviceData = (serviceData ?: mutableMapOf()).apply {
                        put(uuid, data)
                    }
                }
                AD_TYPE_SERVICE_DATA_128_BITS -> {
                    val uuid = Uuid.fromBytes(raw, i, 16)
                    val data = raw.copyOfRange(i + 16, i + length)
                    serviceData = (serviceData ?: mutableMapOf()).apply {
                        put(uuid, data)
                    }
                }
                AD_TYPE_TX_POWER_LEVEL -> {
                    txPowerLevel = raw[i].toInt()
                }
                AD_TYPE_LIST_16_BIT_SOLICITATION_SERVICE_UUIDS -> {
                    serviceSolicitationUuids = (serviceSolicitationUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 2) {
                            add(Uuid.fromBytes(raw, j, 2))
                        }
                    }
                }
                AD_TYPE_LIST_32_BIT_SOLICITATION_SERVICE_UUIDS -> {
                    serviceSolicitationUuids = (serviceSolicitationUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 4) {
                            add(Uuid.fromBytes(raw, j, 4))
                        }
                    }
                }
                AD_TYPE_LIST_128_BIT_SOLICITATION_SERVICE_UUIDS -> {
                    serviceSolicitationUuids = (serviceSolicitationUuids ?: mutableListOf()).apply {
                        for (j in i until i + length step 16) {
                            add(Uuid.fromBytes(raw, j, 16))
                        }
                    }
                }
                AD_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                    val companyId = (raw[i].toInt() and 0xFF) or (raw[i + 1].toInt() and 0xFF shl 8)
                    val data = raw.copyOfRange(i + 2, i + length)
                    manufacturerData = (manufacturerData ?: mutableMapOf()).apply {
                        put(companyId, data)
                    }
                }
                AD_TYPE_MESH_PB_ADV -> {
                    meshPbAdv = raw.copyOfRange(i, i + length)
                }
                AD_TYPE_MESH_MESSAGE -> {
                    meshMessage = raw.copyOfRange(i, i + length)
                }
                AD_TYPE_MESH_BEACON -> {
                    meshBeacon = raw.copyOfRange(i, i + length)
                }
            }

            i += length
        }

        this.flags = flags
        this.name = name
        this.serviceUuids = serviceUuids ?: emptyList()
        this.serviceSolicitationUuids = serviceSolicitationUuids?: emptyList()
        this.serviceData = serviceData ?: emptyMap()
        this.txPowerLevel = txPowerLevel
        this.manufacturerData = manufacturerData ?: emptyMap()
        this.meshPbAdv = meshPbAdv
        this.meshMessage = meshMessage
        this.meshBeacon = meshBeacon
    }
}
