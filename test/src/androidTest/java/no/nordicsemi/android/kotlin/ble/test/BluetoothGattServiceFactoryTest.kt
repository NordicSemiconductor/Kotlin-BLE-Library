package no.nordicsemi.android.kotlin.ble.test

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServiceFactory
import no.nordicsemi.android.kotlin.ble.test.utils.BlinkySpecifications
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BluetoothGattServiceFactoryTest {

    @Test
    fun whenCloneCharacteristicShouldAlsoCloneDescriptors() {
        val characteristic = BluetoothGattCharacteristic(
            BlinkySpecifications.UUID_BUTTON_CHAR,
            BleGattPermission.toInt(listOf(BleGattPermission.PERMISSION_READ)),
            BleGattProperty.toInt(listOf(BleGattProperty.PROPERTY_NOTIFY)),
        )

        val descriptor = BluetoothGattDescriptor(
            BlinkySpecifications.UUID_LED_CHAR,
            BleGattPermission.toInt(listOf(BleGattPermission.PERMISSION_READ)),
        )

        characteristic.addDescriptor(descriptor)

        val clone = BluetoothGattServiceFactory.cloneCharacteristic(characteristic)

        assertEquals(1, clone.descriptors.size)
    }
}
