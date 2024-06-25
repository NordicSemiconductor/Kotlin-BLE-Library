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
import no.nordicsemi.android.kotlin.ble.ui.scanner.CustomFilter
import no.nordicsemi.android.kotlin.ble.ui.scanner.DeviceSelected
import no.nordicsemi.android.kotlin.ble.ui.scanner.OnlyNearby
import no.nordicsemi.android.kotlin.ble.ui.scanner.OnlyWithNames
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerScreen
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScanningCancelled
import no.nordicsemi.android.kotlin.ble.ui.scanner.WithServiceUuid

val ScannerDestinationId = createSimpleDestination("scanner")

val ScannerDestination = defineDestination(ScannerDestinationId) {
    val navigationViewModel = hiltViewModel<SimpleNavigationViewModel>()

    val uuid = ParcelUuid(BlinkySpecifications.UUID_SERVICE_DEVICE)
    val filters = listOf(
        WithServiceUuid("Blinky", uuid, true),
        OnlyNearby(rssi = -50 /* dBm */, initiallySelected = false),
        OnlyWithNames(initiallySelected = true),
        CustomFilter("Nordic", false) { isFilterSelected, result ->
            !isFilterSelected || result.device.name?.contains("nordic", ignoreCase = true) == true
        }
    )
    ScannerScreen(
        filters = filters,
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
