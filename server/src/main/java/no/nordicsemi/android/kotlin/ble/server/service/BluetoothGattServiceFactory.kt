package no.nordicsemi.android.kotlin.ble.server.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
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

                it.descriptors.forEach {
                    val descriptor = BluetoothGattDescriptor(it.uuid, it.permissions)

                    characteristic.addDescriptor(descriptor)
                }
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
