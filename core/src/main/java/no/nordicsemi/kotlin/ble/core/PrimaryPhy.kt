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
 * Primary PHY for an advertisement and establishing connection.
 *
 * The primary PHY of an advertisement can only be LE 1M, for regular advertisement,
 * or LE Coded for long range applications. Devices can switch to other PHY during connection.
 * @see [Phy]
 */
enum class PrimaryPhy {

    /**
     * Bluetooth LE 1M PHY.
     *
     * Default Physical Channel for advertising, scanning or connection.
     */
    PHY_LE_1M,

    /**
     * Bluetooth LE Coded PHY.
     *
     * Coded PHY is used for long range applications, where each bit is encoded as 2 or 8 symbols,
     * sacrificing speed over error correction.
     */
    PHY_LE_CODED;

    override fun toString(): String = when (this) {
        PHY_LE_1M -> "LE 1M"
        PHY_LE_CODED -> "LE Coded"
    }
}

infix fun PrimaryPhy.and(phy: PrimaryPhy): List<PrimaryPhy> = listOf(this, phy)

infix fun List<PrimaryPhy>.and(phy: PrimaryPhy): List<PrimaryPhy> = this + phy

@Suppress("UnusedReceiverParameter")
val PrimaryPhy.ANY: List<PrimaryPhy>
    get() = PrimaryPhy.entries
