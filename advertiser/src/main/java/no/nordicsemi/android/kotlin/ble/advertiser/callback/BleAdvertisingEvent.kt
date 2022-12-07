/*
 * Copyright (c) 2022, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.advertiser.callback

import android.bluetooth.le.AdvertisingSet

sealed interface BleAdvertisingEvent

data class OnAdvertisingDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnAdvertisingEnabled(
    val advertisingSet: AdvertisingSet,
    val enable: Boolean,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnAdvertisingParametersUpdated(
    val advertisingSet: AdvertisingSet,
    val txPower: Int,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnAdvertisingSetStarted(
    /**
     * Returns null for legacy advertisement for Android < Oreo.
     */
    val advertisingSet: AdvertisingSet?,
    val txPower: Int,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnAdvertisingSetStopped(val advertisingSet: AdvertisingSet) : BleAdvertisingEvent

data class OnPeriodicAdvertisingDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnPeriodicAdvertisingEnabled(
    val advertisingSet: AdvertisingSet,
    val enable: Boolean,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnPeriodicAdvertisingParametersUpdated(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent

data class OnScanResponseDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertiseStatus
) : BleAdvertisingEvent
