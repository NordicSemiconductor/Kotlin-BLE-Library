/*
 * Copyright (c) 2024, Nordic Semiconductor
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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Splits the data into chunks of given length. The last chunk may be shorter.
 *
 * @param size The maximum size of a chunk.
 * @return A list of chunks.
 */
fun ByteArray.chunked(size: Int): List<ByteArray> {
    val chunks = mutableListOf<ByteArray>()
    var offset = 0
    while (offset < this.size) {
        val length = minOf(size, this.size - offset)
        chunks.add(copyOfRange(offset, offset + length))
        offset += length
    }
    return chunks
}

/**
 * Collects the flow of data and emits a new flow of chunked data using the given operation.
 *
 * @param size The maximum size of a chunk.
 * @param operation The operation that will split the data into chunks.
 * @return A flow of chunked data.
 */
fun Flow<ByteArray>.split(size: Int, operation: suspend (data: ByteArray, size: Int) -> List<ByteArray>): Flow<ByteArray> = flow {
    collect { full ->
        operation(full, size).forEach { chunk ->
            emit(chunk)
        }
    }
}

/**
 * Collects the flow of data and emits a new flow of packet of at-most given size.
 *
 * @param size The maximum size of a packet.
 * @return A flow of packets.
 */
fun Flow<ByteArray>.split(size: Int): Flow<ByteArray> = split(size) { data, _ -> data.chunked(size) }

/**
 * Collects the flow of packets and emits a new flow of merged data using the given operation.
 *
 * This operator can be used to combine data received in multiple chunks into a single message.
 * The implementation of the operation is application-specific. Most common use cases are:
 * * Known length of the message.
 * * Message with a header that contains the length of the message.
 * * Each packet contains a flag indicating *single*, *first*, *middle*, or *last* packet.
 * * Trying to parse the message (e.g. convert to a valid JSON) after each chunk.
 *
 * On the first run, the accumulator array will be empty. The method should return
 * [MergeResult.Accumulate] if more data is expected.
 * The [MergeResult.Accumulate.accumulated] array will be passed as the accumulator parameter
 * in the next call when more data is received.
 *
 * Return [MergeResult.Completed] when the full message has been received to emit the result.
 *
 * @param operation The operation that will merge the data.
 * @return A flow of merged data.
 */
fun Flow<ByteArray>.merge(operation: suspend (accumulator: ByteArray, received: ByteArray) -> MergeResult): Flow<ByteArray> = flow {
    var accumulator = byteArrayOf()
    collect { received ->
        accumulator = when (val result = operation(accumulator, received)) {
            is MergeResult.Accumulate -> {
                result.accumulated
            }

            is MergeResult.Completed -> {
                emit(result.result)
                byteArrayOf()
            }
        }
    }
}

/**
 * Collects the flow of packets and emits a new flow of merged data using the given operation.
 *
 * This operator can be used to combine data received in multiple chunks into a single message.
 * The implementation of the operation is application-specific. Most common use cases are:
 * * Known length of the message.
 * * Message with a header that contains the length of the message.
 * * Each packet contains a flag indicating *single*, *first*, *middle*, or *last* packet.
 * * Trying to parse the message (e.g. convert to a valid JSON) after each chunk.
 *
 * On the first run, the accumulator array will be empty. The method should return
 * [MergeResult.Accumulate] if more data is expected.
 * The [MergeResult.Accumulate.accumulated] array will be passed as the accumulator parameter
 * in the next call when more data is received.
 *
 * Return [MergeResult.Completed] when the full message has been received to emit the result.
 *
 * @param operation The operation that will merge the data.
 * @return A flow of merged data.
 */
fun Flow<ByteArray>.mergeIndexed(operation: suspend (accumulator: ByteArray, received: ByteArray, index: Int) -> MergeResult): Flow<ByteArray> = flow {
    var accumulator = byteArrayOf()
    var index = 0
    collect { received ->
        accumulator = when (val result = operation(accumulator, received, index++)) {
            is MergeResult.Accumulate -> {
                result.accumulated
            }

            is MergeResult.Completed -> {
                emit(result.result)
                index = 0
                byteArrayOf()
            }
        }
    }
}

/**
 * The result of the [Flow.merge] operation.
 */
sealed class MergeResult {
    /** The next chunk of the array. */
    class Accumulate(val accumulated: ByteArray): MergeResult()
    /** No more chunks are available. */
    class Completed(val result: ByteArray): MergeResult()
}