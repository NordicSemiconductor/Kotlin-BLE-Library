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
import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.Bluetooth5AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.LegacyAdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.android.AdvertisingDataDefinition
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import android.bluetooth.le.AdvertiseData as NativeAdvertiseData
import android.bluetooth.le.AdvertiseSettings as NativeAdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters as NativeAdvertisingSetParameters

@RequiresApi(Build.VERSION_CODES.O)
internal fun AdvertisingSetParameters.toNative(): NativeAdvertisingSetParameters = when (this) {
    is LegacyAdvertisingSetParameters -> {
        NativeAdvertisingSetParameters.Builder()
            .setLegacyMode(true)
            // Legacy advertisements must be both connectable and scannable or neither.
            .setConnectable(connectable).setScannable(connectable)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setDiscoverable(discoverable)
                }
            }
            .setInterval(interval.toNativeInterval())
            .setTxPowerLevel(txPowerLevel.coerceIn(-127..1))
            .build()
    }
    is Bluetooth5AdvertisingSetParameters -> {
        NativeAdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setAnonymous(anonymous)
            .setConnectable(connectable && !anonymous)
            // Non-legacy advertisement cannot be both connectable and scannable or anonymous.
            .setScannable(scannable && !connectable && !anonymous)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setDiscoverable(discoverable)
                }
            }
            .setInterval(interval.toNativeInterval())
            .setTxPowerLevel(txPowerLevel.coerceIn(-127..1))
            .setIncludeTxPower(includeTxPowerLevel)
            .setPrimaryPhy(when (primaryPhy) {
                PrimaryPhy.PHY_LE_1M -> BluetoothDevice.PHY_LE_1M
                PrimaryPhy.PHY_LE_CODED -> BluetoothDevice.PHY_LE_CODED
            })
            .setSecondaryPhy(when (secondaryPhy) {
                Phy.PHY_LE_1M -> BluetoothDevice.PHY_LE_1M
                Phy.PHY_LE_2M -> BluetoothDevice.PHY_LE_2M
                Phy.PHY_LE_CODED -> BluetoothDevice.PHY_LE_CODED
            })
            .build()
    }
}

internal fun AdvertisingSetParameters.toLegacy(timeout: Duration): NativeAdvertiseSettings =
    NativeAdvertiseSettings.Builder()
        .setConnectable(connectable)
        .setAdvertiseMode(interval.toLegacyInterval())
        .setTxPowerLevel(txPowerLevel.toLegacyTxPowerLevel())
        .apply {
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

@OptIn(ExperimentalUuidApi::class)
internal fun AdvertisingDataDefinition.toNative(): NativeAdvertiseData =
    NativeAdvertiseData.Builder()
        .setIncludeDeviceName(includeDeviceName)
        .setIncludeTxPowerLevel(includeTxPowerLevel)
        .apply {
            serviceUuids?.forEach { addServiceUuid(ParcelUuid(it.toJavaUUID)) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                serviceSolicitationUuids?.forEach { addServiceSolicitationUuid(ParcelUuid(it.toJavaUUID)) }
            }
            serviceData?.forEach {
                addServiceData(ParcelUuid(it.key.toJavaUUID), it.value)
            }
            manufacturerData?.forEach {
                addManufacturerData(it.key, it.value)
            }
        }
        .build()

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

private fun Duration.toNativeInterval(): Int =
    (inWholeMilliseconds.coerceAtMost(100) / 0.625f).toInt()

private fun Duration.toLegacyInterval(): Int = when (this) {
    in Duration.ZERO..100.milliseconds -> NativeAdvertiseSettings.ADVERTISE_MODE_LOW_POWER
    in 100.milliseconds..250.milliseconds -> NativeAdvertiseSettings.ADVERTISE_MODE_BALANCED
    else -> NativeAdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
}

private fun Int.toLegacyTxPowerLevel(): Int = when (this) {
    in Int.MIN_VALUE..-20 -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
    in -20..-15 -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_LOW
    in -15..-7 -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
    else -> NativeAdvertiseSettings.ADVERTISE_TX_POWER_HIGH
}

@OptIn(ExperimentalUuidApi::class)
private val Uuid.toJavaUUID: UUID
    get() = toLongs { msb, lsb ->  UUID(msb, lsb) }