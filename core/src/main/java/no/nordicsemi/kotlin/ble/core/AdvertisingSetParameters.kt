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

import kotlin.time.Duration

/**
 * This class specifies parameters for Bluetooth LE advertising.
 *
 * @property connectable Whether the advertisement will be connectable.
 * @property discoverable Whether the General Discoverable flag should be added to the
 * scannable or connectable advertisement. By default, a connectable advertisements will be discoverable.
 * Devices connecting to non-discoverable advertisements cannot initiate bonding.
 * This flag is ignored for non-scannable and non-connectable advertisements.
 * @property interval The advertising interval.
 * @property txPowerLevel The requested TX power level for advertising. The actual TX power level
 * may be different, depending on the hardware and implementation.
 */
sealed class AdvertisingSetParameters(
    val connectable: Boolean,
    val discoverable: Boolean,
    val interval: Duration,
    val txPowerLevel: Int,
)

/**
 * The advertising parameters for legacy advertising.
 *
 * The maximum advertising packet length is 31 bytes and data are sent using PHY 1M.
 *
 * @param connectable Whether the advertisement will be connectable and scannable.
 * @param discoverable Whether the General Discoverable flag should be added to the
 * scannable or connectable advertisement. By default, a connectable advertisements will be discoverable.
 * Devices connecting to non-discoverable advertisements cannot initiate bonding.
 * This flag is ignored for non-scannable and non-connectable advertisements.
 * @param interval The advertising interval. Use [AdvertisingInterval] for recommended values.
 * @param txPowerLevel The requested TX power level for advertising. The actual TX power level
 * may be different, depending on the hardware and implementation.
 * Use [TxPowerLevel] for recommended values.
 */
class LegacyAdvertisingSetParameters(
    connectable: Boolean,
    discoverable: Boolean = connectable,
    interval: Duration = AdvertisingInterval.MEDIUM,
    txPowerLevel: Int = TxPowerLevel.MEDIUM,
): AdvertisingSetParameters(
    connectable = connectable,
    discoverable = discoverable,
    interval = interval,
    txPowerLevel = txPowerLevel,
)

/**
 * The advertising parameters for advertising using Advertising Extension from Bluetooth 5.
 *
 * Advertising Extension allows to advertise with longer packets, up to 1650 bytes.
 * The exact number depends on the hardware. It also allows to use PHY LE 2M and PHY Coded
 * for high throughput and long range, respectively.
 *
 * @param connectable Whether the advertisement will be connectable.
 * @param discoverable Whether the _General Discoverable_ flag (in case advertising is started
 * without a timeout) or _Limited Discoverable_ flag (if a timeout is specified) should be added
 * to the scannable or connectable advertisement.
 * By default, a connectable advertisements will be discoverable.
 * Devices connecting to non-discoverable advertisements cannot initiate bonding.
 * This flag is ignored for non-scannable and non-connectable advertisements.
 * @param interval The advertising interval. Use [AdvertisingInterval] for recommended values.
 * @param txPowerLevel The requested TX power level for advertising. The actual TX power level
 * may be different, depending on the hardware and implementation.
 * Use [TxPowerLevel] for recommended values.
 * @property includeTxPowerLevel Should the TX power level be included in the advertisement
 * in the header, outside the AD structure.
 * @property anonymous Whether the advertisement will be anonymous.
 * @property primaryPhy The primary advertising PHY.
 * @property secondaryPhy The secondary advertising PHY.
 * @property scannable Whether the advertisement type should be scannable.
 * Bluetooth 5 advertisements can only be either scannable or only connectable.
 */
class Bluetooth5AdvertisingSetParameters(
    connectable: Boolean,
    discoverable: Boolean = true,
    interval: Duration = AdvertisingInterval.MEDIUM,
    txPowerLevel: Int = TxPowerLevel.MEDIUM,
    val includeTxPowerLevel: Boolean = false,
    val primaryPhy: PrimaryPhy = PrimaryPhy.PHY_LE_1M,
    val secondaryPhy: Phy = Phy.PHY_LE_1M,
    val anonymous: Boolean = false,
    val scannable: Boolean = false,
): AdvertisingSetParameters(
    connectable = connectable,
    discoverable = discoverable,
    txPowerLevel = txPowerLevel,
    interval = interval,
)
