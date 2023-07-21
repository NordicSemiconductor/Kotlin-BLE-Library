package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

/**
 * A class responsible for generating new requests with unique request ids and store them.
 * After the request has been processed by the server stored request is used to send a response.
 *
 */
internal class MockRequestProvider {
    private var requestId = 0
    private val requests = mutableMapOf<Int, SendResponseRequest>()

    /**
     * Creates new characteristic write request.
     *
     * @param characteristic Id of a characteristic.
     * @return New [SendResponseRequest] request.
     */
    fun newWriteRequest(characteristic: IBluetoothGattCharacteristic): SendResponseRequest {
        return MockCharacteristicWrite(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    /**
     * Creates new characteristic read request.
     *
     * @param characteristic Id of a characteristic.
     * @return New [SendResponseRequest] request.
     */
    fun newReadRequest(characteristic: IBluetoothGattCharacteristic): SendResponseRequest {
        return MockCharacteristicRead(requestId++, characteristic).also {
            requests[it.requestId] = it
        }
    }

    /**
     * Creates new descriptor write request.
     *
     * @param descriptor Id of a descriptor.
     * @return New [SendResponseRequest] request.
     */
    fun newWriteRequest(descriptor: IBluetoothGattDescriptor): SendResponseRequest {
        return MockDescriptorWrite(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    /**
     * Creates new descriptor read request.
     *
     * @param descriptor Id of a descriptor.
     * @return New [SendResponseRequest] request.
     */
    fun newReadRequest(descriptor: IBluetoothGattDescriptor): SendResponseRequest {
        return MockDescriptorRead(requestId++, descriptor).also {
            requests[it.requestId] = it
        }
    }

    /**
     * Creates new execute reliable write request.
     *
     * @return New [SendResponseRequest] request.
     */
    fun newExecuteReliableWriteRequest(): ReliableWriteRequest {
        return MockExecuteReliableWrite(requestId++)
    }

    /**
     * Creates new abort reliable write request.
     *
     * @return New [SendResponseRequest] request.
     */
    fun newAbortReliableWriteRequest(): ReliableWriteRequest {
        return MockAbortReliableWrite(requestId++)
    }

    /**
     * Gets previously store request.
     *
     * @param requestId Unique request id.
     * @return Stored [SendResponseRequest] request.
     */
    fun getRequest(requestId: Int): SendResponseRequest {
        return requests.remove(requestId)!!
    }
}
