package no.nordicsemi.android.kotlin.ble.app.mock

import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.kotlin.ble.app.mock.scanner.view.ScannerScreen
import no.nordicsemi.android.kotlin.ble.app.mock.screen.view.BlinkyScreen
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

val ScannerDestinationId = createSimpleDestination("scanner")

val ScannerDestination = defineDestination(ScannerDestinationId) { ScannerScreen() }

val BlinkyDestinationId = createDestination<ServerDevice, Unit>("blinky")

val BlinkyDestination = defineDestination(BlinkyDestinationId) { BlinkyScreen() }
