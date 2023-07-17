package no.nordicsemi.android.kotlin.ble.test

import android.content.Context
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.common.core.ApplicationScope
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.logger.NordicBlekLogger
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class ReliableWriteTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val serviceRule = ServiceTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var logger: NordicBlekLogger

    @Inject
    lateinit var serverDevice: MockServerDevice

    @Inject
    lateinit var clientDevice: MockClientDevice

    @Inject
    lateinit var server: ReliableWriteServer

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
            mockkStatic("no.nordicsemi.android.common.core.ApplicationScopeKt")
            every { ApplicationScope } returns CoroutineScope(UnconfinedTestDispatcher())

            server.start(context, serverDevice)
        }
    }

    @Before
    fun prepareLogger() {
        mockkObject(NordicBlekLogger.Companion)
        every { NordicBlekLogger.create(any(), any(), any(), any()) } returns mockk()
    }

    @Test
    fun `when connected should return state connected`() = runTest {
        val connectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_CONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        val client = BleGattClient.connect(context, serverDevice)

        assertEquals(connectedState, client.connectionStateWithStatus.value)
    }

    @Test
    fun `when connected should return state disconnected`() = runTest {
        val connectedState = GattConnectionStateWithStatus(
            GattConnectionState.STATE_DISCONNECTED,
            BleGattConnectionStatus.SUCCESS
        )
        val client = BleGattClient.connect(context, serverDevice)

        server.stopServer()

        assertEquals(connectedState, client.connectionStateWithStatus.value)
    }

    @Test
    fun `when reliable aborted should return previous value`() = runTest {
        val client: BleGattClient = BleGattClient.connect(context, serverDevice)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)

        firstCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(DataByteArray.from(0x02))

        client.abortReliableWrite()

        assertEquals(initValue, firstCharacteristic.read())
    }

    @Test
    fun `when reliable aborted should return previous value on each characteristic`() = runTest {
        val client: BleGattClient = BleGattClient.connect(context, serverDevice)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        val secondCharacteristic = theService.findCharacteristic(SECOND_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)

        firstCharacteristic.write(initValue)
        secondCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(DataByteArray.from(0x02))
        secondCharacteristic.write(DataByteArray.from(0x02))

        client.abortReliableWrite()

        assertEquals(initValue, firstCharacteristic.read())
        assertEquals(initValue, secondCharacteristic.read())
    }

    @Test
    fun `when reliable executed should return new value`() = runTest {
        val client: BleGattClient = BleGattClient.connect(context, serverDevice)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)
        val newValue = DataByteArray.from(0x02)

        firstCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(newValue)

        client.executeReliableWrite()

        assertEquals(newValue, firstCharacteristic.read())
    }

    @Test
    fun `when reliable executed should return new value on each characteristic`() = runTest {
        val client: BleGattClient = BleGattClient.connect(context, serverDevice)

        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        val secondCharacteristic = theService.findCharacteristic(SECOND_CHARACTERISTIC)!!

        val initValue = DataByteArray.from(0x01)
        val newValue = DataByteArray.from(0x02)

        firstCharacteristic.write(initValue)
        secondCharacteristic.write(initValue)

        client.beginReliableWrite()

        firstCharacteristic.write(newValue)
        secondCharacteristic.write(newValue)

        client.executeReliableWrite()

        val firstReadValue = firstCharacteristic.read()
        val secondReadValue = secondCharacteristic.read()

        assertEquals(newValue, firstReadValue)
        assertEquals(newValue, secondReadValue)
    }
}
