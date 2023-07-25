package no.nordicsemi.android.kotlin.ble.core.data

import android.bluetooth.BluetoothDevice

/**
 * Defines available bonding states.
 *
 * @property value Native Android value.
 */
enum class BondState(private val value: Int) {

    /**
     * Device is not bonded and bonding process has not been initiated.
     */
    NONE(BluetoothDevice.BOND_NONE),

    /**
     * Device is not bonded, but bonding process has been initiated.
     */
    BONDING(BluetoothDevice.BOND_BONDING),

    /**
     * Device is bonded.
     */
    BONDED(BluetoothDevice.BOND_BONDED);

    companion object {

        /**
         * Creates a bonding state from [Int] value.
         *
         * @throws IllegalArgumentException when bond state cannot be decoded.
         *
         * @param value [Int] value of a bond state.
         * @return Decoded bond state.
         */
        fun create(value: Int): BondState {
            return values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create state for value: $value")
        }
    }
}
