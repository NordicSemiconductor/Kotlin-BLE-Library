package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

internal class MockRequestHolder {
    private var requestId = 0
    private val requests = mutableMapOf<Int, MockRequest>()

    fun newWriteRequest(characteristic: IBluetoothGattCharacteristic): MockRequest {
        return MockCharacteristicWrite(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    fun newReadRequest(characteristic: IBluetoothGattCharacteristic): MockRequest {
        return MockCharacteristicRead(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    fun newWriteRequest(descriptor: IBluetoothGattDescriptor): MockRequest {
        return MockDescriptorWrite(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    fun newReadRequest(descriptor: IBluetoothGattDescriptor): MockRequest {
        return MockDescriptorRead(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    fun getRequest(requestId: Int): MockRequest {
        return requests.remove(requestId)!!
    }
}
