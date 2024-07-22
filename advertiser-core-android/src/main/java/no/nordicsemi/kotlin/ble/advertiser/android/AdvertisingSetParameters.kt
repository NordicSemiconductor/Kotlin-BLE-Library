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

package no.nordicsemi.kotlin.ble.advertiser.android

import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.advertiser.BluetoothLeAdvertiser
import org.jetbrains.annotations.Range
import kotlin.time.Duration

/**
 * The class provide a way to adjust advertising preferences for each Bluetooth LE
 * advertisement instance.
 *
 * @property txPowerLevel The TX power level ([TxPowerLevel]) for advertising.
 * @property interval The advertising interval ([AdvertisingInterval]).
 * @property connectable Whether the advertisement will be connectable.
 * @property timeout Advertising time limit. May not exceed 180000 ms on Android 5-7 and
 *                   655350 ms on Android 8+. By default there is no timeout set.
 * @property maxAdvertisingEvents The maximum number of advertising events. A value of 0 means
 *                         no maximum limit.
 * @property legacy Whether the legacy advertisement will be used.
 * @property anonymous Whether the advertisement will be anonymous.
 * @property primaryPhy The primary advertising [Phy].
 * @property secondaryPhy The secondary advertising [Phy].
 * @property scannable Whether the advertisement will be scannable.
 * @property includeTxPower Whether the TX power level will be included in the advertisement.
 *                          This can only be included in Advertising Extension packet.
 * @property discoverable A discoverable device is a non-connectable one, but can receive Scan
 *                        Request packets and reply with a Scan Response packet.
 */
data class AdvertisingSetParameters(
    val txPowerLevel: TxPowerLevel = TxPowerLevel.TX_POWER_HIGH,
    val interval: AdvertisingInterval = AdvertisingInterval.INTERVAL_MEDIUM,
    val connectable: Boolean,
    val timeout: Duration = Duration.INFINITE,
    val maxAdvertisingEvents: @Range(from = 0L, to = 255L) Int = 0,

    // Available only on Android 8.0+.
    val legacy: Boolean = true,
    val anonymous: Boolean = false,
    val primaryPhy: PrimaryPhy = PrimaryPhy.PHY_LE_1M,
    val secondaryPhy: Phy = Phy.PHY_LE_1M,
    val scannable: Boolean = false,
    val includeTxPower: Boolean = false,

    // Available from Android 14+.
    val discoverable: Boolean = true,
): BluetoothLeAdvertiser.Parameters
