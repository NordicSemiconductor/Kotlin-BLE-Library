/*
 * Copyright (c) 2025, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.client.mock

/**
 * Represents the type of Bluetooth address.
 *
 * Every Bluetooth LE device is identified by a unique 48-bit address. Bluetooth addresses are
 * categorized as either public or random. Random addresses are further classified as either
 * static or private, depending on whether they change or not. And lastly, private addresses
 * are either resolvable or non-resolvable. The image below shows how Bluetooth addresses are
 * categorized.
 *
 * Note that random and private addresses are merely classification types and not actual address types.
 *
 * Learn more at [DevAcademy](https://academy.nordicsemi.com/courses/bluetooth-low-energy-fundamentals/lessons/lesson-2-bluetooth-le-advertising/topic/bluetooth-address).
 */
enum class AddressType {
    /**
     * A public address is a fixed address that is programmed into the device at the manufacturer.
     *
     * It must be registered with the IEEE registration authority, and it’s globally unique to
     * that device and cannot change during the lifetime of the device.
     *
     * There is a fee associated with obtaining this type of address.
     */
    PUBLIC,

    /**
     * A random static address.
     *
     * A random static address can be allocated and fixed throughout the lifetime of the device.
     * It can be altered at bootup, but not during runtime. This is a low-cost alternative to a
     * public address because you don’t need to register it.
     */
    RANDOM_STATIC,

    /**
     * A Resolvable Private Address (RPA) is resolvable as intended listeners have a pre-shared key
     * by which they can figure out the new address every time it changes.
     *
     * The pre-shared key is the Identity Resolving Key (IRK) and is used both to generate and to
     * resolve the random address.
     *
     * The random address is basically just used by the peer to be able to resolve the actual
     * address of the Bluetooth LE device, which is still either the public or the random
     * static address. The IRK allows the peer to translate the random private address into the
     * device’s real Bluetooth LE address.
     */
    RANDOM_PRIVATE_RESOLVABLE,

    /**
     * A non-resolvable private address is not resolvable by other devices and is only intended
     * as a way to prevent tracking. This type of address is not commonly used.
     */
    RANDOM_PRIVATE_NON_RESOLVABLE,
}