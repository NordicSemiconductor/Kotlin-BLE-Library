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

package no.nordicsemi.android.kotlin.ble.advertiser

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.kotlin.ble.advertiser.callback.BleAdvertisingEvent
import no.nordicsemi.android.kotlin.ble.advertiser.callback.BleAdvertisingSetCallback
import no.nordicsemi.android.kotlin.ble.advertiser.data.toNative
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig

/**
 * Class responsible for starting advertisements on Android API level >= 26.
 *
 * @constructor Creates an instance of an advertiser.
 *
 * @param context An Application context.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal class BleAdvertiserOreo(
    context: Context,
) : BleAdvertiser {

    private val bluetoothManager: BluetoothManager by lazy { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter by lazy { bluetoothManager.adapter }
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy { bluetoothAdapter.bluetoothLeAdvertiser }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT])
    override fun advertise(config: BleAdvertisingConfig): Flow<BleAdvertisingEvent> = callbackFlow {
        val settings = config.settings
        val advertiseData = config.advertiseData
        val scanResponseData = config.scanResponseData

        val callback = BleAdvertisingSetCallback {
            trySend(it)
        }

        bluetoothAdapter.name = config.settings.deviceName

        bluetoothLeAdvertiser.startAdvertisingSet(
            settings.toNative(),
            advertiseData?.toNative(),
            scanResponseData?.toNative(),
            null,
            null,
            callback
        )

        awaitClose {
            bluetoothLeAdvertiser.stopAdvertisingSet(callback)
        }
    }
}
