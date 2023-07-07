package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

internal class MockRequestHolder {
    private var requestId = 0
    private val requests = mutableMapOf<Int, SendResponseRequest>()

    fun newWriteRequest(characteristic: IBluetoothGattCharacteristic): SendResponseRequest {
        return MockCharacteristicWrite(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    fun newReadRequest(characteristic: IBluetoothGattCharacteristic): SendResponseRequest {
        return MockCharacteristicRead(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    fun newWriteRequest(descriptor: IBluetoothGattDescriptor): SendResponseRequest {
        return MockDescriptorWrite(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    fun newReadRequest(descriptor: IBluetoothGattDescriptor): SendResponseRequest {
        return MockDescriptorRead(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    fun newExecuteReliableWriteRequest(): ReliableWriteRequest {
        return MockExecuteReliableWrite(requestId++)
    }

    fun newAbortReliableWriteRequest(): ReliableWriteRequest {
        return MockAbortReliableWrite(requestId++)
    }

    fun getRequest(requestId: Int): SendResponseRequest {
        return requests.remove(requestId)!!
    }
}
