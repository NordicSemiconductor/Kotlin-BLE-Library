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

package no.nordicsemi.kotlin.ble.advertiser.android

import android.content.Context
import android.os.Build
import no.nordicsemi.kotlin.ble.advertiser.android.internal.legacy.BluetoothLeAdvertiserLegacy
import no.nordicsemi.kotlin.ble.advertiser.android.internal.oreo.BluetoothLeAdvertiserOreo

/**
 * Creates an instance of [BluetoothLeAdvertiser] for Android.
 *
 * The implementation differs based on Android version.
 * Limited functionality is available prior to Android O.
 *
 * @param context An application context.
 * @param forceLegacy If set to true, the legacy implementation will be used on Android O and newer.
 * @return Instance of [BluetoothLeAdvertiser].
 */
@Suppress("unused")
fun BluetoothLeAdvertiser.Factory.native(
    context: Context,
    forceLegacy: Boolean = false
): BluetoothLeAdvertiser = when {
    !forceLegacy && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> BluetoothLeAdvertiserOreo(context)
    else -> BluetoothLeAdvertiserLegacy(context)
}