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

package no.nordicsemi.android.kotlin.ble.server.main.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.core.wrapper.MockBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.MockBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.MockBluetoothGattService
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattService
import java.lang.reflect.Method
import java.util.*

/**
 * Factory class responsible for creating a new instance, a copy of [IBluetoothGattService].
 */
object BluetoothGattServiceFactory {

    /**
     * Copies a service. New instance is needed to separate containing values per connected
     * client device.
     *
     * @param service Service to copy.
     * @return A new service instance.
     */
    fun copy(service: IBluetoothGattService): IBluetoothGattService {
        return when (service) {
            is MockBluetoothGattService -> service.copy()
            is NativeBluetoothGattService -> NativeBluetoothGattService(
                BluetoothGattService(service.uuid, service.type).apply {
                    service.characteristics.forEach {
                        val native = it as NativeBluetoothGattCharacteristic
                        val characteristic = cloneCharacteristic(native.characteristic)
                        addCharacteristic(characteristic)
                    }
                }
            )
        }
    }

    /**
     * Clones characteristics and it's properties using reflection. Reflection is needed to make an
     * exact copy.
     *
     * @param characteristic Native Android [BluetoothGattCharacteristic]
     * @return Copied characteristic or the same reference if copying not needed.
     */
    @SuppressLint("DiscouragedPrivateApi")
    fun cloneCharacteristic(characteristic: BluetoothGattCharacteristic): BluetoothGattCharacteristic {
        var clone: BluetoothGattCharacteristic

        /**
         * There is a new API for writing since Tiramisu and there is no risk of overwriting
         * characteristic's fields by different clients.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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
                characteristic.descriptors.onEach {
                    val descriptor = cloneDescriptor(it)
                    clone.addDescriptor(descriptor)
                }
            } catch (e: Exception) {
                clone = characteristic
            }
        } else {
            // Newer versions of android have this bug fixed as long as a
            // handler is used in connectGatt().
            clone = characteristic
        }
        clone.value = characteristic.value
        return clone
    }

    /**
     * Clones descriptors and it's properties using reflection. Reflection is needed to make an
     * exact copy.
     *
     * @param descriptor Native Android [BluetoothGattDescriptor]
     * @return Copied descriptor or the same reference if copying not needed.
     */
    @SuppressLint("DiscouragedPrivateApi")
    fun cloneDescriptor(descriptor: BluetoothGattDescriptor): BluetoothGattDescriptor {
        var clone: BluetoothGattDescriptor

        /**
         * There is a new API for writing since Tiramisu and there is no risk of overwriting
         * descriptor's fields by different clients.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            clone = BluetoothGattDescriptor(
                descriptor.uuid,
                descriptor.permissions
            )
            try {
                val initCharacteristic: Method = descriptor.javaClass
                    .getDeclaredMethod(
                        "initDescriptor",
                        BluetoothGattCharacteristic::class.java,
                        UUID::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    )
                initCharacteristic.isAccessible = true
                initCharacteristic.invoke(
                    clone,
                    descriptor.characteristic,
                    descriptor.uuid,
                    descriptor.characteristic.instanceId,
                    descriptor.permissions
                )
            } catch (e: Exception) {
                clone = descriptor
            }
        } else {
            // Newer versions of android have this bug fixed as long as a
            // handler is used in connectGatt().
            clone = descriptor
        }
        clone.value = descriptor.value
        return clone
    }

    /**
     * Creates a [MockBluetoothGattService] instance based on configuration provided.
     *
     * @param config A configuration for creating a service.
     * @return An instance of [MockBluetoothGattService].
     */
    fun createMock(config: ServerBleGattServiceConfig): MockBluetoothGattService {
        val characteristics = config.characteristicConfigs.map {
            val characteristic = MockBluetoothGattCharacteristic(
                it.uuid,
                BleGattPermission.toInt(it.permissions),
                BleGattProperty.toInt(it.properties),
                it.initialValue ?: DataByteArray()
            )

            it.descriptorConfigs.forEach {
                val descriptor = MockBluetoothGattDescriptor(
                    it.uuid,
                    BleGattPermission.toInt(it.permissions),
                    characteristic,
                )
                characteristic.addDescriptor(descriptor)
            }

            if (it.hasNotifications) {
                val cccd = MockBluetoothGattDescriptor(
                    BleGattConsts.NOTIFICATION_DESCRIPTOR,
                    BleGattPermission.toInt(listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)),
                    characteristic
                )

                characteristic.addDescriptor(cccd)
            }

            characteristic
        }

        return MockBluetoothGattService(config.uuid, config.type.value, characteristics)
    }

    /**
     * Creates a [NativeBluetoothGattService] instance based on configuration provided.
     *
     * @param config A configuration for creating a service.
     * @return An instance of [NativeBluetoothGattService].
     */
    fun createNative(config: ServerBleGattServiceConfig): NativeBluetoothGattService {
        val service = BluetoothGattService(config.uuid, config.type.value)

        config.characteristicConfigs.forEach {
            val characteristic = BluetoothGattCharacteristic(
                it.uuid,
                BleGattProperty.toInt(it.properties),
                BleGattPermission.toInt(it.permissions)
            )

            characteristic.value = it.initialValue?.value

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

        return NativeBluetoothGattService(service)
    }
}
