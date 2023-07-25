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

/**
 * Properties available on characteristic. It defines available set of actions.
 *
 * @property value Native Android value.
 */
enum class BleGattProperty(internal val value: Int) {

    /**
     * Characteristic property: Characteristic is broadcastable.
     */
    PROPERTY_BROADCAST(1),

    /**
     * Characteristic property: Characteristic has extended properties.
     */
    PROPERTY_EXTENDED_PROPS(128),

    /**
     * Characteristic property: Characteristic supports indication.
     */
    PROPERTY_INDICATE(32),

    /**
     * Characteristic property: Characteristic supports notification.
     */
    PROPERTY_NOTIFY(16),

    /**
     * Characteristic property: Characteristic is readable.
     */
    PROPERTY_READ(2),

    /**
     * Characteristic property: Characteristic supports write with signature.
     */
    PROPERTY_SIGNED_WRITE(64),

    /**
     * Characteristic property: Characteristic can be written.
     */
    PROPERTY_WRITE(8),

    /**
     * Characteristic property: Characteristic can be written without response.
     */
    PROPERTY_WRITE_NO_RESPONSE(4);

    companion object {

        /**
         * Creates all properties encoded in [Int] value.
         *
         * @param properties [Int] value where each property is represented by a separate bit.
         * @return [List] of properties. The list may be empty.
         */
        fun createProperties(properties: Int): List<BleGattProperty> {
            return values().filter { (it.value and properties) > 0 }
        }

        /**
         * Creates a single property from [Int] value.
         *
         * @throws IllegalStateException when properties cannot be decoded.
         *
         * @param value [Int] value of a property.
         * @return Decoded properties.
         */
        fun create(value: Int): BleGattProperty {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalStateException("Cannot create property for value: $value")
        }

        /**
         * Decodes property into single [Int] value.
         *
         * @param properties [List] of properties to be encoded.
         * @return Single [Int] value representing all the properties.
         */
        fun toInt(properties: List<BleGattProperty>): Int {
            return properties.fold(0) { current, next ->
                current or next.value
            }
        }
    }
}
