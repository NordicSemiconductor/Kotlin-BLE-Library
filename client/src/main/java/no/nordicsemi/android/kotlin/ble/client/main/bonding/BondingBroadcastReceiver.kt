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
import no.nordicsemi.android.kotlin.ble.client.real.BluetoothGattClientCallback
import no.nordicsemi.android.kotlin.ble.core.data.BondState

class BondingBroadcastReceiver : BroadcastReceiver() {

    private val callbacks = mutableMapOf<String, BluetoothGattClientCallback>()

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

        private var instance: BondingBroadcastReceiver? = null

        fun register(context: Context, address: String, callback: BluetoothGattClientCallback) {
            if (instance != null) return
            instance = BondingBroadcastReceiver().also {
                it.callbacks[address] = callback
            }
            context.registerReceiver(instance, IntentFilter(ACTION_BOND_STATE_CHANGED))
        }

        fun unregisterReceiver(context: Context) {
            instance?.let { context.unregisterReceiver(it) }
            instance = null
        }
    }
}
