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

package no.nordicsemi.kotlin.ble.advertiser.android.internal.mapper

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import no.nordicsemi.kotlin.ble.advertiser.exception.AdvertisingNotStartedException
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingInterval
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingPayload
import no.nordicsemi.kotlin.ble.advertiser.android.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.advertiser.android.TxPowerLevel
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.seconds
import android.bluetooth.le.AdvertiseData as NativeAdvertiseData
import android.bluetooth.le.AdvertiseSettings as NativeAdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters as NativeAdvertisingSetParameters

@RequiresApi(Build.VERSION_CODES.O)
internal fun AdvertisingSetParameters.toNative(): NativeAdvertisingSetParameters {
    return NativeAdvertisingSetParameters.Builder()
        .apply {
            setLegacyMode(legacy)
            setAnonymous(!legacy && anonymous)
            setConnectable(connectable && (legacy || !anonymous))
            // Legacy advertisements can be both connectable and scannable,
            // but cannot be connectable and non-scannable.
            setScannable(if (legacy) connectable else scannable && !connectable && !anonymous)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setDiscoverable(discoverable)
            }
            setInterval(interval.toNative())
            setIncludeTxPower(if (legacy) false else includeTxPowerLevel)
            setTxPowerLevel(txPowerLevel.toNative())
            setPrimaryPhy(when (primaryPhy) {
                PrimaryPhy.PHY_LE_1M -> BluetoothDevice.PHY_LE_1M
                PrimaryPhy.PHY_LE_CODED -> BluetoothDevice.PHY_LE_CODED
            })
            setSecondaryPhy(when (secondaryPhy) {
                Phy.PHY_LE_1M -> BluetoothDevice.PHY_LE_1M
                Phy.PHY_LE_2M -> BluetoothDevice.PHY_LE_2M
                Phy.PHY_LE_CODED -> BluetoothDevice.PHY_LE_CODED
            })
        }
        .build()
}

internal fun AdvertisingSetParameters.toLegacy(
    timeout: Duration,
): NativeAdvertiseSettings {
    return NativeAdvertiseSettings.Builder()
        .apply {
            setConnectable(connectable)
            setAdvertiseMode(interval.toLegacy())
            setTxPowerLevel(txPowerLevel.toLegacy())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setDiscoverable(discoverable)
            }
            // Maximum timeout for legacy advertising is 180 seconds.
            val legacyTimeout = when (timeout) {
                INFINITE -> Duration.ZERO
                else -> timeout.coerceAtMost(180.seconds)
            }
            setTimeout(legacyTimeout.inWholeMilliseconds.toInt())
        }
        .build()
}

internal fun AdvertisingPayload.AdvertisingData.toNative(): NativeAdvertiseData {
    return NativeAdvertiseData.Builder().apply {
        setIncludeDeviceName(includeDeviceName)
        setIncludeTxPowerLevel(includeTxPowerLevel)
        serviceUuids?.forEach { addServiceUuid(ParcelUuid(it)) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            serviceSolicitationUuids?.forEach { addServiceSolicitationUuid(ParcelUuid(it)) }
        }
        serviceData?.forEach {
            addServiceData(ParcelUuid(it.key), it.value)
        }
        manufacturerData?.forEach {
            addManufacturerData(it.key, it.value)
        }
    }.build()
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun AdvertisingInterval.toNative(): Int {
    return when (this) {
        AdvertisingInterval.INTERVAL_LOW    -> NativeAdvertisingSetParameters.INTERVAL_LOW
        AdvertisingInterval.INTERVAL_MEDIUM -> NativeAdvertisingSetParameters.INTERVAL_MEDIUM
        AdvertisingInterval.INTERVAL_HIGH   -> NativeAdvertisingSetParameters.INTERVAL_HIGH
    }
}

internal fun AdvertisingInterval.toLegacy(): Int {
    return when (this) {
        AdvertisingInterval.INTERVAL_LOW    -> NativeAdvertiseSettings.ADVERTISE_MODE_LOW_POWER
        AdvertisingInterval.INTERVAL_MEDIUM -> NativeAdvertiseSettings.ADVERTISE_MODE_BALANCED
        AdvertisingInterval.INTERVAL_HIGH   -> NativeAdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun TxPowerLevel.toNative(): Int {
    return when (this) {
        TxPowerLevel.TX_POWER_ULTRA_LOW -> NativeAdvertisingSetParameters.TX_POWER_ULTRA_LOW
        TxPowerLevel.TX_POWER_LOW       -> NativeAdvertisingSetParameters.TX_POWER_LOW
        TxPowerLevel.TX_POWER_MEDIUM    -> NativeAdvertisingSetParameters.TX_POWER_MEDIUM
        TxPowerLevel.TX_POWER_HIGH      -> NativeAdvertisingSetParameters.TX_POWER_HIGH
    }
}

fun TxPowerLevel.toLegacy(): Int {
    return when (this) {
        TxPowerLevel.TX_POWER_ULTRA_LOW -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
        TxPowerLevel.TX_POWER_LOW       -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_LOW
        TxPowerLevel.TX_POWER_MEDIUM    -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
        TxPowerLevel.TX_POWER_HIGH      -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_HIGH
    }
}

internal fun Int.toReason(): AdvertisingNotStartedException.Reason = when (this) {
    AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> AdvertisingNotStartedException.Reason.DATA_TOO_LARGE
    AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> AdvertisingNotStartedException.Reason.TOO_MANY_ADVERTISERS
    AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> AdvertisingNotStartedException.Reason.ALREADY_STARTED
    AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> AdvertisingNotStartedException.Reason.INTERNAL_ERROR
    AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> AdvertisingNotStartedException.Reason.FEATURE_UNSUPPORTED
    // Error 18 = Invalid Advertising data (for example: Adv Ext not supported (nexus 6P with 8.0.0), AE connectable packet has scan response data)
    18 /* HCI_ERR_ILLEGAL_PARAMETER_FMT */ -> AdvertisingNotStartedException.Reason.ILLEGAL_PARAMETERS
    else -> AdvertisingNotStartedException.Reason.UNKNOWN
}
