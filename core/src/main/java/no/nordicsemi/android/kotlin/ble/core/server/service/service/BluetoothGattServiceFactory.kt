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

package no.nordicsemi.android.kotlin.ble.core.server.service.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import android.util.Log
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import java.lang.reflect.Method
import java.util.*

internal object BluetoothGattServiceFactory {

    fun copy(service: BluetoothGattService): BluetoothGattService {
        return BluetoothGattService(service.uuid, service.type).apply {
            service.characteristics.forEach {
                val characteristic = cloneCharacteristic(it)
                addCharacteristic(characteristic)
            }
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun cloneCharacteristic(characteristic: BluetoothGattCharacteristic): BluetoothGattCharacteristic {
        var clone: BluetoothGattCharacteristic
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            // On older versions of android we have to use reflection in order
            // to set the instance ID and the service.
            clone = BluetoothGattCharacteristic(
                characteristic.uuid,
                characteristic.properties,
                characteristic.permissions
            )
            try {
                val initCharacteristic: Method = characteristic.javaClass
                    .getDeclaredMethod(
                        "initCharacteristic",
                        BluetoothGattService::class.java,
                        UUID::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    )
                initCharacteristic.isAccessible = true
                initCharacteristic.invoke(
                    clone,
                    characteristic.service,
                    characteristic.uuid,
                    characteristic.instanceId,
                    characteristic.properties,
                    characteristic.permissions
                )
            } catch (e: Exception) {
                clone = characteristic
            }
        } else {
            // Newer versions of android have this bug fixed as long as a
            // handler is used in connectGatt().
            clone = characteristic
        }
        return clone
    }

    fun create(config: BleServerGattServiceConfig): BluetoothGattService {
        val service = BluetoothGattService(config.uuid, config.type.toNative())

        config.characteristicConfigs.forEach {
            val characteristic = BluetoothGattCharacteristic(
                it.uuid,
                BleGattProperty.toInt(it.properties),
                BleGattPermission.toInt(it.permissions)
            )

            it.descriptorConfigs.forEach {
                val descriptor = BluetoothGattDescriptor(
                    it.uuid,
                    BleGattPermission.toInt(it.permissions)
                )
                characteristic.addDescriptor(descriptor)
            }

            if (it.hasNotifications) {
                val cccd = BluetoothGattDescriptor(
                    BleGattConsts.NOTIFICATION_DESCRIPTOR,
                    BleGattPermission.toInt(listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE))
                )

                cccd.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE

                characteristic.addDescriptor(cccd)
            }

            service.addCharacteristic(characteristic)
        }

        return service
    }
}
