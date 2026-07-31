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

package no.nordicsemi.kotlin.ble.client.android.internal

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattConnectionSettings
import android.content.Context
import android.os.Build
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import java.util.concurrent.Executor

@Suppress("DEPRECATION")
internal fun BluetoothDevice.connect(
    context: Context,
    autoConnect: Boolean,
    autoMtu: Boolean,
    opportunistic: Boolean,
    callback: BluetoothGattCallback,
    preferredPhy: List<PrimaryPhy> = listOf(PrimaryPhy.PHY_LE_1M),
): BluetoothGatt =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        val settings = BluetoothGattConnectionSettings.Builder()
            .setAutoConnectEnabled(autoConnect)
            .setAutomaticMtuEnabled(autoMtu)
            .setOpportunisticEnabled(opportunistic)
            .setTransport(BluetoothDevice.TRANSPORT_LE)
            .build()
        // Note: PHY is again ignored from this version.
        //       The deprecated methods from below use the API above, and ignore it as well.
        val executor = Executor(Runnable::run)
        connectGatt(settings, executor, callback)!!
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE, preferredPhy.toMask())
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE)
    } else {
        connectGatt(context, autoConnect, callback)
    }