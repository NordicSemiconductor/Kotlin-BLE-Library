package no.nordicsemi.android.kotlin.ble.client.main

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.Mtu

internal object MtuProvider {
    val mtu = MutableStateFlow(Mtu.min)
}
