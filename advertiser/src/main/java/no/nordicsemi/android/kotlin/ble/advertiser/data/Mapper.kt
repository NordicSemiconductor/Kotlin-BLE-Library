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

package no.nordicsemi.android.kotlin.ble.advertiser.data

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
import android.os.Build
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingData
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingSettings

@RequiresApi(Build.VERSION_CODES.O)
internal fun BleAdvertisingSettings.toNative(): AdvertisingSetParameters {
    return AdvertisingSetParameters.Builder().apply {
        anonymous?.let { setAnonymous(it) }
        txPowerLevel?.toNative()?.let { setTxPowerLevel(it) }
        interval?.toNative()?.let { setInterval(it) }
        setConnectable(connectable)
        includeTxPower?.let { setIncludeTxPower(it) }
        setLegacyMode(legacyMode)
        primaryPhy?.value?.let { setPrimaryPhy(it) }
        scannable?.let { setScannable(it) }
        secondaryPhy?.value?.let { setSecondaryPhy(it) }
    }.build()
}

internal fun BleAdvertisingSettings.toLegacy(): AdvertiseSettings {
    return AdvertiseSettings.Builder().apply {
        txPowerLevel?.toLegacy()?.let { setTxPowerLevel(it) }
        interval?.toLegacy()?.let { setAdvertiseMode(it) }
        setTimeout(timeout)
        setConnectable(connectable)
    }.build()
}

internal fun BleAdvertisingData.toNative(): AdvertiseData {
    val builder = AdvertiseData.Builder()
    serviceUuid?.let { builder.addServiceUuid(it) }
    includeDeviceName?.let { builder.setIncludeDeviceName(it) }
    includeTxPowerLever?.let { builder.setIncludeTxPowerLevel(it) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        serviceSolicitationUuid?.let { builder.addServiceSolicitationUuid(it) }
    }
    serviceData.forEach {
        builder.addServiceData(it.uuid, it.data.value)
    }
    manufacturerData.forEach {
        builder.addManufacturerData(it.id, it.data.value)
    }
    return builder.build()
}
