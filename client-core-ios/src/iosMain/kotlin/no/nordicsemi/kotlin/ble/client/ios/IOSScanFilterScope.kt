package no.nordicsemi.kotlin.ble.client.ios

import no.nordicsemi.kotlin.ble.client.CentralManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface IOSScanFilterScope : CentralManager.ScanFilterScope {

    @Suppress("FunctionName")
    fun Name(name: String)

    @Suppress("FunctionName")
    fun Name(regex: Regex)

    @Suppress("FunctionName")
    fun ServiceUuid(uuid: Uuid)
}
