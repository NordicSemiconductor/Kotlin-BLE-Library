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

package no.nordicsemi.kotlin.ble.core

/**
 * Advertising Data types (AD types).
 */
enum class AdvertisingDataType(val type: Int) {

    /**
     * Data type is Flags, see the Bluetooth Generic Access Profile for more details.
     */
    FLAGS(0x01),

    /**
     * Data type is Incomplete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    INCOMPLETE_LIST_OF_16_BIT_SERVICE_UUIDS(0x02),

    /**
     * Data type is Complete List of 16-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    COMPLETE_LIST_OF_16_BIT_SERVICE_UUIDS(0x03),

    /**
     * Data type is Incomplete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    INCOMPLETE_LIST_OF_32_BIT_SERVICE_UUIDS(0x04),

    /**
     * Data type is Complete List of 32-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    COMPLETE_LIST_OF_32_BIT_SERVICE_UUIDS(0x05),

    /**
     * Data type is Incomplete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for the details.
     */
    INCOMPLETE_LIST_OF_128_BIT_SERVICE_UUIDS(0x06),

    /**
     * Data type is Complete List of 128-bit Service Class UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    COMPLETE_LIST_OF_128_BIT_SERVICE_UUIDS(0x07),

    /**
     * Data type is Shortened Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    SHORTENED_LOCAL_NAME(0x08),

    /**
     * Data type is Complete Local Name, see the Bluetooth Generic Access Profile for more details.
     */
    COMPLETE_LOCAL_NAME(0x09),

    /**
     * Data type is Tx Power Level, see the Bluetooth Generic Access Profile for more details.
     */
    TX_POWER_LEVEL(0x0A),

    /**
     * Data type is Class of Device, see the Bluetooth Generic Access Profile for more details.
     */
    CLASS_OF_DEVICE(0x0D),

    /**
     * Data type is Simple Pairing Hash C, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SIMPLE_PAIRING_HASH_C(0x0E),

    /**
     * Data type is Simple Pairing Randomizer R, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SIMPLE_PAIRING_RANDOMIZER_R(0x0F),

    /**
     * Data type is Device ID, see the Bluetooth Generic Access Profile for more details.
     */
    DEVICE_ID(0x10),

    /**
     * Data type is Security Manager Out of Band Flags, see the Bluetooth Generic Access Profile for
     * more details.
     */
    SECURITY_MANAGER_OUT_OF_BAND_FLAGS(0x11),

    /**
     * Data type is Peripheral Connection Interval Range, see the Bluetooth Generic Access Profile for
     * more details.
     */
    PERIPHERAL_CONNECTION_INTERVAL_RANGE(0x12),

    /**
     * Data type is List of 16-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    LIST_OF_16_BIT_SERVICE_SOLICITATION_UUIDS(0x14),

    /**
     * Data type is List of 128-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    LIST_OF_128_BIT_SERVICE_SOLICITATION_UUIDS(0x15),

    /**
     * Data type is Service Data - 16-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SERVICE_DATA_16_BIT(0x16),

    /**
     * Data type is Public Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    PUBLIC_TARGET_ADDRESS(0x17),

    /**
     * Data type is Random Target Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    RANDOM_TARGET_ADDRESS(0x18),

    /**
     * Data type is Appearance, see the Bluetooth Generic Access Profile for more details.
     */
    APPEARANCE(0x19),

    /**
     * Data type is Advertising Interval, see the Bluetooth Generic Access Profile for more details.
     */
    ADVERTISING_INTERVAL(0x1A),

    /**
     * Data type is LE Bluetooth Device Address, see the Bluetooth Generic Access Profile for more
     * details.
     */
    LE_BLUETOOTH_DEVICE_ADDRESS(0x1B),

    /**
     * Data type is LE Role, see the Bluetooth Generic Access Profile for more details.
     */
    LE_ROLE(0x1C),

    /**
     * Data type is Simple Pairing Hash C-256, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SIMPLE_PAIRING_HASH_C_256(0x1D),

    /**
     * Data type is Simple Pairing Randomizer R-256, see the Bluetooth Generic Access Profile for
     * more details.
     */
    SIMPLE_PAIRING_RANDOMIZER_R_256(0x1E),

    /**
     * Data type is List of 32-bit Service Solicitation UUIDs, see the Bluetooth Generic Access
     * Profile for more details.
     */
    LIST_OF_32_BIT_SERVICE_SOLICITATION_UUIDS(0x1F),

    /**
     * Data type is Service Data - 32-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SERVICE_DATA_32_BIT(0x20),

    /**
     * Data type is Service Data - 128-bit UUID, see the Bluetooth Generic Access Profile for more
     * details.
     */
    SERVICE_DATA_128_BIT(0x21),

    /**
     * Data type is LE Secure Connections Confirmation Value, see the Bluetooth Generic Access
     * Profile for more details.
     */
    LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE(0x22),

    /**
     * Data type is LE Secure Connections Random Value, see the Bluetooth Generic Access Profile for
     * more details.
     */
    LE_SECURE_CONNECTIONS_RANDOM_VALUE(0x23),

    /**
     * Data type is URI, see the Bluetooth Generic Access Profile for more details.
     */
    URI(0x24),

    /**
     * Data type is Indoor Positioning, see the Bluetooth Generic Access Profile for more details.
     */
    INDOOR_POSITIONING(0x25),

    /**
     * Data type is Transport Discovery Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    TRANSPORT_DISCOVERY_DATA(0x26),

    /**
     * Data type is LE Supported Features, see the Bluetooth Generic Access Profile for more
     * details.
     */
    LE_SUPPORTED_FEATURES(0x27),

    /**
     * Data type is Channel Map Update Indication, see the Bluetooth Generic Access Profile for more
     * details.
     */
    CHANNEL_MAP_UPDATE_INDICATION(0x28),

    /**
     * Data type is PB-ADV, see the Bluetooth Generic Access Profile for more details.
     */
    PB_ADV(0x29),

    /**
     * Data type is Mesh Message, see the Bluetooth Generic Access Profile for more details.
     */
    MESH_MESSAGE(0x2A),

    /**
     * Data type is Mesh Beacon, see the Bluetooth Generic Access Profile for more details.
     */
    MESH_BEACON(0x2B),

    /**
     * Data type is BIGInfo, see the Bluetooth Generic Access Profile for more details.
     */
    BIG_INFO(0x2C),

    /**
     * Data type is Broadcast_Code, see the Bluetooth Generic Access Profile for more details.
     */
    BROADCAST_CODE(0x2D),

    /**
     * Data type is Resolvable Set Identifier, see the Bluetooth Generic Access Profile for more
     * details.
     */
    RESOLVABLE_SET_IDENTIFIER(0x2E),

    /**
     * Data type is Advertising Interval - long, see the Bluetooth Generic Access Profile for more
     * details.
     */
    ADVERTISING_INTERVAL_LONG(0x2F),

    /**
     * Data type is Broadcast Name, see the Bluetooth Generic Access Profile for more details.
     */
    BROADCAST_NAME(0x30 ),

    /**
     * Data type is Encrypted Advertising Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    ENCRYPTED_ADVERTISING_DATA(0x31),

    /**
     * Data type is Periodic Advertising Response Timing  Information, see the Bluetooth
     * Generic Access Profile for more details.
     */
    PERIODIC_ADVERTISING_RESPONSE_TIMING_INFORMATION(0x32),

    /**
     * Data type is Electronic Shelf Label, see the Bluetooth Generic Access Profile for more
     * details.
     */
    ELECTRONIC_SHELF_LABEL(0x34),

    /**
     * Data type is 3D Information Data, see the Bluetooth Generic Access Profile for more details.
     */
    TYPE_3D_INFORMATION_DATA(0x3D),

    /**
     * Data type is Manufacturer Specific Data, see the Bluetooth Generic Access Profile for more
     * details.
     */
    MANUFACTURER_SPECIFIC_DATA(0xFF);

    companion object {

        /**
         * Creates [AdvertisingDataType] based on [Int] value.
         *
         * @param type [Int] value.
         * @return [AdvertisingDataType] or null if not found.
         */
        fun createOrNull(type: Int): AdvertisingDataType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}