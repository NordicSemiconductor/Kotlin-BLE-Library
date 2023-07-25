package no.nordicsemi.android.kotlin.ble.mock

import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData

/**
 * An object responsible for providing list of advertising servers. Each server is represented
 * by [MockServerDevice]. It "advertises" with specified [BleScanResultData].
 */
object MockDevices {

    val devices: StateFlow<Map<MockServerDevice, BleScanResultData>> = MockEngine.advertisedServers
}
