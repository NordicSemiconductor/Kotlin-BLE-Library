package no.nordicsemi.android.kotlin.ble.test

import android.content.Context
import androidx.test.rule.ServiceTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import org.junit.After
import org.junit.Assert
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
class WriteNoResponseTest {

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

    @Inject
    lateinit var clientDevice: MockClientDevice

    @Inject
    lateinit var server: WriteNoResponseServer

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
        }
    }

    @Test
    fun `when write no response should return success`() = runTest {
        val client: ClientBleGatt = ClientBleGatt.connect(context, serverDevice, scope = scope)
        val services = client.discoverServices()
        val theService = services.findService(RELIABLE_WRITE_SERVICE)!!
        val firstCharacteristic = theService.findCharacteristic(FIRST_CHARACTERISTIC)!!

        val value = DataByteArray.from(0x01)

        firstCharacteristic.write(value, writeType = BleWriteType.NO_RESPONSE)

        val readValue = firstCharacteristic.read()

        Assert.assertEquals(value, readValue)
    }
}
