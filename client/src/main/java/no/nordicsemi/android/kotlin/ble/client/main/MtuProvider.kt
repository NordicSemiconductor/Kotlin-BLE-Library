package no.nordicsemi.android.kotlin.ble.client.main

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.Mtu

internal object MtuProvider {
    val mtu = MutableStateFlow(Mtu.min)

    fun availableMtu(writeType: BleWriteType): Int {
        return when (writeType) {
            BleWriteType.DEFAULT -> mtu.value - Mtu.defaultWrite
            BleWriteType.NO_RESPONSE -> mtu.value - Mtu.defaultWrite
            BleWriteType.SIGNED -> mtu.value - Mtu.signedWrite
        }
    }
}
