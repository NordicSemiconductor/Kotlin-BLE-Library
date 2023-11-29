package no.nordicsemi.android.kotlin.ble.test

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.test.utils.TestAddressProvider
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimultaneousRssiStuckTest {

    private val address = TestAddressProvider.address
    private val address2 = TestAddressProvider.auxiliaryAddress

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    private val repeat = 5

    @Test
    fun whenReadRssiWithoutMutexShouldWork() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)
        val mutex = Mutex()

        // This one passes when using a mutex
        repeat(repeat) {
            val jobs = listOf(
                launch { mutex.withLock { gatt.readRssi() } },
                launch { mutex.withLock { gatt2.readRssi() } }
            )
            jobs.forEach { it.join() }
        }

        //Issue: This one gets stuck when no mutex is used
        repeat(repeat) {
            val jobs = listOf(
                launch { gatt.readRssi() },
                launch { gatt2.readRssi() }
            )
            jobs.forEach { it.join() }
        }
    }
}