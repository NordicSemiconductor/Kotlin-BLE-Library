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
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.AdvertisingSetCallback

/**
 * Event class which maps [AdvertisingSetCallback] methods into data classes. On Android before O
 * some of those values are emulated.
 *
 * @see [AdvertisingSetCallback](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback)
 */
sealed interface BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setAdvertisingData] indicating result of the
 * operation. If status is [BleAdvertisingStatus.ADVERTISE_SUCCESS], then data was changed.
 *
 * @property advertisingSet The advertising set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onAdvertisingDataSet](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onAdvertisingDataSet(android.bluetooth.le.AdvertisingSet,%20int)
 */
data class OnAdvertisingDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [BluetoothLeAdvertiser.startAdvertisingSet] indicating result of
 * the operation. If status is [BleAdvertisingStatus.ADVERTISE_SUCCESS], then advertising set is
 * advertising.
 *
 * @property advertisingSet The advertising set.
 * @property enable Is advertising enabled.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onAdvertisingEnabled](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onAdvertisingEnabled(android.bluetooth.le.AdvertisingSet,%20boolean,%20int))
 */
data class OnAdvertisingEnabled(
    val advertisingSet: AdvertisingSet,
    val enable: Boolean,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setAdvertisingParameters] indicating result of
 * the operation.
 *
 * @property advertisingSet The advertising set.
 * @property txPower Transmitter power that will be used for this set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onAdvertisingParametersUpdated](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onAdvertisingParametersUpdated(android.bluetooth.le.AdvertisingSet,%20int,%20int))
 */
data class OnAdvertisingParametersUpdated(
    val advertisingSet: AdvertisingSet,
    val txPower: Int,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [BluetoothLeAdvertiser.startAdvertisingSet] indicating result of
 * the operation. If status is [BleAdvertisingStatus.ADVERTISE_SUCCESS], then advertisingSet contains
 * the started set and it is advertising. If error occurred, advertisingSet is null, and status will
 * be set to proper error code.
 *
 * @property advertisingSet The advertising set. Null on Android < Oreo.
 * @property txPower Transmitter power that will be used for this set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onAdvertisingSetStarted](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onAdvertisingSetStarted(android.bluetooth.le.AdvertisingSet,%20int,%20int))
 */
data class OnAdvertisingSetStarted(
    val advertisingSet: AdvertisingSet?,
    val txPower: Int,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [BluetoothLeAdvertiser.stopAdvertisingSet] indicating advertising
 * set is stopped.
 *
 * @property advertisingSet The advertising set.
 *
 * @see [AdvertisingSetCallback.onAdvertisingSetStopped](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onAdvertisingSetStopped(android.bluetooth.le.AdvertisingSet))
 */
data class OnAdvertisingSetStopped(val advertisingSet: AdvertisingSet?) : BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setPeriodicAdvertisingData] indicating result of
 * the operation.
 *
 * @property advertisingSet The advertising set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onPeriodicAdvertisingDataSet](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onPeriodicAdvertisingDataSet(android.bluetooth.le.AdvertisingSet,%20int))
 */
data class OnPeriodicAdvertisingDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setPeriodicAdvertisingEnabled] indicating result
 * of the operation.
 *
 * @property advertisingSet The advertising set.
 * @property enable Is advertising enabled.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onPeriodicAdvertisingEnabled](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onPeriodicAdvertisingEnabled(android.bluetooth.le.AdvertisingSet,%20boolean,%20int))
 */
data class OnPeriodicAdvertisingEnabled(
    val advertisingSet: AdvertisingSet,
    val enable: Boolean,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setPeriodicAdvertisingParameters] indicating
 * result of the operation.
 *
 * @property advertisingSet The advertising set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onPeriodicAdvertisingParametersUpdated](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onPeriodicAdvertisingParametersUpdated(android.bluetooth.le.AdvertisingSet,%20int))
 */
data class OnPeriodicAdvertisingParametersUpdated(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent

/**
 * Event emitted in response to [AdvertisingSet.setAdvertisingData] indicating result of the
 * operation.
 *
 * @property advertisingSet The advertising set.
 * @property status Status of the operation.
 *
 * @see [AdvertisingSetCallback.onScanResponseDataSet](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetCallback#onScanResponseDataSet(android.bluetooth.le.AdvertisingSet,%20int))
 */
data class OnScanResponseDataSet(
    val advertisingSet: AdvertisingSet,
    val status: BleAdvertisingStatus
) : BleAdvertisingEvent
