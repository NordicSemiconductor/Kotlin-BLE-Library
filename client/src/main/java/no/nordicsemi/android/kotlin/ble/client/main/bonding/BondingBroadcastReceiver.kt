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

package no.nordicsemi.android.kotlin.ble.client.main.bonding

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.BondStateChanged
import no.nordicsemi.android.kotlin.ble.client.real.ClientBleGattCallback
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.core.data.BondState

/**
 * A broadcast receiver to observe [BluetoothDevice.ACTION_BOND_STATE_CHANGED] events.
 * It contains a list of BLE devices and notifies using ([ClientBleGattCallback])
 * about their bond state changes.
 */
class BondingBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val device: BluetoothDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                ?: return
        } else @Suppress("DEPRECATION") {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
        }
        val address = device.address

        if (action == ACTION_BOND_STATE_CHANGED) {
            val bondState = intent.getIntExtra(EXTRA_BOND_STATE, ERROR)
            callbacks[address]?.onEvent(BondStateChanged(BondState.create(bondState)))
        }
    }

    companion object {

        private val callbacks = mutableMapOf<String, ClientBleGattCallback>()

        private var instance: BondingBroadcastReceiver? = null

        /**
         * Registers new [BondingBroadcastReceiver]. If one instance already exists then it just
         * add new records to observe.
         *
         * @param context An application context.
         * @param device An BLE device which changes should be observed.
         * @param callback Bond state changes callback.
         */
        fun register(context: Context, device: BleDevice, callback: ClientBleGattCallback) {
            callbacks[device.address] = callback
            callback.onEvent(BondStateChanged(device.bondState))

            if (instance == null) {
                instance = BondingBroadcastReceiver()
                ContextCompat.registerReceiver(
                    context.applicationContext,
                    instance,
                    IntentFilter(ACTION_BOND_STATE_CHANGED),
                    ContextCompat.RECEIVER_EXPORTED
                )
            }
        }

        /**
         * Unregisters bond state changes callbacks. If this is the last observed callback then
         * BroadcastReceiver will be unregistered.
         *
         * @param context An application context.
         * @param address An BLE device address which changes should be stopped observing.
         */
        fun unregisterReceiver(context: Context, address: String) {
            callbacks.remove(address)
            if (callbacks.isEmpty()) {
                context.applicationContext.unregisterReceiver(instance)
                instance = null
            }
        }
    }
}
