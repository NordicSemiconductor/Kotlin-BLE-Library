package no.nordicsemi.android.kotlin.ble.client.main.bonding

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import no.nordicsemi.android.kotlin.ble.client.api.OnBondStateChanged
import no.nordicsemi.android.kotlin.ble.client.real.ClientBleGattCallback
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.core.data.BondState

/**
 * A broadcast receiver to observe [BluetoothDevice.ACTION_BOND_STATE_CHANGED] events.
 * It contains a list of BLE devices and notifies using callback ([ClientBleGattCallback])
 * about their bond state changes.
 */
class BondingBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
        val address = device.address

        if (action == ACTION_BOND_STATE_CHANGED) {
            val bondState = intent.getIntExtra(EXTRA_BOND_STATE, ERROR)
            val previousBondState = intent.getIntExtra(EXTRA_PREVIOUS_BOND_STATE, ERROR)

            callbacks[address]?.onEvent(OnBondStateChanged(BondState.create(bondState)))
        }
    }

    companion object {

        private val callbacks = mutableMapOf<String, ClientBleGattCallback>()

        private var instance: BondingBroadcastReceiver? = null

        /**
         * Registers new [BondingBroadcastReceiver]. If one instance already exists then it just
         * add new records to observe.
         *
         * @param context An application context.
         * @param device An BLE device which changes should be observed.
         * @param callback Bond state changes callback.
         */
        fun register(context: Context, device: BleDevice, callback: ClientBleGattCallback) {
            callbacks[device.address] = callback
            callback.onEvent(OnBondStateChanged(device.bondState))

            if (instance == null) {
                instance = BondingBroadcastReceiver()
                context.applicationContext.registerReceiver(instance, IntentFilter(ACTION_BOND_STATE_CHANGED))
            }
        }

        /**
         * Unregisters bond state changes callbacks. If this is the last observed callback then
         * BroadcastReceiver will be unregistered.
         *
         * @param context An application context.
         * @param address An BLE device address which changes should be stopped observing.
         */
        fun unregisterReceiver(context: Context, address: String) {
            callbacks.remove(address)
            if (callbacks.isEmpty()) {
                context.unregisterReceiver(instance)
                instance = null
            }
        }
    }
}
