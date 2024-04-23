package no.nordicsemi.android.kotlin.ble.test

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.test.utils.BlinkySpecifications
import no.nordicsemi.android.kotlin.ble.test.utils.TestAddressProvider
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WithTimeoutTest {

    private val TAG = "TIMEOUT-TEST"

    private val address = TestAddressProvider.address
    private val address2 = TestAddressProvider.auxiliaryAddress

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    private val service = BlinkySpecifications.UUID_SERVICE_DEVICE
    private val ledCharacteristic = BlinkySpecifications.UUID_LED_CHAR
    private val buttonCharacteristic = BlinkySpecifications.UUID_BUTTON_CHAR
    private val cccd = BlinkySpecifications.NOTIFICATION_DESCRIPTOR

    private val testCount = 10

    @Test
    fun whenReadCharacteristicMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        val services = gatt.discoverServices()
        val services2 = gatt2.discoverServices()

        val char = services.findService(service)?.findCharacteristic(ledCharacteristic)!!
        val char2 = services2.findService(service)?.findCharacteristic(ledCharacteristic)!!

        repeat(testCount) {
            val jobs = listOf(
                launch {
                    Log.d(TAG, "before timeout")
                    withTimeout(timeMillis = 1) {
                        Log.d(TAG, "timeout")
                        val value = char.read()
                        Log.d(TAG, "read value: $value")
                    }
                    Log.d(TAG, "after timeout")
                },
                launch { char2.read() }
            )
            jobs.forEach { it.join() }
        }

        Log.d(TAG, "before timeout")
        val value = char.read()
        Log.d(TAG, "read value: $value")
        Log.d(TAG, "after timeout")
    }
}
