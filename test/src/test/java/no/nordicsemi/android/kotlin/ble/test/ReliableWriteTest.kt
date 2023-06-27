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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.nordicsemi.android.kotlin.ble.client.main.ClientScope
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.logger.NordicBlekLogger
import no.nordicsemi.android.kotlin.ble.server.main.ServerScope
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
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
            mockkStatic("no.nordicsemi.android.kotlin.ble.client.main.ClientScopeKt")
            every { ClientScope } returns CoroutineScope(UnconfinedTestDispatcher())
            mockkStatic("no.nordicsemi.android.kotlin.ble.server.main.ServerScopeKt")
            every { ServerScope } returns CoroutineScope(UnconfinedTestDispatcher())

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
        val client = serverDevice.connect(context)

//        server.stopServer()

        advanceUntilIdle()

        assertEquals(connectedState, client.connectionStateWithStatus.value)
    }
}
