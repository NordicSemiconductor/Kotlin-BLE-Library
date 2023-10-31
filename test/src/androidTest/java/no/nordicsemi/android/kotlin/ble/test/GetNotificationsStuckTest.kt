package no.nordicsemi.android.kotlin.ble.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.test.utils.BlinkySpecifications
import no.nordicsemi.android.kotlin.ble.test.utils.TestAddressProvider
import org.junit.Test
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetNotificationsStuckTest {

    val service = BlinkySpecifications.UUID_SERVICE_DEVICE
    val char = BlinkySpecifications.UUID_BUTTON_CHAR

    private val address = TestAddressProvider.address

    @Test
    fun whenGetNotificationsAfterDisconnectShouldThrow() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address)
        val services = gatt.discoverServices()
        val not = services.findService(service)?.findCharacteristic(char)!!
        gatt.disconnect()           // Simulate a device disconnection
        Assert.assertThrows(DeviceDisconnectedException::class.java) {
            runBlocking {
                not.getNotifications()     //Issue: stuck here forever
            }
        }
    }
}
