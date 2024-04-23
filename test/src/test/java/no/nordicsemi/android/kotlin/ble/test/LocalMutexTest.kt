package no.nordicsemi.android.kotlin.ble.test

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

//FIXME: Tests don't work when run simultaneously.
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class LocalMutexTest {

    private val service = BlinkySpecifications.UUID_SERVICE_DEVICE
    private val ledCharacteristic = BlinkySpecifications.UUID_LED_CHAR
    private val buttonCharacteristic = BlinkySpecifications.UUID_BUTTON_CHAR
    private val cccd = BlinkySpecifications.NOTIFICATION_DESCRIPTOR

    private val testCount = 10

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @RelaxedMockK
    lateinit var context: Context

    @Inject
    lateinit var serverDevice: MockServerDevice

    private val serverDevice2: MockServerDevice = MockServerDevice(
        name = "GLS Server",
        address = "11:22:33:44:55:66"
    )

    @Inject
    lateinit var server: BlinkyServerProvider

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun release() {
        Dispatchers.resetMain()
    }

    @Before
    fun before() {
        runBlocking {
            server.start(context, serverDevice)
            server.start(context, serverDevice2)
        }
    }
    @Test
    fun whenReadRssiMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
    fun whenRequestMtuMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

        repeat(testCount) {
            val jobs = listOf(
                launch { gatt.requestMtu(23) },
                launch { gatt2.requestMtu(23) }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenDiscoverServicesMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

        repeat(testCount) {
            val jobs = listOf(
                launch { gatt.discoverServices() },
                launch { gatt2.discoverServices() }
            )
            jobs.forEach { it.join() }
        }
    }

    @Test
    fun whenReadCharacteristicMultipleTimesShouldSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
        val gatt = ClientBleGatt.connect(context, serverDevice, scope)
        val gatt2 = ClientBleGatt.connect(context, serverDevice2, scope)

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
