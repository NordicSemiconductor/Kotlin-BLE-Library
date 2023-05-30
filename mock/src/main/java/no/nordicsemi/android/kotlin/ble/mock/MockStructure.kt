package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.advertiser.data.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

class MockStructure {
    private val advertisers = mutableMapOf<ServerDevice, BleAdvertiseConfig>()

    private val servers = mutableMapOf<ServerDevice, GattServerAPI>()
    private val serverConnections = mutableMapOf<ServerDevice, ServerConnection>()

    private val clients = mutableMapOf<ClientDevice, GattClientAPI>()
    private val clientConnections = mutableMapOf<ClientDevice, ServerDevice>()
}
