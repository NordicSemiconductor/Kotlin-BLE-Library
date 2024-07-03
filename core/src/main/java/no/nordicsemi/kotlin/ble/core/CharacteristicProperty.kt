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
 * Enumeration of properties of GATT characteristics.
 *
 * A property defines available actions for a given characteristic.
 */
enum class CharacteristicProperty {
    /**
     * A property that indicates the characteristic can broadcast its value using a characteristic
     * configuration descriptor.
     */
    BROADCAST,
    /** A property that indicates a peripheral can read the characteristic’s value. */
    READ,
    /**
     * A property that indicates a peripheral can write the characteristic’s value,
     * without a response to indicate that the write succeeded.
     */
    WRITE_WITHOUT_RESPONSE,
    /**
     * A property that indicates a peripheral can write the characteristic’s value,
     * with a response to indicate that the write succeeded.
     */
    WRITE,
    /**
     * A property that indicates the peripheral permits notifications of the characteristic’s value,
     * without a response from the central to indicate receipt of the notification.
     */
    NOTIFY,
    /**
     * A property that indicates the peripheral permits notifications of the characteristic’s value,
     * with a response from the central to indicate receipt of the notification.
     */
    INDICATE,
    /**
     * A property that indicates the peripheral allows signed writes of the characteristic’s value,
     * without a response to indicate the write succeeded.
     */
    SIGNED_WRITE,
    /**
     * A property that indicates the characteristic defines additional properties in the extended
     * properties descriptor.
     */
    EXTENDED_PROPERTIES;
    /**
     * A property that indicates the peripheral allows reliable writes of the characteristic’s value.
     *
     * Once reliable write, also known as queued write, is started, all write requests
     * are sent to the remote device for verification and queued up for atomic execution.
     * The application is responsible for verifying whether the value has been transmitted
     * accurately. After all characteristics have been queued up and verified, they may be
     * executed. If a characteristic was not written correctly, aborting reliable write will
     * cancel the current transaction without committing any values on the remote LE device.
     */
    // RELIABLE_WRITE,
    /**
     * A property that indicates the Characteristic User Description descriptor is writable.
     */
    // WRITEABLE_AUXILIARIES,

    /**
     * The write type associated with this property, or null if the property does not
     * relates to writing.
     */
    val writeType: WriteType?
        get() = when (this) {
            WRITE -> WriteType.WITH_RESPONSE
            WRITE_WITHOUT_RESPONSE -> WriteType.WITHOUT_RESPONSE
            SIGNED_WRITE -> WriteType.SIGNED
            else -> null
        }
}

infix fun CharacteristicProperty.and(property: CharacteristicProperty): List<CharacteristicProperty> {
    return listOf(this, property)
}

infix fun List<CharacteristicProperty>.and(property: CharacteristicProperty): List<CharacteristicProperty> {
    return this + property
}

/**
 * Returns the default write type for the given list of properties.
 *
 * The default write type is the first write type found in the list, or [WriteType.WITH_RESPONSE]
 * if the list contains [CharacteristicProperty.WRITE].
 */
val List<CharacteristicProperty>.defaultWriteType: WriteType?
    get() = if (CharacteristicProperty.WRITE in this) WriteType.WITH_RESPONSE else firstNotNullOfOrNull { it.writeType }


