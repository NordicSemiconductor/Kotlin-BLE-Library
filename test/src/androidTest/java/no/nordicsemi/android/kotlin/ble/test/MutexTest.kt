package no.nordicsemi.android.kotlin.ble.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.test.utils.BlinkySpecifications
import no.nordicsemi.android.kotlin.ble.test.utils.TestAddressProvider
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MutexTest {

    private val service = BlinkySpecifications.UUID_SERVICE_DEVICE
    private val ledCharacteristic = BlinkySpecifications.UUID_LED_CHAR
    private val buttonCharacteristic = BlinkySpecifications.UUID_BUTTON_CHAR
    private val cccd = BlinkySpecifications.NOTIFICATION_DESCRIPTOR

    private val address = TestAddressProvider.address
    private val address2 = TestAddressProvider.auxiliaryAddress

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    private val testCount = 10

    @Test
    fun whenReadRssiMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        repeat(testCount) {
            val jobs = listOf(
                launch { gatt.readRssi() },
                launch { gatt2.readRssi() }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenReadPhyMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        repeat(testCount) {
            val jobs = listOf(
                launch { gatt.readPhy() },
                launch { gatt2.readPhy() }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenSetPhyMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        repeat(testCount) {
            val jobs = listOf(
                launch {
                    gatt.setPhy(
                        BleGattPhy.PHY_LE_1M,
                        BleGattPhy.PHY_LE_1M,
                        PhyOption.NO_PREFERRED
                    )
                },
                launch {
                    gatt2.setPhy(
                        BleGattPhy.PHY_LE_1M,
                        BleGattPhy.PHY_LE_1M,
                        PhyOption.NO_PREFERRED
                    )
                }
            )
            jobs.forEach { it.join() }
        }
    }

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
                launch { char.read() },
                launch { char2.read() }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenWriteCharacteristicMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        val services = gatt.discoverServices()
        val services2 = gatt2.discoverServices()

        val char = services.findService(service)?.findCharacteristic(ledCharacteristic)!!
        val char2 = services2.findService(service)?.findCharacteristic(ledCharacteristic)!!

        repeat(testCount) {
            val jobs = listOf(
                launch { char.write(DataByteArray.from(0x01)) },
                launch { char2.write(DataByteArray.from(0x01)) }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenReadDescriptorMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        val services = gatt.discoverServices()
        val services2 = gatt2.discoverServices()

        val desc = services.findService(service)?.findCharacteristic(buttonCharacteristic)?.findDescriptor(cccd)!!
        val desc2 = services2.findService(service)?.findCharacteristic(buttonCharacteristic)?.findDescriptor(cccd)!!

        repeat(testCount) {
            val jobs = listOf(
                launch { desc.read() },
                launch { desc2.read() }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenWriteDescriptorMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, address, scope)
        val gatt2 = ClientBleGatt.connect(context, address2, scope)

        val services = gatt.discoverServices()
        val services2 = gatt2.discoverServices()

        val desc = services.findService(service)?.findCharacteristic(buttonCharacteristic)?.findDescriptor(cccd)!!
        val desc2 = services2.findService(service)?.findCharacteristic(buttonCharacteristic)?.findDescriptor(cccd)!!

        repeat(testCount) {
            val jobs = listOf(
                launch { desc.write(DataByteArray.from(0x01)) },
                launch { desc2.write(DataByteArray.from(0x01)) }
            )
            jobs.forEach { it.join() }
        }
    }
}
