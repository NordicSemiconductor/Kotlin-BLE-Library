/*
 * Copyright (c) 2026, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.core.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * A marker exception whose only purpose is to capture a stack trace at a point of interest, e.g.
 * where a public API function was called, or where a [kotlinx.coroutines.flow.Flow] it returned
 * started being collected.
 *
 * @see <a href="https://github.com/nordicsemi/Kotlin-BLE-Library/issues/330">Issue #330</a>
 */
class CallSiteException(message: String) : Exception(message)

/**
 * Runs [block], and if it throws anything other than [CancellationException], attaches the call
 * site of this function as a suppressed [CallSiteException] before rethrowing.
 *
 * Intended to wrap the body of public suspending API functions that internally collect a
 * [Flow], so that a failure reported deep inside that flow still points back to the caller.
 *
 * @param operation The name of the wrapped public API method, used only in the exception message.
 */
suspend fun <T> withCallSite(operation: String, block: suspend () -> T): T {
    val callSite = CallSiteException("$operation() was called here")
    try {
        return block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        e.addSuppressed(callSite)
        throw e
    }
}
