/*
 * Copyright (c) 2026, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.core.log

import no.nordicsemi.kotlin.log.Log

/**
 * A Bluetooth LE protocol layer.
 *
 * Each layer logs using a separate category, allowing log output to be filtered
 * by the area of the Bluetooth LE stack involved.
 */
enum class Layer : Log.Category {

    /**
     * Physical layer.
     *
     * This layer contains events related to physical communication, including:
     * - PHY updates
     */
    PHY,

    /**
     * Link layer.
     *
     * This layer contains events related to link management, including:
     * - Connection parameter updates
     * - RSSI updates
     */
    LINK,

    /**
     * Generic Access Profile (GAP).
     *
     * This layer contains events related to device discovery and connection
     * management, including:
     * - Advertising
     * - Scanning
     * - Scan results
     * - Establishing connections
     * - Disconnections
     */
    GAP,

    /**
     * Generic Attribute Profile (GATT).
     *
     * This layer contains events related to service discovery and data exchange,
     * including:
     * - Service discovery
     * - Characteristic discovery
     * - Descriptor discovery
     * - Reading attributes
     * - Writing attributes
     * - Notifications
     * - Indications
     * - ATT MTU negotiation
     */
    GATT,

    /**
     * Security Manager Protocol (SMP).
     *
     * This layer contains events related to Bluetooth LE security, including:
     * - Pairing
     * - Bonding
     * - Authentication
     * - Encryption
     * - Key exchange
     * - Security level changes
     */
    SMP,
}