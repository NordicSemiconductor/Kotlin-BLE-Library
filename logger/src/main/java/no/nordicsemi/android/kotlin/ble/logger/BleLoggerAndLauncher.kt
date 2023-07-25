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

package no.nordicsemi.android.kotlin.ble.logger

import android.content.Context
import android.util.Log

/**
 * Interface grouping [BleRemoteLoggerLauncher] and [BleLogger]. Needed to be used as a return type.
 */
interface BleLoggerAndLauncher : BleRemoteLoggerLauncher, BleLogger

/**
 * Functional interface responsible for launching a dedicated logger app.
 */
fun interface BleRemoteLoggerLauncher {

    /**
     * Launches dedicated logger app.
     */
    fun launch()
}

/**
 * Functional interface which defines logs logging action.
 */
fun interface BleLogger {

    /**
     * Prints log.
     *
     * @param priority Priority of the logs.
     * @param log Message.
     */
    fun log(priority: Int, log: String)
}

/**
 * Default implementation of [BleLoggerAndLauncher] which print logs to Logcat and launch main page
 * of [the Logger app](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log&hl=en&gl=US).
 *
 * @property context An application context.
 */
class DefaultBlekLogger(private val context: Context) : BleLoggerAndLauncher {

    override fun log(priority: Int, log: String) {
        Log.println(priority, "BLEK-LOG", log)
    }

    override fun launch() {
        LoggerLauncher.launch(context)
    }
}
