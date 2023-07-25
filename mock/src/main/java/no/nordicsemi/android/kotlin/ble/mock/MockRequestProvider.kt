/*
 * Copyright (c) 2023, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

/**
 * A class responsible for generating new requests with unique request ids and store them.
 * After the request has been processed by the server stored request is used to send a response.
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
