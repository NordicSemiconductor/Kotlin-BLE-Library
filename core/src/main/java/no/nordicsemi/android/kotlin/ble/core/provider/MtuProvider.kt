package no.nordicsemi.android.kotlin.ble.core.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.Mtu

class MtuProvider {
    private val _mtu = MutableStateFlow(Mtu.min)
    val mtu = _mtu.asStateFlow()

    fun updateMtu(mtu: Int) {
        _mtu.value = mtu
    }

    fun availableMtu(writeType: BleWriteType): Int {
        return when (writeType) {
            BleWriteType.DEFAULT -> mtu.value - Mtu.defaultWrite
            BleWriteType.NO_RESPONSE -> mtu.value - Mtu.defaultWrite
            BleWriteType.SIGNED -> mtu.value - Mtu.signedWrite
        }
    }
}
