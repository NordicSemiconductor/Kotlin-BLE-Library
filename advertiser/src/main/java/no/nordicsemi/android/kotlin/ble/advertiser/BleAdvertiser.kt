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

package no.nordicsemi.android.kotlin.ble.advertiser

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseData
import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseSettings
import no.nordicsemi.android.kotlin.ble.advertiser.data.toNative
import no.nordicsemi.android.kotlin.ble.advertiser.error.AdvertisementNotStartedException
import no.nordicsemi.android.kotlin.ble.advertiser.error.BleAdvertiseError

class BleAdvertiser(
    context: Context
) {

    private val bluetoothManager: BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner: BluetoothLeScanner
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun advertise(
        settings: BleAdvertiseSettings,
        advertiseData: BleAdvertiseData,
        scanResponseData: BleAdvertiseData
    ) = callbackFlow {

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                trySend(Unit)
            }

            override fun onStartFailure(errorCode: Int) {
                close(AdvertisementNotStartedException(BleAdvertiseError.create(errorCode)))
            }
        }

        bluetoothLeAdvertiser.startAdvertising(settings.toNative(), advertiseData.toNative(), scanResponseData.toNative(), callback)

        awaitClose {
            bluetoothLeAdvertiser.stopAdvertising(callback)
        }
    }
}
