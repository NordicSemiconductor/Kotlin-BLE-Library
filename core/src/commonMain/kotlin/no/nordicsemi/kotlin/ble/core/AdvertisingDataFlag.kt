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

package no.nordicsemi.kotlin.ble.core;

import kotlin.experimental.or

/**
 * An enum with AD Flag values.
 *
 * @property value The flag bit mask.
 */
enum class AdvertisingDataFlag(val mask: Byte) {
    LE_LIMITED_DISCOVERABLE_MODE(0b00000001),
    LE_GENERAL_DISCOVERABLE_MODE(0b00000010),
    BR_EDR_NOT_SUPPORTED(0b00000100),
    SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_CONTROLLER(0b00001000),
    @Deprecated("Deprecated in Bluetooth 6")
    SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_HOST(0b00010000);

    @Suppress("DEPRECATION")
    override fun toString(): String = when (this) {
        LE_LIMITED_DISCOVERABLE_MODE -> "LE Limited Discoverable Mode"
        LE_GENERAL_DISCOVERABLE_MODE -> "LE General Discoverable Mode"
        BR_EDR_NOT_SUPPORTED -> "BR/EDR Not Supported"
        SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_CONTROLLER -> "Simultaneous LE and BR/EDR to Same Device Capable (Controller)"
        SIMULTANEOUS_LE_BR_EDR_TO_SAME_DEVICE_CAPABLE_HOST -> "Simultaneous LE and BR/EDR to Same Device Capable (Host)"
    }
}

/**
 * Returns the value of the flags as a bitfield.
 */
val Set<AdvertisingDataFlag>.value: Byte
    get() = fold(0) { acc, flag -> acc or flag.mask }

/**
 * Parses the AD type value as flags.
 */
fun Int.asFlags(): Set<AdvertisingDataFlag> =
    AdvertisingDataFlag.entries.filter { (this and it.mask.toInt()) != 0 }.toSet()

/**
 * Parses the AD type value as flags.
 */
fun Byte.asFlags(): Set<AdvertisingDataFlag> = toInt().asFlags()