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

package no.nordicsemi.android.kotlin.ble.advertiser.callback

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Bluetooth LE advertising set callbacks, used to deliver advertising operation status.
 * It maps native Android callback [AdvertisingSetCallback] into data class events
 * [BleAdvertisingEvent].
 *
 * @property onEvent Callback class for the event.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal class BleAdvertisingSetCallback(
    private val onEvent: (BleAdvertisingEvent) -> Unit
) : AdvertisingSetCallback() {

    /**
     * Callback responsible for emitting event [OnAdvertisingDataSet].
     */
    override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet?, status: Int) {
        onEvent(OnAdvertisingDataSet(advertisingSet!!, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnAdvertisingEnabled].
     */
    override fun onAdvertisingEnabled(advertisingSet: AdvertisingSet?, enable: Boolean, status: Int) {
        onEvent(OnAdvertisingEnabled(advertisingSet!!, enable, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnAdvertisingParametersUpdated].
     */
    override fun onAdvertisingParametersUpdated(advertisingSet: AdvertisingSet?, txPower: Int, status: Int) {
        onEvent(OnAdvertisingParametersUpdated(advertisingSet!!, txPower, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnAdvertisingSetStarted].
     */
    override fun onAdvertisingSetStarted(advertisingSet: AdvertisingSet?, txPower: Int, status: Int) {
        onEvent(OnAdvertisingSetStarted(advertisingSet, txPower, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnAdvertisingSetStopped].
     */
    override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
        onEvent(OnAdvertisingSetStopped(advertisingSet))
    }

    /**
     * Callback responsible for emitting event [OnPeriodicAdvertisingDataSet].
     */
    override fun onPeriodicAdvertisingDataSet(advertisingSet: AdvertisingSet?, status: Int) {
        onEvent(OnPeriodicAdvertisingDataSet(advertisingSet!!, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnPeriodicAdvertisingEnabled].
     */
    override fun onPeriodicAdvertisingEnabled(advertisingSet: AdvertisingSet?, enable: Boolean, status: Int) {
        onEvent(OnPeriodicAdvertisingEnabled(advertisingSet!!, enable, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnPeriodicAdvertisingParametersUpdated].
     */
    override fun onPeriodicAdvertisingParametersUpdated(advertisingSet: AdvertisingSet?, status: Int) {
        onEvent(OnPeriodicAdvertisingParametersUpdated(advertisingSet!!, BleAdvertisingStatus.create(status)))
    }

    /**
     * Callback responsible for emitting event [OnScanResponseDataSet].
     */
    override fun onScanResponseDataSet(advertisingSet: AdvertisingSet?, status: Int) {
        onEvent(OnScanResponseDataSet(advertisingSet!!, BleAdvertisingStatus.create(status)))
    }
}
