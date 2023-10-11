package no.nordicsemi.android.kotlin.ble.app.client

import android.os.ParcelUuid
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.app.client.screen.view.BlinkyScreen
import no.nordicsemi.android.kotlin.ble.app.client.screen.viewmodel.BlinkySpecifications
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.ui.scanner.DeviceSelected
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerScreen
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScanningCancelled

val ScannerDestinationId = createSimpleDestination("scanner")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    val navigationViewModel = hiltViewModel<SimpleNavigationViewModel>()

    ScannerScreen(
        uuid = ParcelUuid(BlinkySpecifications.UUID_SERVICE_DEVICE),
        cancellable = false,
        onResult = {
            when (it) {
                is DeviceSelected -> navigationViewModel.navigateTo(BlinkyDestinationId, it.scanResults.device)
                ScanningCancelled -> navigationViewModel.navigateUp()
            }
        }
    )
}

val BlinkyDestinationId = createDestination<ServerDevice, Unit>("blinky")

val BlinkyDestination = defineDestination(BlinkyDestinationId) { BlinkyScreen() }
