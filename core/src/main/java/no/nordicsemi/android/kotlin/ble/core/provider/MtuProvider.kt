package no.nordicsemi.android.kotlin.ble.core.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.Mtu

/**
 * Provides an MTU value.
 *
 * MTU value is shared between many components. To avoid propagating MTU value changed event to
 * all of the components the [MtuProvider] is shared instead in constructor and value is updated
 * using it's field.
 *
 */
class MtuProvider {

    private val _mtu = MutableStateFlow(Mtu.min)

    /**
     * Most recent MTU value.
     */
    val mtu = _mtu.asStateFlow()

    /**
     * Updates MTU value and notifies observers.
     *
     * @param mtu New MTU value.
     */
    fun updateMtu(mtu: Int) {
        _mtu.value = mtu
    }

    /**
     * Calculates available size for write operation when a particular [writeType] is going to be
     * used.
     *
     * @param writeType Selected write type.
     * @return Available space for value in write operation in bytes.
     */
    fun availableMtu(writeType: BleWriteType): Int {
        return when (writeType) {
            BleWriteType.DEFAULT -> mtu.value - Mtu.defaultWrite
            BleWriteType.NO_RESPONSE -> mtu.value - Mtu.defaultWrite
            BleWriteType.SIGNED -> mtu.value - Mtu.signedWrite
        }
    }
}
