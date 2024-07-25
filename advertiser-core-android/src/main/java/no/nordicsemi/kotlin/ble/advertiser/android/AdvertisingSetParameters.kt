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

package no.nordicsemi.kotlin.ble.advertiser.android

import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.advertiser.GenericBluetoothLeAdvertiser

/**
 * The class provide a way to adjust advertising preferences for each Bluetooth LE
 * advertisement instance.
 *
 * @property txPowerLevel The TX power level for advertising.
 * The actual TX power level used for advertising will be reported using
 * [BluetoothLeAdvertiser.advertise] block.
 * @property interval The advertising interval.
 * @property connectable Whether the advertisement will be connectable.
 * @property legacy Whether the legacy advertisement will be used.
 * @property anonymous Whether the advertisement will be anonymous.
 * @property primaryPhy The primary advertising [Phy].
 * @property secondaryPhy The secondary advertising [Phy].
 * @property scannable Whether the advertisement type should be scannable.
 * Legacy advertisements can be both connectable and scannable. Non-legacy advertisements can
 * be only scannable or only connectable.
 * @property includeTxPowerLevel Whether the TX power level will be included in the advertisement.
 * This can only be included in non-legacy packet and is ignored when [legacy] is true.
 * For legacy advertisements the TX power level can be added to the advertising data
 * using [AdvertisingPayload.AdvertisingData.includeTxPowerLevel].
 * @property discoverable Set whether the General Discoverable flag should be added to the
 * scannable or connectable advertisement. By default, advertisements will be discoverable.
 * Devices connecting to non-discoverable advertisements cannot initiate bonding.
 * This flag is ignored for non-scannable and non-connectable advertisements.
 */
sealed class AdvertisingSetParameters(
    open val legacy: Boolean,

    // Common
    open val connectable: Boolean,
    open val txPowerLevel: TxPowerLevel,
    open val interval: AdvertisingInterval,

    // Available only on Android 8.0+.
    open val anonymous: Boolean = false,
    open val primaryPhy: PrimaryPhy = PrimaryPhy.PHY_LE_1M,
    open val secondaryPhy: Phy = Phy.PHY_LE_1M,
    open val scannable: Boolean = if (legacy) connectable else false,
    open val includeTxPowerLevel: Boolean = false,

    // Available from Android 14+.
    open val discoverable: Boolean = true,
): GenericBluetoothLeAdvertiser.Parameters

/**
 * The advertising parameters for legacy advertising.
 *
 * The maximum advertising packet length is 31 bytes and data are sent using PHY 1M.
 */
data class LegacyAdvertisingSetParameters(
    override val connectable: Boolean,
    override val txPowerLevel: TxPowerLevel = TxPowerLevel.TX_POWER_HIGH,
    override val interval: AdvertisingInterval = AdvertisingInterval.INTERVAL_MEDIUM,
): AdvertisingSetParameters(
    legacy = true,
    connectable = connectable,
    txPowerLevel = txPowerLevel,
    interval = interval,
)

/**
 * The advertising parameters for Bluetooth 5 advertising.
 *
 * These features are available only on devices running Android 8 (Oreo) or newer
 * which support Advertising Extension.
 *
 * Advertising Extension allows to advertise with longer packets, up to 1650 bytes.
 * The exact number depends on the phone and can be checked using
 * [BluetoothLeAdvertiser.getMaximumAdvertisingDataLength]. It also allows to use
 * PHY 2M and PHY Coded for high throughput and long range, respectively.
 */
data class Bluetooth5AdvertisingSetParameters(
    override val connectable: Boolean,
    override val txPowerLevel: TxPowerLevel = TxPowerLevel.TX_POWER_HIGH,
    override val interval: AdvertisingInterval = AdvertisingInterval.INTERVAL_MEDIUM,

    // Available only on Android 8.0+.
    override val anonymous: Boolean = false,
    override val primaryPhy: PrimaryPhy = PrimaryPhy.PHY_LE_1M,
    override val secondaryPhy: Phy = Phy.PHY_LE_1M,
    override val scannable: Boolean = false,
    override val includeTxPowerLevel: Boolean = false,

    // Available from Android 14+.
    override val discoverable: Boolean = true,
): AdvertisingSetParameters(
    legacy = false,
    connectable = connectable,
    txPowerLevel = txPowerLevel,
    interval = interval,
    anonymous = anonymous,
    primaryPhy = primaryPhy,
    secondaryPhy = secondaryPhy,
    scannable = scannable,
    includeTxPowerLevel = includeTxPowerLevel,
    discoverable = discoverable
)
