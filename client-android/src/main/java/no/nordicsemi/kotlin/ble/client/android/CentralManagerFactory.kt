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

package no.nordicsemi.kotlin.ble.client.android

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.android.internal.NativeCentralManagerImpl
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment

/**
 * Creates a [CentralManager] implementation which is using native Android API to
 * scan and connect to physical Bluetooth LE devices.
 *
 * ### Important
 * Remember to call [NativeAndroidEnvironment.close] when the environment is no longer needed.
 * Each [NativeAndroidEnvironment] registers a [android.content.BroadcastReceiver] to be notified
 * about Bluetooth state changes. Closing it will unregister the receiver.
 *
 * @param scope The coroutine scope.
 * @param environment Native Android environment object.
 */
fun CentralManager.Factory.native(
    scope: CoroutineScope,
    environment: NativeAndroidEnvironment,
): CentralManager =
    NativeCentralManagerImpl(scope, environment)

/**
 * Creates a [CentralManager] implementation which is using native Android API to
 * scan and connect to physical Bluetooth LE devices.
 *
 * This method creates an instance of [NativeAndroidEnvironment] which gets closed
 * automatically when the [scope] is cancelled. If an environment instance is needed for multiple
 * [CentralManager]s, use the other factory method.
 *
 * @param context Android context, needed to connect to peripherals and listen to system events.
 * @param scope The coroutine scope.
 */
fun CentralManager.Factory.native(
    context: Context,
    scope: CoroutineScope,
): CentralManager {
    val env = NativeAndroidEnvironment(context)
    scope.launch {
        try {
            awaitCancellation()
        } finally {
            env.close()
        }
    }
    return NativeCentralManagerImpl(scope, env)
}